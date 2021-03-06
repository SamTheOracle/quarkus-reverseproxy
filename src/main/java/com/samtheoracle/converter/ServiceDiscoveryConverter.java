package com.samtheoracle.converter;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samtheoracle.dto.RecordDto;

import io.vertx.mutiny.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ServiceDiscoveryConverter {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	public Record from(RecordDto recordDto){
		logger.debug("Received record {}",recordDto);
		return HttpEndpoint.createRecord(recordDto.name,recordDto.location.ssl,recordDto.location.host,recordDto.location.port,recordDto.location.root,null);
	}
	public List<RecordDto> to(List<Record> records){
		return records.stream().map(this::to).collect(Collectors.toUnmodifiableList());
	}
	public RecordDto to(Record record){
		RecordDto recordDto = new RecordDto();
		recordDto.name = record.getName();
		recordDto.location = new RecordDto.LocationDto();
		Boolean ssl = record.getLocation().getBoolean("ssl");
		recordDto.location.ssl = ssl != null && ssl;
		recordDto.location.host = record.getLocation().getString("host");
		recordDto.location.port = record.getLocation().getInteger("port");
		recordDto.location.root = record.getLocation().getString("root");
		recordDto.location.endpoint = record.getLocation().getString("endpoint");
		return recordDto;
	}
}
