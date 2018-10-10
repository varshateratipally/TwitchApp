package com.possiblemobile.twitchapp.tasks;

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.possiblemobile.twitchapp.LoginActivity;
import com.possiblemobile.twitchapp.model.UserInfo;
import com.possiblemobile.twitchapp.service.TwitchClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import settings.AppPrefs;

public class HandlerUserLoginTask extends AsyncTask<Object, Void, UserInfo> {

    private Context mContext;
    private String token;
    private LoginActivity mLoginActivity;
    private AppPrefs appPrefs = new AppPrefs();

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected UserInfo doInBackground(Object... params) {
        mContext = (Context) params[0];
        token = (String) params[1];
        mLoginActivity = (LoginActivity) params[2];
        try {

             TwitchClient.getTwitchClient().getUserInfo(token).enqueue(new Callback<UserInfo>() {
                @Override
                public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                    if (response.isSuccessful()) {
                        appPrefs.setUserInfo(response.body());
                        mLoginActivity.handleLoginSuccess();
                    }
                }

                @Override
                public void onFailure(Call<UserInfo> call, Throwable t) {
                    mLoginActivity.handleLoginFailure();
                }
            });

        } catch (Exception e) {
            Log.e("Error Logging Task", e.getLocalizedMessage());
        }
        return appPrefs.getUserInfo();
    }

}
