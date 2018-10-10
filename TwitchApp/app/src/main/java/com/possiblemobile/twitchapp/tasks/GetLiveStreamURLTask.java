package com.possiblemobile.twitchapp.tasks;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import settings.AppPrefs;

public class GetLiveStreamURLTask extends AsyncTask<String, Void, String> {

    private String LOG_TAG = getClass().getSimpleName();
    private AsyncResponse callback;

    public interface AsyncResponse {
        void finished(String url);
    }

    public GetLiveStreamURLTask(AsyncResponse aCallback) {
        callback = aCallback;
    }

    @Override
    protected String doInBackground(String... params) {
        String streamerName = params[0];
        String sig = "";
        String tokenString = "";

        String resultString = AppPrefs.urlToJSONString("https://api.twitch.tv/api/channels/" + streamerName + "/access_token");
        try {
            JSONObject resultJSON = new JSONObject(resultString);
            tokenString = resultJSON.getString("token").replaceAll("\\\\", ""); // Remove all backslashes from the returned string. We need the string to make a jsonobject
            sig = resultJSON.getString("sig");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String streamUrl = String.format("http://usher.twitch.tv/api/channel/hls/%s.m3u8" +
                "?player=twitchweb&" +
                "&token=%s" +
                "&sig=%s" +
                "&allow_audio_only=true" +
                "&allow_source=true" +
                "&type=any" +
                "&p=%s", streamerName, tokenString, sig, "" + new Random().nextInt(6));


        return streamUrl;
    }

    @Override
    protected void onPostExecute(String result) {
        callback.finished(result);
    }

}
