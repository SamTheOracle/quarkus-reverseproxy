package com.samtheoracle.converter;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samtheoracle.dto.ProxyResponseDto;
import com.samtheoracle.service.cache.ProxyResponse;

@ApplicationScoped
public class ReverseProxyConverter {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	public ProxyResponseDto from(ProxyResponse proxyResponse){
		ProxyResponseDto proxyResponseDto = new ProxyResponseDto();
		proxyResponseDto.isCached = proxyResponse.isCached();
		proxyResponseDto.data = proxyResponse.getData();
		try{
			proxyResponseDto.data = proxyResponse.getData().toJson();

		}catch (Exception e){
			proxyResponseDto.data = proxyResponse.getData().toString();
		}
		return proxyResponseDto;
	}
}
