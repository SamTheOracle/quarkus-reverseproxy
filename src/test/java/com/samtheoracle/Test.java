package com.samtheoracle;

import java.nio.charset.StandardCharsets;

import io.vertx.mutiny.core.buffer.Buffer;

public class Test {
	public static void main(String[] args) {

		String uri = "tracks/position";
		String root = uri.split("/")[0];
		System.out.println(root);
	}
}
