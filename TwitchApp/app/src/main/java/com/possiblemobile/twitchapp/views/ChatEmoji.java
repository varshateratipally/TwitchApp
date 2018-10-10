package com.possiblemobile.twitchapp.views;

import android.graphics.Bitmap;

public class ChatEmoji {
    private String[] emojiPositions;
    private Bitmap emojiBitmap;

    public ChatEmoji(String[] emojiPositions, Bitmap emojiBitmap) {
        this.emojiPositions = emojiPositions;
        this.emojiBitmap = emojiBitmap;
    }

    public Bitmap getEmojiBitmap() {
        return emojiBitmap;
    }

    public String[] getEmojiPositions() {
        return emojiPositions;
    }

}