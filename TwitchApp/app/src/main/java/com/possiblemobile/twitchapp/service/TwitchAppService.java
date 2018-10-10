package com.possiblemobile.twitchapp.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.possiblemobile.twitchapp.model.StreamInfo;
import com.possiblemobile.twitchapp.model.StreamItem;
import com.possiblemobile.twitchapp.model.UserInfo;

public interface TwitchAppService {

    @Headers({
            "Accept: application/vnd.twitchtv.v5+json",
            "Client-ID: vgl10ogqr6s8xqotaxc5256log6txm"
    })
    @GET("/kraken/user")
    Call<UserInfo> getUserInfo(@Query("oauth_token") String authToken);


    @GET("/kraken/search/streams")
    Call<StreamInfo> getStreams(@Query("query") String query, @Query("client_id") String clientId);

    @GET("/kraken/streams")
    Call<StreamInfo> getTopStreams(@Query("client_id") String clientId);

    @GET("/kraken/streams/{stream_id}")
    Call<StreamItem> getStreamForId(@Path("stream_id") int id,@Query("client_id") String clientId);

}
