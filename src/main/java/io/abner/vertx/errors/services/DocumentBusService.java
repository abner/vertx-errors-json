package io.abner.vertx.errors.services;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import rx.Emitter;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.Emitter.BackpressureMode;

public class DocumentBusService {

    private static DocumentBusService INSTANCE = null;
    private Observable<Message<Object>> receivedMessages = null;
    private Emitter<Message<Object>> emitter;
    private EventBus eventBus = null;
    private DocumentBusService(Vertx vertx) {
    }

    public static  DocumentBusService initialize(Vertx vertx) {
        INSTANCE = new DocumentBusService(vertx);
        return INSTANCE.prepare(vertx);
    }

    public Single<Message<Object>> sendMessage(JsonObject message) {
        if (message == null) {
            throw new RuntimeException("Message should not be null");
        }
        return this.eventBus.rxSend("documentsChannel", message);
    }

    public Observable<Message<Object>> rxMessageReceived() {
        return this.receivedMessages;
    }

    public static DocumentBusService getInstance() {
        if (INSTANCE == null) {
            throw new RuntimeException("Instance of DocumentBusService was not initialized!");
        }
        return INSTANCE;
    }

	private DocumentBusService prepare(Vertx vertx) {
        eventBus = vertx.eventBus();
        receivedMessages =  Observable.create(emitter -> {
            this.emitter = emitter;
        }, BackpressureMode.BUFFER);

        eventBus.consumer("documentsChannel", msg -> {
            msg.reply("OK");
            if (this.emitter != null) {
                this.emitter.onNext(msg);
            } else {
                System.out.println("MESSAGE RECEIVED WHEN EMITTER WASN'T READY! " + msg.body().toString() );
            }
        });
        
        return this;
	}

}

