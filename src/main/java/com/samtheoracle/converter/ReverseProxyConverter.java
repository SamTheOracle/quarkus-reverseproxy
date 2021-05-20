package com.samtheoracle.converter;

import javax.enterprise.context.ApplicationScoped;

import com.samtheoracle.dto.ProxyResponseDto;
import com.samtheoracle.service.cache.ProxyResponse;

@ApplicationScoped
public class ReverseProxyConverter {

	public ProxyResponseDto from(ProxyResponse proxyResponse){
		ProxyResponseDto proxyResponseDto = new ProxyResponseDto();
		proxyResponseDto.isCached = proxyResponse.isCached();
		try{
			proxyResponseDto.data = proxyResponse.getData().toJson();
		}catch (Exception e){
			proxyResponseDto.data = proxyResponse.getData().toString();
		}
		return proxyResponseDto;
	}
}
