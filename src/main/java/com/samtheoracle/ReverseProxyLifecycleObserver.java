package com.samtheoracle;

import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.samtheoracle.config.ProxyConfigurator;
import com.samtheoracle.service.HealthCheckService;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;

@ApplicationScoped
public class ReverseProxyLifecycleObserver {

	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());


	@Inject
	ProxyConfigurator proxyConfigurator;


	void onStart(@Observes StartupEvent event){
		ServiceDiscovery discovery = proxyConfigurator.getServiceDiscovery();
		try {
			Record record = discovery.publishAndAwait(new Record());
			logger.info("Test record: "+record.toJson().encodePrettily());
			discovery.unpublishAndAwait(record.getRegistration());
			Optional<Record> optionalRecord = Optional.ofNullable(discovery.getRecordAndAwait(r->r.getRegistration().equals(record.getRegistration())));
			if(optionalRecord.isPresent()){
				throw new Exception("Error in configuration of discovery");
			}
		}catch(Exception e){
			e.printStackTrace();
			Quarkus.asyncExit(500);
		}
	}
}
