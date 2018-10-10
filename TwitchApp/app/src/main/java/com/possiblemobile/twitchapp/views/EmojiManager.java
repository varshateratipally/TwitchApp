package com.possiblemobile.twitchapp.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import settings.AppPrefs;
import com.possiblemobile.twitchapp.R;

public class EmojiManager {
    private static LruCache<String, Bitmap> cache = new LruCache<>(4 * 1024 * 1024);
    private static Map<String, String> bttvEmojiesToId;

    private final List<Emoji> bttvGlobal = new ArrayList<>();
    private final List<Emoji> bttvChannel = new ArrayList<>();

    private Pattern bttvEmojiesPattern = Pattern.compile("");
    private Pattern emojiPattern = Pattern.compile("(\\d+):((?:\\d+-\\d+,?)+)");

    private Context context;
    private int channelId;
    private String channelName;

    public EmojiManager(int channelId, String channelName, Context context) {
        this.context = context;
        this.channelId = channelId;
        this.channelName = channelName;
    }

    protected void loadBttvEmotes(EmoteFetchCallback callback) {
        Map<String, String> result = new HashMap<>();
        String emojiPattern = "";

        final String BASE_GLOBAL_URL = "https://api.betterttv.net/2/emotes";
        final String BASE_CHANNEL_URL = "https://api.betterttv.net/2/channels/" + channelName;
        final String EMOTE_ARRAY = "emotes";
        final String EMOTE_ID = "id";
        final String EMOTE_WORD = "code";

        try {
            JSONObject topObject = new JSONObject(AppPrefs.urlToJSONString(BASE_GLOBAL_URL));
            JSONArray globalEmojies = topObject.getJSONArray(EMOTE_ARRAY);

            for (int i = 0; i < globalEmojies.length(); i++) {
                JSONArray arrayWithEmoji = globalEmojies;

                JSONObject emojiObject = arrayWithEmoji.getJSONObject(i);
                String emojiKeyword = emojiObject.getString(EMOTE_WORD);
                String emojiId = emojiObject.getString(EMOTE_ID);
                result.put(emojiKeyword, emojiId);

                Emoji emoji = new Emoji(emojiId, emojiKeyword, true);
                bttvGlobal.add(emoji);

                if (emojiPattern.equals("")) {
                    emojiPattern = Pattern.quote(emojiKeyword);
                } else {
                    emojiPattern += "|" + Pattern.quote(emojiKeyword);
                }
            }

            JSONObject topChannelEmotes = new JSONObject(AppPrefs.urlToJSONString(BASE_CHANNEL_URL));
            JSONArray channelEmotes = topChannelEmotes.getJSONArray(EMOTE_ARRAY);
            for (int i = 0; i < channelEmotes.length(); i++) {
                JSONArray arrayWithEmote = channelEmotes;

                JSONObject emoteObject = arrayWithEmote.getJSONObject(i);
                String emoteKeyword = emoteObject.getString(EMOTE_WORD);
                String emoteId = emoteObject.getString(EMOTE_ID);
                result.put(emoteKeyword, emoteId);

                Emoji emoji = new Emoji(emoteId, emoteKeyword, true);
                emoji.setBetterTTVChannelEmote(true);
                bttvChannel.add(emoji);

                if (emojiPattern.equals("")) {
                    emojiPattern = Pattern.quote(emoteKeyword);
                } else {
                    emojiPattern += "|" + Pattern.quote(emoteKeyword);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        bttvEmojiesPattern = Pattern.compile("\\b(" + emojiPattern + ")\\b");
        bttvEmojiesToId = result;

        try {
            callback.onEmoteFetched();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Bitmap getSubscriberEmote() {
        Bitmap eemoji = null;

        final String URL = "https://api.twitch.tv/kraken/chat/" + channelId + "/badges";
        final String SUBSCRIBER_OBJECT = "subscriber";
        final String SUBSCRIBER_IMAGE_STRING = "image";

        try {
            JSONObject dataObject = new JSONObject(AppPrefs.urlToJSONString(URL));
            JSONObject subscriberObject = dataObject.getJSONObject(SUBSCRIBER_OBJECT);
            String imageUrl = subscriberObject.getString(SUBSCRIBER_IMAGE_STRING);

            eemoji = AppPrefs.getBitmapFromUrl(imageUrl);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(eemoji != null) {
            return eemoji;
        } else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_missing_emote);
        }
    }


    protected List<ChatEmoji> findBttvEmotes(String message) {
        List<ChatEmoji> emotes = new ArrayList<>();
        Matcher bttvEmoteMatcher = bttvEmojiesPattern.matcher(message);

        while (bttvEmoteMatcher.find()) {
            String emoteKeyword = bttvEmoteMatcher.group();
            String emoteId = bttvEmojiesToId.get(emoteKeyword);

            String[] positions = new String[] {bttvEmoteMatcher.start() + "-" + (bttvEmoteMatcher.end() - 1)};
            Bitmap emote = getEmoteFromId(emoteId, true);
            if (emote != null) {
                final ChatEmoji chatEmoji = new ChatEmoji(positions, emote);
                emotes.add(chatEmoji);
            }
        }

        return emotes;
    }


    protected List<ChatEmoji> findTwitchEmotes(String message) {
        List<ChatEmoji> emotes = new ArrayList<>();
        Matcher emoteMatcher = emojiPattern.matcher(message);

        while(emoteMatcher.find()) {
            String emoteId = emoteMatcher.group(1);
            String[] positions = emoteMatcher.group(2).split(",");
            emotes.add(new ChatEmoji(positions, getEmoteFromId(emoteId, false)));
        }

        return emotes;
    }


    protected Bitmap getEmoteFromId(String emoteId, boolean isBttvEmote) {
        String emoteKey = getEmoteStorageKey(emoteId, 2);
        if(cache.get(emoteKey) != null) {
            return cache.get(emoteKey);
        } else if (AppPrefs.doesStorageFileExist(emoteKey, context)) {
            try {
                Bitmap emote = AppPrefs.getImageFromStorage(emoteKey, context);
                if (emote != null) {
                    cache.put(emoteKey, emote);
                }
                return emote;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return saveAndGetEmote(getEmoteFromInternet(isBttvEmote, emoteId), emoteId);
    }


    private Bitmap getEmoteFromInternet(boolean isBttvEmote, String emoteId) {
        int settingsSize = getApiEmoteSizeFromSettingsSize(2);
        String emoteUrl = getEmoteUrl(isBttvEmote, emoteId, settingsSize);

        Bitmap emote = AppPrefs.getBitmapFromUrl(emoteUrl);
        emote = AppPrefs.getResizedBitmap(emote, 30, context);

        return emote;
    }



    private int getApiEmoteSizeFromSettingsSize(int settingsSize) {
        return settingsSize == 1 ? 2 : settingsSize;
    }

    private Bitmap saveAndGetEmote(Bitmap emote, String emoteId) {
        if(emote != null) {
            String emoteKey = getEmoteStorageKey(emoteId, 2);
            if ( !AppPrefs.doesStorageFileExist(emoteKey, context)) {
                AppPrefs.saveImageToStorage(emote, emoteKey, context);
            }
            cache.put(emoteKey, emote);
            return emote;
        } else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_missing_emote);
        }
    }


    public List<Emoji> getGlobalBttvEmotes() {
        return bttvGlobal;
    }

    public List<Emoji> getChanncelBttvEmotes() {
        return bttvChannel;
    }

    public interface EmoteFetchCallback {
        void onEmoteFetched();
    }


    public static String getEmoteStorageKey(String emoteId, int size) {
        return "emote-" + emoteId + "-" + size + "Upgraded";
    }

    public static String getEmoteUrl(boolean isEmoteBttv, String emoteId, int size) {
        return isEmoteBttv
                ? "https://cdn.betterttv.net/emote/" + emoteId + "/" + size + "x"
                : "https://static-cdn.jtvnw.net/emoticons/v1/" + emoteId + "/" + size + ".0";
    }

}
