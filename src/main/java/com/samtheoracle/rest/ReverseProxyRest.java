package com.samtheoracle.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.samtheoracle.converter.ReverseProxyConverter;
import com.samtheoracle.dto.ProxyResponseDto;
import com.samtheoracle.service.ErrorHandlerService;
import com.samtheoracle.service.ProxyService;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;

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
	public Uni<JsonObject> rerouteGet(@PathParam(value = "uri") String uri,
			@Context HttpHeaders httpHeaders) {

		MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();
		return proxyService.handleGetReroute(uri, headers).onFailure()
				.recoverWithUni(throwable -> errorHandlerService.handleError(throwable))
				.onItem()
				.ifNotNull().transform(proxyResponse -> JsonObject.mapFrom(converter.from(proxyResponse)));
	}


	@POST
	@Path("{root: .*}/{uri: .+}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public Uni<Response> reroutePost(@PathParam(value = "root") String serviceRoot, @PathParam(value = "uri") String uri, byte[] body) {
		return Uni.createFrom().item(Response.ok(Buffer.buffer(body).toString()).build());
	}

}
