package io.abner.vertx.errors;

import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator;

import io.vertx.core.json.JsonObject;

public class UUIDTest {

	@Test
	public void test() {
		JsonObject t = new JsonObject();
		UUIDGenerator gen = new UUIDGenerator();
		// gen.forScope(UUIDTest.class);
		for (int i = 0; i < 10000000; i++) {
			UUID uuid = gen.generateId(t);
			//System.out.println("UID > " + uuid.toString());
		}
	}

}
