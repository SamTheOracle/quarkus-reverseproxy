package com.samtheoracle.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samtheoracle.config.ProxyConfigurator;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;
import io.vertx.servicediscovery.Record;

@ApplicationScoped
public class HealthCheckService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@ConfigProperty(name = "proxy.healthcheck.timeout")
	Integer timeout;

	@ConfigProperty(name = "proxy.healthcheck.enable")
	Boolean shouldDoHealthCheck;

	@Inject
	ServiceDiscoveryHelper helper;

	@Inject
	ProxyConfigurator proxyConfigurator;

	WebClient webClient;

	void start(@Observes StartupEvent startupEvent) {
		logger.info("health check started");
		webClient = proxyConfigurator.getWebClient();
	}

	@Scheduled(cron = "{proxy.healthcheck.heartbeat}")
	void checkServices() {

		if (shouldDoHealthCheck)
			helper.getRecords().subscribe().with(
					records -> records.stream().filter(record -> record.getLocation() != null).forEach(this::pingRecord));
	}

	private void pingRecord(Record record) {
		String host = record.getLocation().getString("host");
		Integer port = record.getLocation().getInteger("port");
		webClient.get(port, host, "/ping").expect(ResponsePredicate.SC_OK).timeout(timeout * 1000L).send().subscribe().with(
				bufferHttpResponse -> {
					logger.trace("Received response: {}", bufferHttpResponse.bodyAsBuffer().toString());
				}, throwable -> {
					logger.debug("Removing record {} after fail ping", record.toJson().encode());
					helper.removeRecord(record.getRegistration()).subscribe().with(unused -> logger.trace("Completed removal"));
				});
	}

}
