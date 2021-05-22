package com.samtheoracle.server;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.io.File;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;

import com.samtheoracle.service.ProxyService;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ProxyRerouteTest {
//
//	@Inject
//	ProxyService proxyService;
//
//	public static DockerComposeContainer<?> environment = new DockerComposeContainer<>(
//			new File("src/test/resources/docker-compose.test.yml")).withExposedService("redis_1", 6379).withExposedService("tracks_1",
//			8902).withExposedService("mongodb_1", 27017);
//
//	@BeforeAll
//	static void setUp() {
//		environment.withPull(true).start();
//	}
//
//	@Test
//	public void test() {
//		given().when().get("/api/v1/services?root=ciao").then().statusCode(200).body(is("hello"));
//	}
}
