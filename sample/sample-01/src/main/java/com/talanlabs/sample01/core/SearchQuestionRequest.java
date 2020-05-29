package com.talanlabs.sample01.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

public class SearchQuestionRequest {

    public static Integer PAGE_DEFAULT_VALUE = 1;
    public static Integer LIMIT_DEFAULT_VALUE = 10;

    public static final String JSON_PROPERTY_PAGE = "page";
    public static final String JSON_PROPERTY_LIMIT = "limit";

    @JsonProperty(JSON_PROPERTY_PAGE)
    private Integer page;

    @JsonProperty(JSON_PROPERTY_LIMIT)
    private Integer limit;

    public SearchQuestionRequest(JsonObject json) {
        this.page = json.getInteger(JSON_PROPERTY_PAGE, PAGE_DEFAULT_VALUE);
        this.limit = json.getInteger(JSON_PROPERTY_LIMIT, LIMIT_DEFAULT_VALUE);
    }

    public SearchQuestionRequest() {
        this.page = PAGE_DEFAULT_VALUE;
        this.limit = LIMIT_DEFAULT_VALUE;
    }

    public SearchQuestionRequest withPage(Integer page) {
        this.page = page;
        return this;
    }

    public SearchQuestionRequest withLimit(Integer limit) {
        this.limit = limit;
        return this;
    }


    public Integer getPage() {
        return this.page;
    }
    public Integer getLimit() {
        return this.limit;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
