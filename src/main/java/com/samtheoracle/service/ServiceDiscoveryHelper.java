package com.samtheoracle.service;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samtheoracle.config.ProxyConfigurator;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;

@ApplicationScoped
public class ServiceDiscoveryHelper {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Inject
	ProxyConfigurator proxyConfigurator;

	ServiceDiscovery discovery;

	@PostConstruct
	void init(){
		discovery = proxyConfigurator.getServiceDiscovery();
	}
	public Uni<Record> createNewRecord(Record record){
		return discovery.getRecord(r-> Optional.ofNullable(r.getName()).orElse("").equals(record.getName())).onItem()
				.ifNull()
				.continueWith(record)
				.onItem()
				.transformToUni(r->discovery.publish(r));
	}
	public Uni<Record> getRecord(String root){
		return  discovery.getRecord(record -> record.getLocation()!=null && record.getLocation().getString("root").equals(root))
				.onFailure()
				.recoverWithNull()
				.onItem()
				.ifNull()
				.failWith(()->new NotFoundException("Record with root "+root+" was not found"))
				.onItem()
				.ifNotNull()
				.transformToUni(record -> Uni.createFrom().item(record));
	}

	public Uni<List<Record>> getRecords(){
		return discovery.getRecords(record -> true);
	}

	public Uni<Void> removeRecord(String registration) {
		return discovery.unpublish(registration);
	}
}
