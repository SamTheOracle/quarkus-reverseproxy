package com.samtheoracle.discovery;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ServiceDiscoveryHelper {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());


	@Inject
	ServiceDiscovery discovery;


	public Uni<Record> createNewRecord(Record newRecord){
		Predicate<Record> namePredicate = record -> Optional.ofNullable(record.getName()).orElse("").equals(newRecord.getName());
		return discovery.getRecord(namePredicate).onItem()
				.ifNull()
				.continueWith(newRecord)
				.onItem()
				.transformToUni(r->discovery.createRecord(r.setStatus(Status.UP)));
	}
	public Uni<Record> getRecord(String root){
		Predicate<Record> rootPredicate = record -> record.getLocation()!=null && record.getLocation().getRoot().equals(root);
		Predicate<Record> statusPredicate = record -> record.getStatus()==Status.UP;
		return  discovery.getRecord(rootPredicate.and(statusPredicate))
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
