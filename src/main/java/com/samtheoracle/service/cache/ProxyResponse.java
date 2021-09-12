package com.samtheoracle.service.cache;

import io.vertx.mutiny.core.buffer.Buffer;

public class ProxyResponse {
	private final Buffer data;
	private final boolean isCached;
	private final int status;
	private String cacheId;

	private ProxyResponse(Buffer data, boolean isCached, int status) {
		this.data = data;
		this.isCached = isCached;
		this.status = status;
	}
	private ProxyResponse(Buffer data, boolean isCached, int status,String cacheId) {
		this.data = data;
		this.isCached = isCached;
		this.status = status;
		this.cacheId = cacheId;
	}
	public static ProxyResponse create(Buffer data, boolean isCached, int status){
		return new ProxyResponse(data,isCached, status);
	}

	public static ProxyResponse create(Buffer data, boolean isCached, int status, String cacheId) {
		return new ProxyResponse(data,isCached, status,cacheId);
	}

	public String getCacheId() {
		return cacheId;
	}

	public int getStatus() {
		return status;
	}

	public Buffer getData() {
		return data;
	}

	public boolean isCached() {
		return isCached;
	}

	@Override
	public String toString() {
		return "ProxyResponse{" +
				"data=" + data +
				", isCached=" + isCached +
				", status=" + status +
				", cacheId='" + cacheId + '\'' +
				'}';
	}
}
