package com.samtheoracle.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.samtheoracle.config.ProxyConfigurator;
import com.samtheoracle.service.cache.CacheService;
import com.samtheoracle.service.cache.ProxyResponse;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.servicediscovery.Record;

@ApplicationScoped
public class ReverseProxyService {
	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	@ConfigProperty(name = "proxy.reroute.timeout")
	Integer timeout;

	@ConfigProperty(name = "proxy.reroute.cache.maxage")
	Integer cacheMaxAge;

	@Inject
	ProxyConfigurator proxyConfigurator;

	@Inject
	ServiceDiscoveryHelper discoveryHelper;

	@Inject
	CacheService cacheService;

	WebClient webClient;

	@PostConstruct
	void init() {
		logger.info("Reverse proxy service created");
		webClient = proxyConfigurator.getWebClient();
	}

	public Uni<ProxyResponse> handleGetReroute(String uri, MultivaluedMap<String, String> headers) {
		Optional<String> cacheExp = Optional.ofNullable(headers.getFirst(HttpHeaderNames.CACHE_CONTROL.toString()));
		String serviceRoot = "/" + uri.split("/")[0];
		if (cacheExp.isEmpty()) {
			return rerouteGetRequest(serviceRoot, uri, headers);
		}
		int age;
		String maxAge = cacheExp.get();
		if (maxAge.contains(HttpHeaderValues.MAX_AGE + "=")) {
			age = Integer.parseInt(maxAge.replace(HttpHeaderValues.MAX_AGE + "=", ""));
		} else {
			age = 0;
		}
		int cacheAge = Math.min(age, cacheMaxAge);
		return rerouteGetRequest(serviceRoot, uri, cacheAge, headers);
	}

	private Uni<ProxyResponse> rerouteGetRequest(String root, String uri, MultivaluedMap<String, String> headers) {

		Uni<String> cachedDataUni = cacheService.get(uri);
		Optional<String> cachedDataOptional = cachedDataUni.await().asOptional().atMost(Duration.ofMillis(1000));
		if (cachedDataOptional.isPresent()) {
			return Uni.createFrom().item(
					ProxyResponse.create(Buffer.buffer(cachedDataOptional.get().getBytes(StandardCharsets.UTF_8)), true,
							Response.Status.OK.getStatusCode()));
		}
		Uni<Record> recordUni = cachedDataUni.onFailure().recoverWithNull().onItem().transformToUni(
				data -> discoveryHelper.getRecord(root));
		Uni<HttpResponse<Buffer>> httpResponseUni = recordUni.onItem().ifNotNull().transformToUni(record -> {
			String host = record.getLocation().getString("host");
			Integer port = record.getLocation().getInteger("port");
			String fullUri = "http://" + host + ":" + port + "/" + uri;
			logger.info("making http request to " + fullUri);
			return reroute(webClient, "/" + uri, host, port, headers);
		});

		return httpResponseUni.onItem().transform(bufferHttpResponse -> ProxyResponse.create(bufferHttpResponse.bodyAsBuffer(), false, bufferHttpResponse.statusCode()));
	}

	private Uni<ProxyResponse> rerouteGetRequest(String root, String uri, int cacheEx, MultivaluedMap<String, String> headers) {
		return rerouteGetRequest(root, uri, headers).onItem().ifNotNull().transformToUni(
				proxyResponse -> cacheService.set(uri, proxyResponse.getData().toString(), cacheEx).onItem().transform(
						response -> ProxyResponse.create(proxyResponse.getData(), true, proxyResponse.getStatus())));
	}

	private static Uni<HttpResponse<Buffer>> reroute(WebClient webClient, String uri, String host, int port,
			MultivaluedMap<String, String> headers) {
		MultiMap httpRequestHeaders = MultiMap.caseInsensitiveMultiMap();
		headers.forEach(httpRequestHeaders::add);
		return webClient.get(port, host, uri).putHeaders(httpRequestHeaders).send();
	}

}
