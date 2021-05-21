package com.samtheoracle;

import java.nio.charset.StandardCharsets;

import io.vertx.mutiny.core.buffer.Buffer;

public class Test {
	public static void main(String[] args) {

		String uri = "tracks/position";
		String root = uri.split("/")[0];
		System.out.println(root);
		String test = "[{\"latitude\":\"skljglfk\",\"longitude\":\"bel veicolo\",\"vehicleId\":\"213\"},{\"latitude\":\"skljglfk\",\"longitude\":\"bel veicolo\",\"vehicleId\":\"213\"},{\"latitude\":\"skljglfk\",\"longitude\":\"bel veicolo\",\"vehicleId\":\"213\"},{\"latitude\":\"skljglfk\",\"longitude\":\"bel veicolo\",\"vehicleId\":\"213\"},{\"latitude\":\"skljglfk\",\"longitude\":\"bel veicolo\",\"vehicleId\":\"213\"}]";
		Object object = Buffer.buffer(test.getBytes(StandardCharsets.UTF_8)).toJson();
		System.out.println(object);
	}
}
