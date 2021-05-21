package com.samtheoracle.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.samtheoracle.config.ProxyConfigurator;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.UniSubscriber;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.servicediscovery.Record;

@ApplicationScoped
public class HealthCheckService{
	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	@ConfigProperty(name = "proxy.healthcheck.timeout")
	Integer timeout;

	@Inject
	ServiceDiscoveryHelper helper;

	@Inject
	ProxyConfigurator proxyConfigurator;

	WebClient webClient;

	@PostConstruct
	void init(){
		webClient = proxyConfigurator.getWebClient();
	}

	void start(@Observes StartupEvent startupEvent){
		logger.info("health check started");
	}

	@Scheduled(cron = "{proxy.healthcheck.heartbeat}")
	void checkServices(){
		logger.info("periodic health check");
		List<Record> records = helper.getRecords().await().atMost(Duration.ofSeconds(2));
		records.stream().filter(record -> record.getLocation()!=null).forEach(record->{
			String host = record.getLocation().getString("host");
			Integer port = record.getLocation().getInteger("port");
			webClient.get(port,host,"/ping").timeout(timeout)
					.send()
					.onFailure()
					.recoverWithNull()
					.onItem()
					.transformToUni(unused->helper.removeRecord(record.getRegistration()))
					.subscribe()
					.with(unused-> logger.info("Removed record "+record.toJson().encodePrettily()), Throwable::printStackTrace);
		});
	}

}
