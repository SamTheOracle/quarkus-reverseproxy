package com.samtheoracle.service;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import com.samtheoracle.config.ReverseProxyConfigurator;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;

@ApplicationScoped
public class ServiceDiscoveryHelper {

	@Inject
	ReverseProxyConfigurator reverseProxyConfigurator;

	ServiceDiscovery discovery;

	@PostConstruct
	void init(){
		discovery = reverseProxyConfigurator.getServiceDiscovery();
	}
	public Uni<Record> createNewRecord(Record record){
		return discovery.getRecord(r-> Optional.ofNullable(r.getName()).orElse("").equals(r.getName())).onItem()
				.ifNull()
				.continueWith(record)
				.onItem()
				.transformToUni(r->discovery.publish(r));
	}
	public Uni<Record> getRecord(String root){
		return  discovery.getRecord(record -> record.getLocation()!=null && root.equals(record.getLocation().getString("root")))
				.onItem()
				.ifNull()
				.failWith(()->new NotFoundException("Record with root "+root+" was not found"))
				.onItem()
				.ifNotNull()
				.transformToUni(record -> Uni.createFrom().item(record));
	}
}
