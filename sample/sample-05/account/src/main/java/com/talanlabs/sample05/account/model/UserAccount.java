package com.talanlabs.sample05.account.model;


import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject()
public class UserAccount {

    private static final String JSON_PROPERTY_UUID = "uuid";
    private static final String JSON_PROPERTY_USERNAME = "username";
    private static final String JSON_PROPERTY_PHONE = "phone";
    private static final String JSON_PROPERTY_EMAIL = "email";

    private String uuid;
    private String username;
    private String phone;
    private String email;

    public UserAccount() {

    }

    public UserAccount(JsonObject json) {
        this.uuid = json.getString(JSON_PROPERTY_UUID);
        this.username = json.getString(JSON_PROPERTY_USERNAME);
        this.phone = json.getString(JSON_PROPERTY_PHONE);
        this.email = json.getString(JSON_PROPERTY_EMAIL);
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public UserAccount withUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public UserAccount withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserAccount withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public UserAccount withEmail(String email) {
        this.email = email;
        return this;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }


}
