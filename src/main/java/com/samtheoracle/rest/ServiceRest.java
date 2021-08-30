package com.samtheoracle.rest;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.samtheoracle.converter.ServiceDiscoveryConverter;
import com.samtheoracle.dto.RecordDto;
import com.samtheoracle.discovery.ServiceDiscoveryHelper;

import io.smallrye.mutiny.Uni;

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
    @GET
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN})
    public Uni<Response> getService(@QueryParam("root") String root){
        return serviceDiscoveryHelper.getRecord(root)
                .onItem()
                .ifNotNull()
                .transform(record -> Response.ok(serviceDiscoveryConverter.to(record)).build())
                .onFailure()
                .recoverWithItem(throwable -> Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(throwable.getMessage()).build());
    }

    @GET
    @Path("all")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN})
    public Uni<Response> getServices(){
        return serviceDiscoveryHelper.getRecords()
                .onItem()
                .ifNotNull()
                .transform(records -> Response.ok(serviceDiscoveryConverter.to(records)).build())
                .onFailure()
                .recoverWithItem(throwable -> Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(throwable.getMessage()).build());
    }



}