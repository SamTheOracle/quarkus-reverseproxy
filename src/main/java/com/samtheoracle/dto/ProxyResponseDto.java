package com.samtheoracle.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ProxyResponseDto {

	public Boolean isCached;
	public Object data;
	public int status;
}
