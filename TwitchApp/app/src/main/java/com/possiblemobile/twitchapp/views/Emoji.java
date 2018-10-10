package com.possiblemobile.twitchapp.views;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Emoji implements Comparable<Emoji>, Serializable {
    private String emojiId, emojiKeyword;
    private boolean isBetterTTVEmoji, isTextEmoji, isBetterTTVChannelEmote;

    public Emoji(String emoteId, String emojiKeyword, boolean isBetterTTVEmote) {
        this.emojiId = emoteId;
        this.emojiKeyword = emojiKeyword;
        this.isBetterTTVEmoji = isBetterTTVEmote;
        this.isTextEmoji = false;
    }

    public Emoji(String textEmoteUnicode) {
        emojiKeyword = textEmoteUnicode;
        isTextEmoji = true;
    }

    public boolean isBetterTTVChannelEmote() {
        return isBetterTTVChannelEmote;
    }

    public void setBetterTTVChannelEmote(boolean betterTTVChannelEmote) {
        isBetterTTVChannelEmote = betterTTVChannelEmote;
    }


    public String getEmoteId() {
        return emojiId;
    }

    public boolean isBetterTTVEmote() {
        return isBetterTTVEmoji;
    }


    public boolean isTextEmoji() {
        return isTextEmoji;
    }

    public void setTextEmoji(boolean textEmoji) {
        isTextEmoji = textEmoji;
    }

    @Override
    public int compareTo(@NonNull Emoji emoji) {
        if (this.isBetterTTVChannelEmote() && !emoji.isBetterTTVChannelEmote()) {
            return -1;
        } else if (emoji.isBetterTTVChannelEmote() && !this.isBetterTTVChannelEmote()) {
            return 1;
        } else {
            return this.emojiKeyword.compareTo(emoji.emojiKeyword);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Emoji emoji = (Emoji) o;

        if (isBetterTTVEmoji != emoji.isBetterTTVEmoji) return false;
        if (isTextEmoji != emoji.isTextEmoji) return false;
        if (emojiId != null ? !emojiId.equals(emoji.emojiId) : emoji.emojiId != null) return false;
        return emojiKeyword != null ? emojiKeyword.equals(emoji.emojiKeyword) : emoji.emojiKeyword == null;
    }

    @Override
    public int hashCode() {
        int result = emojiId != null ? emojiId.hashCode() : 0;
        result = 31 * result + (emojiKeyword != null ? emojiKeyword.hashCode() : 0);
        result = 31 * result + (isBetterTTVEmoji ? 1 : 0);
        result = 31 * result + (isTextEmoji ? 1 : 0);
        return result;
    }
}
