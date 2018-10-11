package com.possiblemobile.twitchapp.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import settings.AppPrefs;

public class ChatManager extends AsyncTask<Void, ChatManager.ProgressUpdate, Void> {
    private final String LOG_TAG = getClass().getSimpleName();

    private Pattern roomstatePattern = Pattern.compile("@broadcaster-lang=(.*);r9k=(0|1);slow=(0|\\d+);subs-only=(0|1)"),
            userStatePattern = Pattern.compile("color=(#?\\w*);display-name=(.+);emote-sets=(.+);mod=(0|1);subscriber=(0|1);(turbo=(0|1)|user)"),
            stdVarPattern = Pattern.compile("color=(#?\\w*);display-name=(\\w+).*;mod=(0|1);room-id=\\d+;.*subscriber=(0|1);.*turbo=(0|1);.* PRIVMSG #\\S* :(.*)"),
            noticePattern = Pattern.compile("@msg-id=(\\w*)");

    private String twitchChatServer = "irc.twitch.tv";
    private int twitchChatPort = 6667;
    private Bitmap subscriberIcon;

    private BufferedWriter writer;
    private BufferedReader reader;

    private Handler callbackHandler;
    private boolean isStopping;
    private String user;
    private String oauth_key;
    private String channelName;
    private String hashChannel;
    private int channelUserId;
    private ChatCallback callback;
    private Context context;
    private AppPrefs appSettings;

    private String userDisplayName;
    private String userColor;
    private boolean userIsMod;
    private boolean userIsSubscriber;
    private boolean userIsTurbo;

    private boolean chatIsR9kmode;
    private boolean chatIsSlowmode;
    private boolean chatIsSubsonlymode;

    private EmojiManager mEmoteManager;

    public ChatManager(Context aContext, String aChannel, int aChannelUserId, ChatCallback aCallback){
        mEmoteManager = new EmojiManager(aChannelUserId, aChannel, aContext);
        appSettings = new AppPrefs();
        user = appSettings.getTwitchName();
        oauth_key = "oauth:" + appSettings.getTwitchAccessToken();
        hashChannel = "#" + aChannel;
        channelName = aChannel;
        channelUserId = aChannelUserId;
        callback = aCallback;
        context = aContext;

        executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    public interface ChatCallback {
        void onMessage(ChatMessage message);
        void onConnecting();
        void onReconnecting();
        void onConnected();
        void onConnectionFailed();
        void onRoomstateChange(boolean isR9K, boolean isSlow, boolean isSubsOnly);
        void onBttvEmoteIdFetched(List<Emoji> bttvChannel, List<Emoji> bttvGlobal);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callbackHandler = new Handler();
    }

    @Override
    protected Void doInBackground(Void... params) {
        subscriberIcon = mEmoteManager.getSubscriberEmote();
        mEmoteManager.loadBttvEmotes(new EmojiManager.EmoteFetchCallback() {
            @Override
            public void onEmoteFetched() {
                onProgressUpdate(new ChatManager.ProgressUpdate(ChatManager.ProgressUpdate.UpdateType.ON_BTTV_FETCHED));
            }
        });

        ChatProperties properties = fetchChatProperties();
        if(properties != null) {
            String ipAndPort = properties.getChatIp();
            String[] ipAndPortArr = ipAndPort.split(":");
            twitchChatServer = ipAndPortArr[0];
            twitchChatPort = Integer.parseInt(ipAndPortArr[1]);
        }

        connect(twitchChatServer, twitchChatPort);

        return null;
    }

    @Override
    protected void onProgressUpdate(ProgressUpdate... values) {
        super.onProgressUpdate(values);
        final ProgressUpdate update = values[0];
        final ProgressUpdate.UpdateType type = update.getUpdateType();
        callbackHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case ON_MESSAGE:
                        callback.onMessage(update.getMessage());
                        break;
                    case ON_CONNECTED:
                        callback.onConnected();
                        break;
                    case ON_CONNECTING:
                        callback.onConnecting();
                        break;
                    case ON_CONNECTION_FAILED:
                        callback.onConnectionFailed();
                        break;
                    case ON_RECONNECTING:
                        callback.onReconnecting();
                        break;
                    case ON_ROOMSTATE_CHANGE:
                        callback.onRoomstateChange(chatIsR9kmode, chatIsSlowmode, chatIsSubsonlymode);
                        break;
                    case ON_BTTV_FETCHED:
                        callback.onBttvEmoteIdFetched(
                                mEmoteManager.getChanncelBttvEmotes(), mEmoteManager.getGlobalBttvEmotes()
                        );
                        break;
                }
            }
        });
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }


    private void connect(String address, int port) {
        try {
            @SuppressWarnings("resource")
            Socket socket = new Socket(address, port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.write("PASS " + oauth_key + "\r\n");
            writer.write("NICK " + user + "\r\n");
            writer.write("USER " + user + " \r\n");
            writer.flush();

            String line = "";
            while ((line = reader.readLine()) != null) {
                if (isStopping) {
                    leaveChannel();
                    Log.d(LOG_TAG, "Stopping chat for " + channelName);
                    break;
                }

                if (line.contains("001 " + user + " :")) {
                    Log.d(LOG_TAG, "<" + line);
                    Log.d(LOG_TAG, "Connected >> " + user + " ~ irc.twitch.tv");
                    onProgressUpdate(new ProgressUpdate(ProgressUpdate.UpdateType.ON_CONNECTED));
                    sendRawMessage("CAP REQ :twitch.tv/tags twitch.tv/commands");
                    sendRawMessage("JOIN " + hashChannel + "\r\n");
                } else if(userDisplayName == null && line.contains("USERSTATE " + hashChannel)) {
                    handleUserstate(line);
                } else if(line.contains("ROOMSTATE " + hashChannel)) {
                    handleRoomstate(line);
                } else if(line.contains("NOTICE " + hashChannel)) {
                    handleNotice(line);
                } else if (line.startsWith("PING")) { // Twitch wants to know if we are still here. Send PONG and Server info back
                    handlePing(line);
                } else if (line.contains("PRIVMSG")) {
                    handleMessage(line);
                } else if (line.toLowerCase().contains("disconnected"))	{
                    Log.e(LOG_TAG, "Disconnected - trying to reconnect");
                    onProgressUpdate(new ProgressUpdate(ProgressUpdate.UpdateType.ON_RECONNECTING));
                    connect(address, port); //ToDo: Test if chat keeps playing if connection is lost
                } else if(line.contains("NOTICE * :Error logging in")) {
                    onProgressUpdate(new ProgressUpdate(ProgressUpdate.UpdateType.ON_CONNECTION_FAILED));
                } else {
                    Log.d(LOG_TAG, "<" + line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            onProgressUpdate(new ProgressUpdate(ProgressUpdate.UpdateType.ON_CONNECTION_FAILED));
        }
    }

    private void handleNotice(String line) {
        Matcher noticeMatcher = noticePattern.matcher(line);
        if(noticeMatcher.find()) {
            String msgId = noticeMatcher.group(1);
            if(msgId.equals("subs_on")) {
                chatIsSubsonlymode = true;
            } else if(msgId.equals("subs_off")) {
                chatIsSubsonlymode = false;
            } else if(msgId.equals("slow_on")) {
                chatIsSlowmode = true;
            } else if(msgId.equals("slow_off")) {
                chatIsSlowmode = false;
            } else if(msgId.equals("r9k_on")) {
                chatIsR9kmode = true;
            } else if(msgId.equals("r9k_off")) {
                chatIsR9kmode = false;
            }

            onProgressUpdate(new ProgressUpdate(ProgressUpdate.UpdateType.ON_ROOMSTATE_CHANGE));
        }
    }


    private void handleRoomstate(String line) {
        Matcher roomstateMatcher = roomstatePattern.matcher(line);
        if(roomstateMatcher.find()) {
            String broadcastlanguage = roomstateMatcher.group(1);
            boolean newR9k = roomstateMatcher.group(2).equals("1");
            boolean newSlow = !roomstateMatcher.group(3).equals("0");
            boolean newSub = roomstateMatcher.group(4).equals("1");
            if(chatIsR9kmode != newR9k || chatIsSlowmode != newSlow || chatIsSubsonlymode != newSub) {
                chatIsR9kmode = newR9k;
                chatIsSlowmode = newSlow;
                chatIsSubsonlymode = newSub;

                onProgressUpdate(new ProgressUpdate(ProgressUpdate.UpdateType.ON_ROOMSTATE_CHANGE));
            }
        }
    }

    private void handleUserstate(String line) {
        Matcher userstateMatcher = userStatePattern.matcher(line);
        if(userstateMatcher.find()) {
            userColor = userstateMatcher.group(1);
            userDisplayName = userstateMatcher.group(2);
            String emoteSets = userstateMatcher.group(3);
            userIsMod = userstateMatcher.group(4).equals("1");
            userIsSubscriber = userstateMatcher.group(5).equals("1");
            if (userstateMatcher.groupCount() > 7) {
                userIsTurbo = userstateMatcher.group(7).equals("1");
            }

        } else {
            Log.e(LOG_TAG, "Failed to find userstate pattern in: \n" + line);
        }
    }


    private void handleMessage(String line) {
        Matcher stdVarMatcher = stdVarPattern.matcher(line);
        List<ChatEmoji> emotes = new ArrayList<>(mEmoteManager.findTwitchEmotes(line));

        if(stdVarMatcher.find()) {
            String color = stdVarMatcher.group(1);
            String displayName = stdVarMatcher.group(2);
            boolean isMod = stdVarMatcher.group(3).equals("1");
            boolean isSubscriber = stdVarMatcher.group(4).equals("1");
            boolean isTurbo = stdVarMatcher.group(5).equals("1");
            String message = stdVarMatcher.group(6);
            emotes.addAll(mEmoteManager.findBttvEmotes(message));
            boolean highlight = false;//Pattern.compile(Pattern.quote(userDisplayName), Pattern.CASE_INSENSITIVE).matcher(message).find();

            ChatMessage chatMessage = new ChatMessage(message, displayName, color, isMod, isTurbo, isSubscriber, emotes, subscriberIcon, highlight);
            publishProgress(new ProgressUpdate(ProgressUpdate.UpdateType.ON_MESSAGE, chatMessage));
        } else {
            Log.e(LOG_TAG, "Failed to find message pattern in: \n" + line);
        }
    }


    private void handlePing(String line) throws IOException {
        writer.write("PONG " + line.substring(5) + "\r\n");
        writer.flush();
    }

    private void sendRawMessage(String message) {
        try {
            writer.write(message + " \r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isStopping = true;
    }


    public void sendMessage(final String message) {
        try {
            if (writer != null) {
                writer.write("PRIVMSG " + hashChannel + " :" + message + "\r\n");
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void leaveChannel() {
        sendRawMessage("PART " + hashChannel);
    }


    private ChatProperties fetchChatProperties() {
        final String URL = "https://api.twitch.tv/api/channels/" + channelName + "/chat_properties";
        final String CHAT_SERVERS_ARRAY = "chat_servers";

        try {
            JSONObject dataObject = new JSONObject(AppPrefs.urlToJSONString(URL));
            JSONArray chatServers = dataObject.getJSONArray(CHAT_SERVERS_ARRAY);

            ArrayList<String> chatServersResult = new ArrayList<>();
            for(int i = 0; i < chatServers.length(); i++) {
                chatServersResult.add(chatServers.getString(i));
            }

            return new ChatProperties(chatServersResult);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected static class ProgressUpdate {
        public enum UpdateType {
            ON_MESSAGE,
            ON_CONNECTING,
            ON_RECONNECTING,
            ON_CONNECTED,
            ON_CONNECTION_FAILED,
            ON_ROOMSTATE_CHANGE,
            ON_BTTV_FETCHED
        }

        private UpdateType updateType;
        private ChatMessage message;

        public ProgressUpdate(UpdateType type) {
            updateType = type;
        }

        public ProgressUpdate(UpdateType type, ChatMessage aMessage) {
            updateType = type;
            message = aMessage;
        }

        public UpdateType getUpdateType() {
            return updateType;
        }

        public void setUpdateType(UpdateType updateType) {
            this.updateType = updateType;
        }

        public ChatMessage getMessage() {
            return message;
        }

        public void setMessage(ChatMessage message) {
            this.message = message;
        }
    }
}