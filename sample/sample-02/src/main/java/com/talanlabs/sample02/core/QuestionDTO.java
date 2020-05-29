package com.talanlabs.sample02.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public class QuestionDTO {

    public static final String JSON_PROPERTY_UID = "uid";
    public static final String JSON_PROPERTY_TITLE = "title";
    public static final String JSON_PROPERTY_AUTHOR = "author";
    public static final String JSON_PROPERTY_CONTENT = "content";


    public static QuestionDTO create() {
        return  new QuestionDTO();
    }


    @JsonProperty(JSON_PROPERTY_TITLE)
    private String title;

    @JsonProperty(JSON_PROPERTY_AUTHOR)
    private String author;

    @JsonProperty(JSON_PROPERTY_CONTENT)
    private String content;

    @JsonProperty(JSON_PROPERTY_UID)
    private UUID uid;


    public QuestionDTO() {

    }

    public QuestionDTO(JsonObject json) {
        this.uid = UUID.fromString(json.getString(JSON_PROPERTY_UID));
        this.author = json.getString(JSON_PROPERTY_AUTHOR);
        this.content = json.getString(JSON_PROPERTY_CONTENT);
        this.title = json.getString(JSON_PROPERTY_TITLE);
    }

    public QuestionDTO withTitle(String title) {
        this.title = title;
        return this;
    }

    public QuestionDTO withAuthor(String author) {
        this.author = author;
        return this;
    }

    public QuestionDTO withContent(String content) {
        this.content = content;
        return this;
    }


    public QuestionDTO withUID(UUID uid) {
        this.uid = uid;
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

    public UUID getUID() { return  uid; }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
