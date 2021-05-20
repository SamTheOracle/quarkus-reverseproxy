package com.samtheoracle.rest;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.samtheoracle.converter.ServiceDiscoveryConverter;
import com.samtheoracle.dto.RecordDto;
import com.samtheoracle.service.ServiceDiscoveryHelper;

import io.smallrye.mutiny.Uni;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;

@Path("/services")
public class ServiceRest {
    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Inject
    ServiceDiscoveryHelper serviceDiscoveryHelper;

    @Inject
    ServiceDiscoveryConverter serviceDiscoveryConverter;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> createNewServiceRecord(RecordDto recordDto){
        return serviceDiscoveryHelper.createNewRecord(serviceDiscoveryConverter.from(recordDto))
                .onItem()
                .ifNotNull()
                .transform(r->Response.noContent().build())
                .onFailure()
                .recoverWithItem(exception->{
                    logger.severe(exception.getMessage());
                    return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
                });
    }


}