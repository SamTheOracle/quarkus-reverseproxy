package com.samtheoracle;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.samtheoracle.discovery.Record;
import com.samtheoracle.discovery.ServiceDiscovery;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class ReverseProxyLifecycleObserver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    ServiceDiscovery discovery;

    @Inject
    WebClient webClient;


    void onStart(@Observes StartupEvent event) {

            discovery.createRecord(new Record()).onFailure()
                    .invoke(throwable -> {
                    }).onItemOrFailure().transformToUni((record, throwable) -> {
                        if(throwable!=null){
                            logger.error("Error in configuration of redis back-end. Could not register temp dummy record.", throwable);
                            Quarkus.asyncExit(500);
                        }
                        logger.info("Published dummy record {}. Trying to remove it.", record);
                        return discovery.removeRecord(record.getRegistration());
                    }).onFailure().invoke(throwable -> {
                        logger.error("could not delete dummy record.",throwable);
                        Quarkus.asyncExit(500);
                    }).subscribe()
                    .with(record->logger.info("Redis backend successfully setup"));
    }
}
