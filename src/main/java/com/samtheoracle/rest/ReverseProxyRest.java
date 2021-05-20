package com.samtheoracle.rest;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.samtheoracle.converter.ReverseProxyConverter;
import com.samtheoracle.service.ErrorHandlerService;
import com.samtheoracle.service.ReverseProxyService;


import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;

@Path("/")
public class ReverseProxyRest {

	@Inject
	ReverseProxyService reverseProxyService;

	@Inject
	ErrorHandlerService errorHandlerService;

	@Inject
	ReverseProxyConverter converter;

	@GET
	@Path("{uri: .*}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public Uni<Response> rerouteGet(@PathParam(value = "uri") String uri,
			@Context HttpHeaders httpHeaders) {

		MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();
		return reverseProxyService.handleGetReroute(uri, headers).onFailure()
				.recoverWithUni(throwable -> errorHandlerService.handleError(throwable))
				.onItem()
				.ifNotNull().transform(proxyResponse -> Response.status(proxyResponse.getStatus()).entity(converter.from(proxyResponse)).build());
	}

	@POST
	@Path("{root: .*}/{uri: .+}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public Uni<Response> reroutePost(@PathParam(value = "root") String serviceRoot, @PathParam(value = "uri") String uri, byte[] body) {
		return Uni.createFrom().item(Response.ok(Buffer.buffer(body).toString()).build());
	}

}
