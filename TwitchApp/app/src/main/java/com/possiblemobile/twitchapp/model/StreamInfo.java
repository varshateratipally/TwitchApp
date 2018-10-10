package com.possiblemobile.twitchapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StreamInfo {

    @SerializedName("streams")
    final private List<StreamItem> streamItemList;
    @SerializedName("_total")
    final private int total;

    @SerializedName("_links")
    final private Links links;

    public StreamInfo(List<StreamItem> streamItemList, int total, Links links) {
        this.links = links;
        this.streamItemList = streamItemList;
        this.total = total;
    }

    public Links getLinks() {
        return links;
    }

    public List<StreamItem> getStreamItemList() {
        return streamItemList;
    }

    public int getTotal() {
        return total;
    }
}
