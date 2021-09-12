package com.samtheoracle.service;

import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import com.samtheoracle.service.cache.ProxyResponse;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ErrorHandlerService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	public Uni<ProxyResponse> handleError(Throwable throwable){
		logger.error("Error during proxy request.",throwable);
		String message = throwable.getMessage();
		ProxyResponse proxyResponse = ProxyResponse.create(Buffer.buffer(message!=null?message.getBytes(StandardCharsets.UTF_8):"Error".getBytes(StandardCharsets.UTF_8) ),
				false, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		return Uni.createFrom().item(proxyResponse);
	}
}
