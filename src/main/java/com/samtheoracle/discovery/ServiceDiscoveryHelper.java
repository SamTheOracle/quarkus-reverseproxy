package com.samtheoracle.discovery;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import com.samtheoracle.discovery.Record;
import com.samtheoracle.discovery.ServiceDiscovery;
import com.samtheoracle.discovery.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ServiceDiscoveryHelper {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());


	@Inject
	ServiceDiscovery discovery;


	public Uni<Record> createNewRecord(Record record){
		return discovery.getRecord(r-> Optional.ofNullable(r.getName()).orElse("").equals(record.getName())).onItem()
				.ifNull()
				.continueWith(record)
				.onItem()
				.transformToUni(r->discovery.createRecord(r));
	}
	public Uni<Record> getRecord(String root){
		return  discovery.getRecord(record -> record.getLocation()!=null && record.getLocation().getRoot().equals(root))
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
		return discovery.getAllRecords();
	}

	public Uni<Record> removeRecord(String registration) {
		return discovery.removeRecord(registration);
	}

	public Uni<Record> updateRecord(Record record) {
		return discovery.updateRecord(record).onFailure().invoke(throwable -> logger.error("Could not update record",throwable));
	}
}
