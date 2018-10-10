package com.possiblemobile.twitchapp.views;

import android.graphics.Bitmap;

import java.util.List;

public class ChatMessage {
    private String message;
    private String name;
    private String color = "";
    private boolean mod;
    private boolean turbo;
    private boolean subscriber;
    private List<ChatEmoji> emojies;
    private Bitmap subscriberIcon;
    private boolean highlight;

    public ChatMessage(String message, String name, String color, boolean mod, boolean turbo, boolean subscriber, List<ChatEmoji> emojies, Bitmap subscriberIcon, boolean highlight) {
        this.message = message;
        this.name = name;
        this.color = "#000000";
        this.mod = mod;
        this.turbo = turbo;
        this.subscriber = subscriber;
        this.emojies = emojies;
        this.subscriberIcon = subscriberIcon;
        this.highlight = highlight;
    }

    public Bitmap getSubscriberIcon() {
        return subscriberIcon;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public boolean isMod() {
        return mod;
    }

    public boolean isTurbo() {
        return turbo;
    }

    public boolean isSubscriber() {
        return subscriber;
    }

    public List<ChatEmoji> getEmojies() {
        return emojies;
    }

    public boolean isHighlight() {
        return highlight;
    }

}
