package com.talanlabs.sample01.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

public class QuestionDTO {

    private static final String JSON_PROPERTY_ID = "id";
    private static final String JSON_PROPERTY_TITLE = "title";
    private static final String JSON_PROPERTY_AUTHOR = "author";
    private static final String JSON_PROPERTY_CONTENT = "content";

    @JsonProperty(JSON_PROPERTY_ID)
    private Integer id;

    @JsonProperty(JSON_PROPERTY_TITLE)
    private String title;

    @JsonProperty(JSON_PROPERTY_AUTHOR)
    private String author;

    @JsonProperty(JSON_PROPERTY_CONTENT)
    private String content;

    public static QuestionDTO create() {
        return  new QuestionDTO();
    }


    public QuestionDTO(JsonObject json) {
        this.id = json.getInteger(JSON_PROPERTY_ID);
        this.title = json.getString(JSON_PROPERTY_TITLE);
        this.content = json.getString(JSON_PROPERTY_CONTENT);
        this.author = json.getString(JSON_PROPERTY_AUTHOR);
    }
    public QuestionDTO() {
    }

    public QuestionDTO withId(Integer id) {
        this.id = id;
        return this;
    }

    public QuestionDTO withAuthor(String author) {
        this.author = author;
        return this;
    }

    public QuestionDTO withTitle(String title) {
        this.title = title;
        return this;
    }

    public QuestionDTO withContent(String content) {
        this.content = content;
        return this;
    }

    public Integer getId() {
        return id;
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

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
