package io.abner.vertx.errors.starter;

import com.savoirtech.logging.slf4j.json.LoggerFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import com.savoirtech.logging.slf4j.json.logger.Logger;

public class MainVerticle extends AbstractVerticle {

	public final static Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

	@Override
	public void start() {
		LOGGER.info().message("Main Message").log();
		LOGGER.error()
		.message("False error")
		.exception("cause", (Exception) new RuntimeException("False error")).log();
		vertx.createHttpServer().requestHandler(req -> {
			LOGGER.info().message("Request called!!!!").log();
			req.response().end("Hello Vert.x!");
		}).listen(8080);
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MainVerticle(), deployResult -> {
			if (deployResult.succeeded()) {
				LOGGER.info().message("MainVerticle successfully deployed!").log();
			} else {
				LOGGER.error().message("Failed to deploy MainVerticle: " + deployResult.cause().getMessage())
						.exception("cause", (Exception) deployResult.cause()).log();
			}
		});
	}

}