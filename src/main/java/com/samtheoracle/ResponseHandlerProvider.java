package com.samtheoracle;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.samtheoracle.dto.ProxyResponseDto;
import com.samtheoracle.service.cache.ProxyResponse;

import io.vertx.core.json.Json;

@Provider
public class ResponseHandlerProvider implements ContainerResponseFilter {
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		if(responseContext.hasEntity()) {
			ProxyResponseDto proxyResponseDto = Json.decodeValue(responseContext.getEntity().toString(),ProxyResponseDto.class);
			responseContext.setStatus(proxyResponseDto.status);

		}

	}
}