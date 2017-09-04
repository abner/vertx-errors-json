package io.abner.vertx.errors.starter;

import com.savoirtech.logging.slf4j.json.LoggerFactory;
import io.abner.vertx.errors.handlers.DocumentRouteHandler;
import io.abner.vertx.errors.services.DocumentBusService;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;

import com.savoirtech.logging.slf4j.json.logger.Logger;

public class MainVerticle extends AbstractVerticle {

	public final static Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
	DocumentBusService busService;

	@Override
	public void start() {
		LOGGER.info().message("Main Message").log();
		LOGGER.error().message("False error").exception("cause", (Exception) new RuntimeException("False error")).log();

		// vertx.sharedData().getClusterWideMapObservable("documentsMap").subscribe(map -> {
		// 	busService = new DocumentBusService();
		// })

		DocumentBusService.initialize(vertx);

		DocumentBusService.getInstance().rxMessageReceived().subscribe(msg -> {
			LOGGER.info().message("LOOKS GOOD: We Received the message => " + msg.body().toString()).log();
		});

		if ("true".equals(System.getenv("SENDER"))) {
			System.out.println("OPS -> I'M A SENDER AND I WILL SEND A MESSAGE!");
			DocumentBusService.getInstance().sendMessage(new JsonObject().put("id", 1).put("action", "GET"))
					.subscribe(msg -> {
						if (msg.isSend()) {
							LOGGER.info().message("MESSAGE SENT!").log();
						} else {
							LOGGER.info().message("FAILED TO SEND THE MESSAGE!").log();
						}
					});
		}

		Router router = Router.router(vertx);
		
		router.route().handler(BodyHandler.create());
		DocumentRouteHandler docHandler = new DocumentRouteHandler();
		router.get("/documents/:id").handler(docHandler);
		router.put("/documents/:id").handler(docHandler);
		router.delete("/documents/:id").handler(docHandler);
		router.get("/documents").handler(docHandler);
		router.post("/documents").handler(docHandler);
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);

		vertx.setPeriodic(20000L, v -> {
			DocumentBusService.getInstance().sendMessage(new JsonObject().put("id", 1).put("action", "periodic")).subscribe();
		});
	}

	public static void main(String[] args) {
		// Vertx vertx = Vertx.vertx(); 

		Vertx.rxClusteredVertx(null).subscribe(vertx -> {

			vertx.rxDeployVerticle("MainVerticle").subscribe(name -> {
				LOGGER.info().message("MainVerticle successfully deployed!").log();

			}, e -> {
				LOGGER.error().message("Failed to deploy MainVerticle: " + e.getMessage()).log();
			});

		});

	}
}