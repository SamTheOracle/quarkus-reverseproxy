package com.samtheoracle.converter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.samtheoracle.discovery.Location;
import com.samtheoracle.discovery.Record;
import com.samtheoracle.discovery.Status;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samtheoracle.dto.RecordDto;


import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ServiceDiscoveryConverter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());


    public List<RecordDto> to(List<Record> records) {
        return records.stream().map(this::to).collect(Collectors.toUnmodifiableList());
    }

    public RecordDto to(Record record) {
        RecordDto recordDto = new RecordDto();
        recordDto.name = record.getName();
        recordDto.location = new RecordDto.LocationDto();
        recordDto.status = record.getStatus().name();
        recordDto.registration = record.getRegistration();
        Location location = record.getLocation();
        if (location != null) {
            Boolean ssl = location.getSsl();
            recordDto.location.ssl = ssl != null && ssl;
            recordDto.location.host = record.getLocation().getHost();
            recordDto.location.port = record.getLocation().getPort();
            recordDto.location.root = record.getLocation().getRoot();
            recordDto.location.endpoint = record.getLocation().getEndpoint();
        }
        return recordDto;
    }
    public Record from(JsonObject jsonObject) {
        Record record = new Record();
        record.setName(jsonObject.getString("name", ""))
                .setStatus(Status.from(jsonObject.getString("status", "")))
                .setRegistration(jsonObject.getString("registration",""));
        JsonObject locationObject = jsonObject.getJsonObject("location", new JsonObject());
        Location location = new Location();
        location.setEndpoint(locationObject.getString("endpoint"));
        location.setHost(locationObject.getString("host"));
        location.setPort(locationObject.getInteger("port"));
        location.setSsl(locationObject.getBoolean("ssl"));
        location.setRoot(locationObject.getString("root"));
        return record.setLocation(location);
    }
    public Record from(RecordDto recordDto){
        Record record = new Record();
        record.setName(recordDto.name);
        Location location = new Location();
        RecordDto.LocationDto locationDto = recordDto.location;
        if(locationDto!=null){
            location.setRoot(locationDto.root);
            location.setPort(locationDto.port);
            location.setHost(locationDto.host);
            location.setEndpoint(locationDto.endpoint);
            location.setSsl(locationDto.ssl);
        }
        record.setRegistration(recordDto.registration);
        record.setStatus(Status.from(Objects.requireNonNullElse(recordDto.status,"")));
        return record.setLocation(location);
    }
}
