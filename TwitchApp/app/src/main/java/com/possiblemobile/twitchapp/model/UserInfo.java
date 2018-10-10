package com.possiblemobile.twitchapp.model;

import com.google.gson.annotations.SerializedName;

final public class UserInfo {

    @SerializedName("_id")
    final private String id;
    @SerializedName("name")
    final private String name;
    @SerializedName("display_name")
    final private String displayName;

    public UserInfo(String id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
