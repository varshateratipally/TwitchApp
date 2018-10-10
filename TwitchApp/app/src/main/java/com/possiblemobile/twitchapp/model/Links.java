package com.possiblemobile.twitchapp.model;

public class Links {
    final private String self;
    final private String next;

    Links(String self, String next) {
        this.self = self;
        this.next = next;
    }

    public String getNext() {
        return next;
    }

    public String getSelf() {
        return self;
    }
}
