package com.samtheoracle;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.samtheoracle.dto.ProxyResponseDto;
import com.samtheoracle.service.cache.ProxyResponse;

import io.vertx.core.json.Json;

@Provider
public class ResponseHandlerProvider implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		String baseUri = requestContext.getUriInfo().getPath();
		if(baseUri.contains("/services")){
			return;
		}
		if(responseContext.hasEntity()) {
			ProxyResponseDto proxyResponseDto = Json.decodeValue(responseContext.getEntity().toString(),ProxyResponseDto.class);
			responseContext.setStatus(proxyResponseDto.status);
		}

	}
}
