package com.conniey.cloudclipboard.models;

import java.util.Objects;

public class Secret {
    private final String key;
    private final String friendlyName;
    private final String value;
    private final String contentType;

    public Secret(String key, String value) {
        this(key, key, value, "");
    }

    public Secret(String key, String friendlyName, String value, String contentType) {
        this.key = Objects.requireNonNull(key, "'key' is required.");
        this.friendlyName = friendlyName;
        this.value = Objects.requireNonNull(value, "'value' is required.");
        this.contentType = contentType != null ? contentType : "n/a";
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
