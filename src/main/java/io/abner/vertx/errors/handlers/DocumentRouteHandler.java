package io.abner.vertx.errors.handlers;

import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.Logger;

import io.abner.vertx.errors.services.DocumentBusService;
import io.abner.vertx.errors.services.DocumentRepositoryService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.ext.web.RoutingContext;

public class DocumentRouteHandler implements Handler<RoutingContext> {

    public final static Logger LOGGER = LoggerFactory.getLogger(DocumentRouteHandler.class);

    private DocumentRepositoryService documentService = new DocumentRepositoryService();

    @Override
    public void handle(RoutingContext context) {
        switch (context.request().method()) {
        case GET:
            processGet(context);
            break;
        case PUT:
            processPut(context);
            break;
        case POST:
            processPost(context);
            break;
        case DELETE:
            processDelete(context);
            break;
        default:
            respondNotFound(context);
        }
    }

    private void processDelete(RoutingContext context) {
        try {
            JsonObject document =  context.getBodyAsJson();
            documentService.delete(document);
            context.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
            DocumentBusService.getInstance().sendMessage(
                new JsonObject()
                .put("action", "delete")
                .put("key", "document-" + document.getInteger("id").toString())
                .put("object", document)
            );
        } catch (RuntimeException e) {
            context.fail(400);
        }
	}

	private void respondNotFound(RoutingContext context) {
        context.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
    }

    private void processPost(RoutingContext context) {
        try {
            JsonObject document =  context.getBodyAsJson();
            documentService.add(document).subscribe(Void -> {
                context.response().setStatusCode(HttpResponseStatus.CREATED.code()).end();
                DocumentBusService.getInstance().sendMessage(
                    new JsonObject()
                    .put("action", "post")
                    .put("key", "document-" + document.getInteger("id").toString())
                    .put("object", document)
                    );
            });
        } catch (RuntimeException e) {
            context.fail(400);
        }
    }

    private void processPut(RoutingContext context) {
        try {
            JsonObject document =  context.getBodyAsJson();
            LOGGER.info().message("ALGUMA MENSAGEM").log();
            LOGGER.info().message("PUT document: " + (document == null ? "NULL" : document.encode())).log();
            documentService.put(document).subscribe((ok) -> {
                if (!ok) {
                    context.fail(HttpResponseStatus.NOT_FOUND.code());
                    return;
                }
                context.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
                DocumentBusService.getInstance().sendMessage(
                    new JsonObject()
                    .put("action", "put")
                    .put("key", "document-" + document.getInteger("id").toString())
                    .put("object", document)
                    ).subscribe();
                });
        } catch (RuntimeException e) {
            e.printStackTrace();
            context.fail(400);
        }
    }

    private void processGet(RoutingContext context) {
        String id = context.request().getParam("id");
        if (id == null) {
            documentService.getAll().subscribe(documents -> {
                context.response().end(documents.encode());
            });
        } else {
            Integer idAsInteger = null;
            try {
                idAsInteger = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                context.fail(400);
                return;
            }
            documentService.getById(idAsInteger).subscribe(document -> {
                if (document == null) {
                    context.fail(404);
                    return;
                } else {
                    context.response().end(document.encode());
                }
            });
        }
    }

}