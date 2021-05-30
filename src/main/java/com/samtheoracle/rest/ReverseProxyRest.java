package com.samtheoracle.rest;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.samtheoracle.converter.ReverseProxyConverter;
import com.samtheoracle.service.ErrorHandlerService;
import com.samtheoracle.service.ProxyService;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

@Path("/")
public class ReverseProxyRest {

	@Inject
	ProxyService proxyService;

	@Inject
	ErrorHandlerService errorHandlerService;

	@Inject
	ReverseProxyConverter converter;



	@GET
	@Path("{uri: .*}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public Uni<JsonObject> rerouteGet(@PathParam(value = "uri") String uri, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();
		return proxyService.handleRerouteWithCache(uri, headers,queryParameters).onFailure()
				.recoverWithUni(throwable -> errorHandlerService.handleError(throwable))
				.onItem()
				.ifNotNull().transform(proxyResponse -> JsonObject.mapFrom(converter.from(proxyResponse)));
	}


	@POST
	@Path("{uri: .*}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public Uni<JsonObject> reroutePost(@PathParam(value = "uri") String uri, byte[] body,@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();
		return proxyService.handleRerouteWithBody(uri, headers,body,HttpMethod.POST,queryParameters).onFailure()
				.recoverWithUni(throwable -> errorHandlerService.handleError(throwable))
				.onItem()
				.ifNotNull().transform(proxyResponse -> JsonObject.mapFrom(converter.from(proxyResponse)));
	}
	@PUT
	@Path("{uri: .*}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public Uni<JsonObject> reroutePut(@PathParam(value = "uri") String uri, byte[] body,@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();
		return proxyService.handleRerouteWithBody(uri, headers,body,HttpMethod.PUT,queryParameters).onFailure()
				.recoverWithUni(throwable -> errorHandlerService.handleError(throwable))
				.onItem()
				.ifNotNull().transform(proxyResponse -> JsonObject.mapFrom(converter.from(proxyResponse)));
	}
	@DELETE
	@Path("{uri: .*}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public Uni<JsonObject> rerouteDelete(@PathParam(value = "uri") String uri, byte[] body,@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();
		return proxyService.handleRerouteWithBody(uri, headers,body, HttpMethod.DELETE,queryParameters).onFailure()
				.recoverWithUni(throwable -> errorHandlerService.handleError(throwable))
				.onItem()
				.ifNotNull().transform(proxyResponse -> JsonObject.mapFrom(converter.from(proxyResponse)));
	}

}
