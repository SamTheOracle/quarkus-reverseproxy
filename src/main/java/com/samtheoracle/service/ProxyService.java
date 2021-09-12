package com.samtheoracle.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.samtheoracle.discovery.Record;
import com.samtheoracle.discovery.ServiceDiscoveryHelper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samtheoracle.service.cache.CacheService;
import com.samtheoracle.service.cache.ProxyResponse;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
public class ProxyService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final static String CACHE_ID_NAME = "Cache-Id";

    @ConfigProperty(name = "proxy.reroute.timeout")
    Integer timeout;

    @ConfigProperty(name = "proxy.reroute.cache.maxage")
    Integer cacheMaxAge;

    @Inject
    ServiceDiscoveryHelper discoveryHelper;

    @Inject
    CacheService cacheService;

    @Inject
    WebClient webClient;


    @PostConstruct
    void init() {
        logger.info("Reverse proxy service created");
    }

    public Uni<ProxyResponse> handleRerouteWithBody(String uri, MultivaluedMap<String, String> headers, byte[] body, HttpMethod method,
                                                    MultivaluedMap<String, String> queryParameters) {
        String serviceRoot = "/" + uri.split("/")[0];
        Uni<Record> recordUni = discoveryHelper.getRecord(serviceRoot);
        Uni<HttpResponse<Buffer>> httpResponseUni = recordUni.onItem().transformToUni(record -> {
            String host = record.getLocation().getHost();
            Integer port = record.getLocation().getPort();
            String fullUri = "http://" + host + ":" + port + "/" + uri;
            logger.debug("making http request to {}", fullUri);
            return reroute(webClient, "/" + uri, host, port, headers, timeout * 1000, method, body, queryParameters);
        });
        return handleHttpResponse(httpResponseUni, serviceRoot);

    }

    public Uni<ProxyResponse> handleRerouteWithCache(String uri, MultivaluedMap<String, String> headers,
                                                     MultivaluedMap<String, String> queryParameters) {
        Optional<String> cacheExp = Optional.ofNullable(headers.getFirst(HttpHeaderNames.CACHE_CONTROL.toString()));
        Optional<String> cacheId = Optional.ofNullable(headers.getFirst(CACHE_ID_NAME));
        String serviceRoot = "/" + uri.split("/")[0];
        logger.debug("full uri {}, root {}", uri, serviceRoot);
        if (cacheExp.isEmpty() && cacheId.isPresent()) {
            return rerouteGetRequest(serviceRoot, uri, cacheId.get(), headers, queryParameters);
        }
        int age;
        String maxAge = cacheExp.orElse("0");
        if (maxAge.contains(HttpHeaderValues.MAX_AGE + "=")) {
            age = Integer.parseInt(maxAge.replace(HttpHeaderValues.MAX_AGE + "=", ""));
        } else {
            age = 0;
        }
        int cacheAge = Math.min(age, cacheMaxAge);
        if (cacheAge == 0) {
            return rerouteGetRequest(serviceRoot, uri, headers, queryParameters);
        }
        String newCacheId = UUID.randomUUID().toString();
        return rerouteGetRequest(serviceRoot, uri, newCacheId, cacheAge, headers, queryParameters);
    }

    private Uni<ProxyResponse> rerouteGetRequest(String root, String uri, MultivaluedMap<String, String> headers,
                                                 MultivaluedMap<String, String> queryParameters) {
        Uni<Record> recordUni = discoveryHelper.getRecord(root);
        Uni<HttpResponse<Buffer>> httpResponseUni = recordUni.onItem().ifNotNull().transformToUni(record -> {
            String host = record.getLocation().getHost();
            Integer port = record.getLocation().getPort();
            String fullUri = "http://" + host + ":" + port + "/" + uri;
            logger.debug("making http request to {}", fullUri);
            return reroute(webClient, "/" + uri, host, port, headers, timeout * 1000, HttpMethod.GET, null, queryParameters);
        });
        return handleHttpResponse(httpResponseUni, root);
    }

    private Uni<ProxyResponse> rerouteGetRequest(String root, String uri, String cacheId, MultivaluedMap<String, String> headers,
                                                 MultivaluedMap<String, String> queryParameters) {
        logger.debug("Trying to get cache for id {}", cacheId);
        Uni<String> cachedDataUni = cacheService.get(cacheId);
        return cachedDataUni
                .onFailure()
                .recoverWithNull()
                .onItem()
                .transformToUni(redisResult -> {
                    if (redisResult != null) {
                        return Uni.createFrom().item(ProxyResponse.create(Buffer.buffer(redisResult.getBytes(StandardCharsets.UTF_8)), true,
                                Response.Status.OK.getStatusCode(), cacheId));
                    }
                    logger.debug("No cache available for id {}, trying to proxy request to service {}", cacheId, root);
                    return rerouteGetRequest(root, uri, headers, queryParameters);
                });
    }

    private Uni<ProxyResponse> rerouteGetRequest(String root, String uri, String cacheId, int cacheEx, MultivaluedMap<String, String> headers,
                                                 MultivaluedMap<String, String> queryParameters) {
        return rerouteGetRequest(root, uri, cacheId, headers,
                queryParameters).onFailure().recoverWithNull().onItem().ifNull().fail().onItem().ifNotNull().transformToUni(proxyResponse -> {
            if (proxyResponse.getStatus() >= 400) {
                return Uni.createFrom().item(proxyResponse);
            }
            return cacheService.set(cacheId, proxyResponse.getData().toString(), cacheEx).onItem().transform(
                    response -> ProxyResponse.create(proxyResponse.getData(), true, proxyResponse.getStatus(), cacheId));
        });
    }


    private static Uni<HttpResponse<Buffer>> reroute(WebClient webClient, String uri, String host, int port,
                                                     MultivaluedMap<String, String> headers, int timeout, HttpMethod method, byte[] body,
                                                     MultivaluedMap<String, String> queryParameters) {
        MultiMap httpRequestHeaders = MultiMap.caseInsensitiveMultiMap();
        headers.forEach(httpRequestHeaders::add);
        HttpRequest<Buffer> request = webClient.request(method, port, host, uri).timeout(timeout).putHeaders(httpRequestHeaders);
        MultiMap queryParametersRequest = MultiMap.caseInsensitiveMultiMap();
        queryParameters.forEach(queryParametersRequest::add);
        queryParametersRequest.forEach(nameValue -> request.addQueryParam(nameValue.getKey(), nameValue.getValue()));
        return body == null ? request.send() : request.sendBuffer(Buffer.buffer(body));
    }

    private static Uni<ProxyResponse> handleHttpResponse(Uni<HttpResponse<Buffer>> httpResponseUni, String serviceRoot) {
        return httpResponseUni.onItem().transformToUni(bufferHttpResponse -> {
            int status = bufferHttpResponse.statusCode();

            ProxyResponse response;
            if (status >= 400) {
                Buffer responseBody = bufferHttpResponse.bodyAsBuffer() == null ? Buffer.buffer("Error from service " + serviceRoot) :
                        bufferHttpResponse.bodyAsBuffer();
                response = ProxyResponse.create(responseBody, false, bufferHttpResponse.statusCode());
                return Uni.createFrom().item(response);
            }
            Buffer responseBody = bufferHttpResponse.bodyAsBuffer();
            response = ProxyResponse.create(responseBody, false, bufferHttpResponse.statusCode());
            return Uni.createFrom().item(response);
        });
    }

}
