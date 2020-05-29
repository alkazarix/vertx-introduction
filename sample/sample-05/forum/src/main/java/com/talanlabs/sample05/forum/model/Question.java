package com.talanlabs.sample05.forum.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class Question {

    public static final String JSON_PROPERTY_UID = "uid";
    public static final String JSON_PROPERTY_TITLE = "title";
    public static final String JSON_PROPERTY_AUTHOR = "author";
    public static final String JSON_PROPERTY_CONTENT = "content";


    public static Question create() {
        return  new Question();
    }


    @JsonProperty(JSON_PROPERTY_TITLE)
    private String title;

    @JsonProperty(JSON_PROPERTY_AUTHOR)
    private String author;

    @JsonProperty(JSON_PROPERTY_CONTENT)
    private String content;

    @JsonProperty(JSON_PROPERTY_UID)
    private String uuid;


    public Question() {

    }

    public Question(JsonObject json) {
        this.uuid = json.getString(JSON_PROPERTY_UID);
        this.author = json.getString(JSON_PROPERTY_AUTHOR);
        this.content = json.getString(JSON_PROPERTY_CONTENT);
        this.title = json.getString(JSON_PROPERTY_TITLE);
    }

    public Question withTitle(String title) {
        this.title = title;
        return this;
    }

    public Question withAuthor(String author) {
        this.author = author;
        return this;
    }

    public Question withContent(String content) {
        this.content = content;
        return this;
    }


    public Question withUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public String getUUID() { return  uuid; }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}

