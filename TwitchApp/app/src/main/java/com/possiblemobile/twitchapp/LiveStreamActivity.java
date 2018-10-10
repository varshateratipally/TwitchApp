package com.possiblemobile.twitchapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.possiblemobile.twitchapp.model.ChannelInfo;
import com.possiblemobile.twitchapp.model.StreamItem;
import com.possiblemobile.twitchapp.views.ChatFragment;

public class LiveStreamActivity extends StreamVideoActivity {

    public static Intent createLiveStreamIntent(StreamItem stream, boolean sharedTransition, Context context) {
        Intent liveStreamIntent = new Intent(context, LiveStreamActivity.class);
        liveStreamIntent.putExtra(context.getString(R.string.intent_key_streamer_info), stream.getChannel());
        liveStreamIntent.putExtra(context.getString(R.string.intent_key_stream_viewers), stream.getViewers());
        liveStreamIntent.putExtra(context.getString(R.string.stream_preview_url), stream.getPreview().getMedium());
        liveStreamIntent.putExtra(context.getString(R.string.stream_shared_transition), sharedTransition);
        return liveStreamIntent;
    }
    private ChatFragment mChatFragment;

    @Override
    protected int getLayoutRessource() {
        return R.layout.activity_live_stream;
    }

    @Override
    protected int getVideoContainerRessource() {
        return R.id.video_fragment_container;
    }

    @Override
    protected Bundle getStreamArguments() {
        boolean autoPlay = true;

        Intent intent = getIntent();
        ChannelInfo mChannelInfo = intent.getParcelableExtra(getResources().getString(R.string.intent_key_streamer_info));
        int currentViewers = intent.getIntExtra(getResources().getString(R.string.intent_key_stream_viewers), -1);

        if (mChannelInfo == null) {
            try {

                MediaInfo mediaInfo = null;
                if (mediaInfo != null) {
                    MediaMetadata metadata = mediaInfo.getMetadata();
                    mChannelInfo = new Gson().fromJson(metadata.getString(getString(R.string.stream_fragment_streamerInfo)), new TypeToken<ChannelInfo>() {
                    }.getType());
                    autoPlay = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.stream_fragment_streamerInfo), mChannelInfo);
        args.putInt(getString(R.string.stream_fragment_viewers), currentViewers);
        args.putBoolean(getString(R.string.stream_fragment_autoplay), autoPlay);
        return args;
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        if (savedInstance == null) {
            FragmentManager fm = getSupportFragmentManager();

            if (mChatFragment == null) {
                mChatFragment = ChatFragment.getInstance(getStreamArguments());
                fm.beginTransaction().replace(R.id.chat_fragment, mChatFragment).commit();
            }
        }


    }

    @Override
    public void onStop() {
        super.onStop();
    }


}
