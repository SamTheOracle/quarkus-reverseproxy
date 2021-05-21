package com.samtheoracle;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samtheoracle.config.ProxyConfigurator;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;

@ApplicationScoped
public class ReverseProxyLifecycleObserver {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());


	@Inject
	ProxyConfigurator proxyConfigurator;


	void onStart(@Observes StartupEvent event){
		ServiceDiscovery discovery = proxyConfigurator.getServiceDiscovery();
		try {
			Record record = discovery.publishAndAwait(new Record());
			logger.debug("Test record: {}",record.toJson().encodePrettily());
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
