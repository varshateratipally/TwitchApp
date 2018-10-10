package com.possiblemobile.twitchapp.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

public class ChannelInfo implements Comparable<ChannelInfo>, Parcelable {
    @SerializedName("_id")
    final private int channelId;
    final private String name;
    final private String language;
    final private String description;
    final private String url;
    final private int followers;
    @SerializedName("profile_banner")
    final private String profileBanner;
    @SerializedName("video_banner")
    final private String videoBanner;
    final private String views;
    final String logo;
    final String game;
    @SerializedName("display_name")
    final String displayName;

    public ChannelInfo(int channelId, String name, String language, String url, int followers,
                       String profileBanner, String videoBanner, String logo, String game,
                       String displayName, String viewers, String description) {
        this.channelId = channelId;
        this.name = name;
        this.language = language;
        this.url = url;
        this.followers = followers;
        this.profileBanner = profileBanner;
        this.videoBanner = videoBanner;
        this.logo = logo;
        this.game = game;
        this.displayName = displayName;
        this.views = viewers;
        this.description = description;
    }

    public int getChannelId() {
        return channelId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getFollowers() {
        return followers;
    }

    public String getGame() {
        return game;
    }

    public String getLanguage() {
        return language;
    }

    public String getLogo() {
        return logo;
    }

    public String getName() {
        return name;
    }

    public String getProfileBanner() {
        return profileBanner;
    }

    public String getUrl() {
        return url;
    }

    public String getVideoBanner() {
        return videoBanner;
    }

    public String getViewers() {
        return views;
    }

    public String getDescription() {
        return description;
    }

    public ChannelInfo(Parcel in) {
        String[] data = new String[13];

        in.readStringArray(data);
        this.channelId = Integer.parseInt(data[0]);
        this.name = data[1];
        this.displayName = data[2];
        this.followers = Integer.parseInt(data[4]);
        this.views = data[5];
                this.logo = data[6];
                this.videoBanner = data[7];


                this.profileBanner = data[8];

            this.language = data[9];
        this.description = data[10];
        this.url = data[11];
        this.game = data[12];
    }

    public static final Parcelable.Creator<ChannelInfo> CREATOR = new ClassLoaderCreator<ChannelInfo>(){
        @Override
        public ChannelInfo createFromParcel(Parcel source) {
            return new ChannelInfo(source);
        }

        @Override
        public ChannelInfo createFromParcel(Parcel source, ClassLoader loader) {
            return new ChannelInfo(source);
        }

        @Override
        public ChannelInfo[] newArray(int size) {
            return new ChannelInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Create array with values to send with intent - I think
        String[] toSend = new String[] {
                String.valueOf(this.channelId),
                this.name,
                this.displayName,
                this.description,
                String.valueOf(this.followers),
                String.valueOf(this.views),
                null, //this.logoURL.toString(),
                null, //this.videoBannerURL.toString(),
                null, //this.profileBannerURL.toString()
                null,
                null,
                null,
                null
        };

        // Only send URLS with if they are not null
        if(this.logo != null) {
            toSend[6] = String.valueOf(this.logo);
        }

        if(this.videoBanner != null) {
            toSend[7] = String.valueOf(this.videoBanner);
        }

        if(this.profileBanner != null) {
            toSend[8] = String.valueOf(this.profileBanner);
        }

        toSend[9] = this.language;
        toSend[10] = this.description;
        toSend[11] = this.url;
        toSend[12] = this.game;

        dest.writeStringArray(toSend);
    }


    @Override
    public int compareTo(@NonNull ChannelInfo another) {
        return String.CASE_INSENSITIVE_ORDER.compare(another.getDisplayName(), getDisplayName());
    }

}
