package io.abner.vertx.errors.services;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.shareddata.AsyncMap;
import rx.Single;

public class DocumentSharedDataService {

    AsyncMap<String, String> docsMap = null;

    public DocumentSharedDataService(AsyncMap<String, String> map) {
        docsMap = map;
    }

    public Single<String> initialize() {
        return docsMap.rxPutIfAbsent("allDocuments", new JsonArray().encode());
    }

    public Single<JsonArray> getDocuments() {
        return this.docsMap.rxGet("allDocuments").map(s -> new JsonArray(s));
    }

    public Single<Void> addDocument(JsonObject doc) {
        return this.getDocuments().flatMap(docs -> {
            docs.add(doc);
            return this.docsMap.rxPut("allDocuments", docs.encode());
        });
    }

    public Single<JsonObject> getDocumentById(Integer id) {
        return this.getDocuments().flatMap(arr -> {
            return new DocumentRepositoryService(arr).getById(id);
        });
    }

    public Single<Boolean> updateDocument(JsonObject object) {
        return this.getDocuments().flatMap(arr -> {
            return new DocumentRepositoryService(arr).put(object);
        });
    }
}
