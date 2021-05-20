package com.samtheoracle.service;

import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import com.samtheoracle.service.cache.ProxyResponse;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;

@ApplicationScoped
public class ErrorHandlerService {

	public Uni<ProxyResponse> handleError(Throwable throwable){
		if(throwable instanceof NotFoundException){
			ProxyResponse proxyResponse = ProxyResponse.create(Buffer.buffer(throwable.getMessage().getBytes(StandardCharsets.UTF_8) ),
					false, Response.Status.NOT_FOUND.getStatusCode());
			return Uni.createFrom().item(proxyResponse);
		}
		ProxyResponse proxyResponse = ProxyResponse.create(Buffer.buffer(throwable.getMessage().getBytes(StandardCharsets.UTF_8) ),
				false, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		throwable.printStackTrace();
		return Uni.createFrom().item(proxyResponse);
	}
}
