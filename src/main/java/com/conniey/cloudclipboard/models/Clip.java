package com.conniey.cloudclipboard.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class Clip {
    @JsonProperty
    private String id;
    @JsonProperty
    private String contents;
    @JsonProperty
    private OffsetDateTime created;

    public String getId() {
        return id;
    }

    public Clip setId(String id) {
        this.id = id;
        return this;
    }

    public String getContents() {
        return contents;
    }

    public Clip setContents(String contents) {
        this.contents = contents;
        return this;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public Clip setCreated(OffsetDateTime created) {
        this.created = created;
        return this;
    }
}
