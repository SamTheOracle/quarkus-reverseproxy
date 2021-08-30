package com.samtheoracle.discovery;

import com.samtheoracle.converter.ServiceDiscoveryConverter;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class ServiceDiscovery {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @ConfigProperty(name = "proxy.redis.key")
    String key;

    @Inject
    ReactiveRedisClient redisClient;

    @Inject
    ServiceDiscoveryConverter recordConverter;

    public Uni<Record> createRecord(Record record) {

        String uuid = record.getRegistration()==null?UUID.randomUUID().toString():record.getRegistration();
        record.setRegistration(uuid);
        record.setStatus(Status.UP);
        logger.debug("creating new record {}", record);
        return redisClient.hset(Arrays.asList(key, uuid, record.toJson().encode()))
                .onItem().transform(response -> record);
    }

    public Uni<Record> getRecord(String registration) {
        return redisClient.hget(this.key, Objects.requireNonNull(registration)).onItem()
                .transform(response -> recordConverter.from(response.toBuffer().toJsonObject()));
    }

    public Uni<Record> removeRecord(String registration) {
        return getRecord(registration)
                .onItem()
                .transformToUni(record -> redisClient.hdel(Arrays.asList(this.key, registration)).onItem().transform(response -> record));
    }

    public Uni<Record> getRecord(Predicate<Record> filter) {
        return redisClient.hgetall(this.key)
                .onItem()
                .transform(response -> response.getKeys()
                        .stream()
                        .map(key -> recordConverter.from(response.get(key).toBuffer().toJsonObject()))
                        .filter(filter)
                        .findAny()
                        .orElse(null));

    }

    public Uni<List<Record>> getAllRecords() {
        return redisClient.hgetall(this.key).onItem()
                .transformToUni(response -> Uni.createFrom().item(response.getKeys()
                        .stream()
                        .map(key -> recordConverter.from(response.get(key).toBuffer().toJsonObject()))
                        .collect(Collectors.toUnmodifiableList())));
    }

    public Uni<Record> updateRecord(Record record) {
        return redisClient.hset(Arrays.asList(this.key,record.getRegistration(),record.toJson().encode()))
                .onItem().transform(response -> record);
    }
}
