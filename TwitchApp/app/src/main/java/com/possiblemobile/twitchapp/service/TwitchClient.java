package com.possiblemobile.twitchapp.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TwitchClient {

    public static TwitchAppService getTwitchClient() {

        Retrofit.Builder builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.twitch.tv");

        return builder.build().create(TwitchAppService.class);
    }

}
