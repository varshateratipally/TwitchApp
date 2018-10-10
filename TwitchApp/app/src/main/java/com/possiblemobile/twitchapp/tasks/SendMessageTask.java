package com.possiblemobile.twitchapp.tasks;

import android.os.AsyncTask;

import com.possiblemobile.twitchapp.views.ChatManager;

public class SendMessageTask extends AsyncTask<Void, Void, Void> {
    private ChatManager mBot;
    private String message;

    public SendMessageTask(ChatManager mBot, String message) {
        this.mBot = mBot;
        this.message = message;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (mBot != null && message != null) {
            mBot.sendMessage(message);
        }
        return null;
    }
}
