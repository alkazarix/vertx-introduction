package com.talanlabs.sample05.forum.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class CreateQuestionCommand {

    public static final String JSON_PROPERTY_TITLE = "title";
    public static final String JSON_PROPERTY_AUTHOR = "author";
    public static final String JSON_PROPERTY_CONTENT = "content";

    @JsonProperty(JSON_PROPERTY_TITLE)
    private String title;

    @JsonProperty(JSON_PROPERTY_AUTHOR)
    private String author;

    @JsonProperty(JSON_PROPERTY_CONTENT)
    private String content;


    public CreateQuestionCommand() {

    }

    public CreateQuestionCommand(JsonObject json) {
        this.author = json.getString(JSON_PROPERTY_AUTHOR);
        this.content = json.getString(JSON_PROPERTY_CONTENT);
        this.title = json.getString(JSON_PROPERTY_TITLE);
    }

    public CreateQuestionCommand withTitle(String title) {
        this.title = title;
        return this;
    }

    public CreateQuestionCommand withAuthor(String author) {
        this.author = author;
        return this;
    }

    public CreateQuestionCommand withContent(String content) {
        this.content = content;
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

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
