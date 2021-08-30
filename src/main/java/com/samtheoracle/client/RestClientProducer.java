package com.samtheoracle.client;

import io.quarkus.runtime.Startup;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class RestClientProducer {
    @Inject
    Vertx vertx;

    private static WebClient webClient;

    @Produces
    public WebClient webClient() {
        if (webClient == null) {
            webClient = WebClient.create(Objects.requireNonNull(vertx), new WebClientOptions().setKeepAlive(false));
        }
        return webClient;
    }

}
