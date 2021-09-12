package com.samtheoracle.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyResponseDto {

	public Boolean isCached;
	public Object data;
	public int status;
	public String cacheId;

	@Override
	public String toString() {
		return "ProxyResponseDto{" +
				"isCached=" + isCached +
				", data=" + data +
				", status=" + status +
				", cacheId='" + cacheId + '\'' +
				'}';
	}
}
