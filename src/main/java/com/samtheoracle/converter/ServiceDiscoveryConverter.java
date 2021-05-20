package com.samtheoracle.converter;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import com.samtheoracle.dto.RecordDto;

import io.vertx.mutiny.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;

@ApplicationScoped
public class ServiceDiscoveryConverter {
	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
	public Record from(RecordDto recordDto){
		logger.info("Received record "+recordDto);
		return HttpEndpoint.createRecord(recordDto.name,recordDto.location.ssl,recordDto.location.host,recordDto.location.port,recordDto.location.root,null);
	}
}
