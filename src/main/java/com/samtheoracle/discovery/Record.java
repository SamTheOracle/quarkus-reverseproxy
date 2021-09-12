package com.samtheoracle.discovery;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.JsonObject;

import java.util.Iterator;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class Record {

    private Location location;
    private String name;
    private Status status;
    private String registration;
    private String type;

    public Record() {
        this.status = Status.UNKNOWN;
    }


    public Location getLocation() {
        return location;
    }

    public Record setLocation(Location location) {
        this.location = location;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public Record setName(String name) {
        this.name = name;
        return this;
    }

    public Status getStatus() {
        return this.status;
    }

    public Record setStatus(Status status) {
        Objects.requireNonNull(status);
        this.status = status;
        return this;
    }

    public Record setRegistration(String reg) {
        this.registration = reg;
        return this;
    }

    public String getRegistration() {
        return this.registration;
    }

    public JsonObject toJson(){
        return JsonObject.mapFrom(this);
    }

    @Override
    public String toString() {
        return "Record{" +
                "location=" + location +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", registration='" + registration + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
