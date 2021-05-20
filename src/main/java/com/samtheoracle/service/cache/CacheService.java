package com.samtheoracle.service.cache;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Response;

@ApplicationScoped
public class CacheService {

	@Inject
	ReactiveRedisClient reactiveRedisClient;

	public Uni<String> get(String key){
		return reactiveRedisClient.get(key).onItem().ifNotNull().transform(Response::toString);
	}
	public Uni<Response> set(String key,String data, int expiration){
		return reactiveRedisClient.setex(key,String.valueOf(expiration),data);
	}
}
