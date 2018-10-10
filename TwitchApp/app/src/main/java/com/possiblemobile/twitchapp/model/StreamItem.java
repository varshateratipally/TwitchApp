package com.possiblemobile.twitchapp.model;

import com.google.gson.annotations.SerializedName;

public class StreamItem {

    @SerializedName("_id")
    final private String id;
    final private String game;
    final private int viewers;
    final private int delay;
    @SerializedName("stream_type")
    final private String streamType;
    @SerializedName("is_playlist")
    final private String isPlaylist;
    @SerializedName("average_fps")
    final private String averageFPS;
    @SerializedName("created_at")
    final private String createdAt;

    @SerializedName("video_height")
    final private int videoHeight;
    final private Preview preview;
    final private ChannelInfo channel;

    public StreamItem(String id, String game, int viewers, int delay, String streamType,
                      String isPlaylist, String averageFPS, String createdAt, int videoHeight,
                      Preview preview, ChannelInfo channel) {
        this.id = id;
        this.game = game;
        this.viewers = viewers;
        this.delay = delay;
        this.streamType = streamType;
        this.isPlaylist = isPlaylist;
        this.averageFPS = averageFPS;
        this.createdAt = createdAt;
        this.videoHeight = videoHeight;
        this.preview = preview;
        this.channel = channel;
    }

    public Preview getPreview() {
        return preview;
    }

    public String getGame() {
        return game;
    }

    public String getStreamType() {
        return streamType;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public String isPlaylist() {
        return isPlaylist;
    }

    public ChannelInfo getChannel() {
        return channel;
    }

    public String getAverageFPS() {
        return averageFPS;
    }

    public int getDelay() {
        return delay;
    }

    public String getId() {
        return id;
    }

    public int getViewers() {
        return viewers;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
