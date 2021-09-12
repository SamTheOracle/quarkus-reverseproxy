package com.samtheoracle.converter;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samtheoracle.dto.ProxyResponseDto;
import com.samtheoracle.service.cache.ProxyResponse;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;

@ApplicationScoped
public class ReverseProxyConverter {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	public ProxyResponseDto from(ProxyResponse proxyResponse) {
		ProxyResponseDto proxyResponseDto = new ProxyResponseDto();
		proxyResponseDto.isCached = proxyResponse.isCached();
		Buffer data = proxyResponse.getData();
		proxyResponseDto.data = convertData(data);
		proxyResponseDto.status = proxyResponse.getStatus();
		proxyResponseDto.cacheId=proxyResponse.getCacheId();
		return proxyResponseDto;
	}

	private static Object convertData(Buffer data) {
		if (data == null) {
			return new JsonObject();
		}
		Object dataToReturn;
		try {
			dataToReturn = data.toJson();
		} catch (Exception e) {
			dataToReturn = data.toString();
		}
		return dataToReturn;

	}
}
