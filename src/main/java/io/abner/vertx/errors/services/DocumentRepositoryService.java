package io.abner.vertx.errors.services;

import java.util.stream.Collectors;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import rx.Single;

public class DocumentRepositoryService {
    JsonArray documents = new JsonArray();

    public DocumentRepositoryService() {
        this.documents
            .add(new JsonObject().put("id", 1).put("description", "My Document 1"))
            .add(new JsonObject().put("id", 2).put("description", "My Document 2"));            
    }

    public DocumentRepositoryService(JsonArray documents) {
        this.documents = documents;
    }

    public Single<JsonArray> getAll() {
        return Single.just(this.documents);
    }

    public Single<JsonObject> getById(Integer id) {
        JsonArray result = new JsonArray(this.documents
            .stream()
            .filter(o -> o instanceof JsonObject)
            .map(JsonObject.class::cast)
            .filter(jsonObj -> jsonObj.getInteger("id").equals(id))
            .collect(Collectors.toList()));
        return result.size() > 0 ?  Single.just(result.getJsonObject(0)) : Single.just(null);
    }

    public Single<Void> add(JsonObject document) {
        validateDocument(document);
        if (document == null) {
            throw new RuntimeException("document can not be null!");
        }
        if (!document.containsKey("id")) {
            throw new RuntimeException("document id not defined!");            
        } 
        if (!(document.getValue("id") instanceof Integer)) {
            throw new RuntimeException("document id should be Integer!");            
        }
        this.documents.add(document);
        return Single.just(null);
    }

    public Single<Boolean> put(JsonObject document) {
        validateDocument(document);
        return getById(document.getInteger("id")).flatMap(storedDocument -> {
            System.out.println("ENCONTROU DOCUMENTO");
            if(storedDocument != null) {
                storedDocument.mergeIn(document);
                return Single.just(true);
            }
            return Single.just(false);
        });
    }

    public Single<Boolean> delete(JsonObject document) {
        validateDocument(document);
        return getById(document.getInteger("id")).flatMap(storedDocument -> {
            if(storedDocument != null) {
                documents.remove(storedDocument);
                return Single.just(true);
            }
            return Single.just(false);
        });
    }

    private void validateDocument(JsonObject document) {
        if (document == null) {
            throw new RuntimeException("document can not be null!");
        }
        if (!document.containsKey("id")) {
            throw new RuntimeException("document id not defined!");            
        } 
        if (!(document.getValue("id") instanceof Integer)) {
            throw new RuntimeException("document id should be Integer!");            
        }
    }

}