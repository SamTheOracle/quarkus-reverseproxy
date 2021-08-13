package com.samtheoracle.config;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

@ApplicationScoped
public class ProxyConfigurator {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@ConfigProperty(name = "proxy.redis.host")
	String redisHost;

	@ConfigProperty(name = "proxy.redis.port")
	Integer redisPort;

	@ConfigProperty(name = "proxy.redis.key")
	String key;



	@Inject
	Vertx vertx;

	WebClient webClient;

	ServiceDiscovery serviceDiscovery;

	@PostConstruct
	void init() {
		String redisHost = "REDIS HOST: " + this.redisHost;
		String redisPort = "REDIS PORT: " + this.redisPort;
		String redisKey = "REDIS KEY: " + this.key;
		String config = String.join("\n", redisHost, redisPort, redisKey);
		logger.info("Starting reverse proxy with user configuration:\n{}",  config);
		String redisUrl = String.format("redis://%s:%s", this.redisHost, this.redisPort);
		ServiceDiscoveryOptions discoveryOptions = new ServiceDiscoveryOptions().setBackendConfiguration(
				new JsonObject().put("connectionString", redisUrl).put("key", key));


		serviceDiscovery = ServiceDiscovery.create(vertx, discoveryOptions);
		WebClientOptions webClientOptions = new WebClientOptions();
		webClientOptions.setKeepAlive(false);
		webClient = WebClient.create(vertx, webClientOptions);

	}

	public WebClient getWebClient() {
		return webClient;
	}

	public ServiceDiscovery getServiceDiscovery() {
		return serviceDiscovery;
	}

}
