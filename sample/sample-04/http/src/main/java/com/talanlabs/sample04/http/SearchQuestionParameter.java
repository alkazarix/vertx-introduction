package com.talanlabs.sample04.http;

public enum SearchQuestionParameter {

    LIMIT("limit"),
    PAGE("page");

    private String name;

    SearchQuestionParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
