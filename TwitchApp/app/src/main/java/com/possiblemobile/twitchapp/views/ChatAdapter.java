package com.possiblemobile.twitchapp.views;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import com.possiblemobile.twitchapp.R;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import settings.AppPrefs;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ContactViewHolder> implements Drawable.Callback {
    private List<ChatMessage> messages;
    private RecyclerView mRecyclerView;
    private Activity context;
    private Pattern linkPattern;
    private Matcher linkMatcher;

    private ImageSpan imageMod;
    private ImageSpan imageTurbo;
    private ImageSpan imageSub;
    private int emoteAlignment;


    private final String BLACK_TEXT = "#000000";
    private final String EMPTY_MESSAGE = "";
    private final String PREMESSAGE = ": ";

    public ChatAdapter(RecyclerView aRecyclerView, Activity aContext) {
        messages = new ArrayList<>();
        mRecyclerView = aRecyclerView;
        context = aContext;
        linkPattern = Pattern.compile("((http|https|ftp)\\:\\/\\/[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?\\/?([a-zA-Z0\u200C123456789\\-\\._\\?\\,\\'\\/\\\\\\+&amp;%\\$#\\=~])*[^\\.\\,\\)\\(\\s])");

        emoteAlignment = DynamicDrawableSpan.ALIGN_BASELINE;
        imageMod = new ImageSpan(context, R.drawable.ic_moderator, emoteAlignment);
        imageTurbo = new ImageSpan(context, R.drawable.ic_twitch_turbo, emoteAlignment);
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.chat_message, parent, false);

        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder holder, int position) {
        try {
            final ChatMessage message = messages.get(position);

            if(imageSub == null) {
                Bitmap resizedSubscriberEmote = AppPrefs.getResizedBitmap(message.getSubscriberIcon(),
                        imageMod.getDrawable().getIntrinsicWidth(),
                        imageMod.getDrawable().getIntrinsicHeight());
                imageSub = new ImageSpan(context, resizedSubscriberEmote, emoteAlignment);
            }


            final SpannableStringBuilder builder = new SpannableStringBuilder();
            if (message.getName() == null) {
                return;
            }

            builder.append(message.getName());
            int nameColor = getNameColor(message.getColor());
            builder.setSpan(new ForegroundColorSpan(nameColor), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            String messageWithPre = PREMESSAGE + message.getMessage();
            final SpannableStringBuilder resultMessage = new SpannableStringBuilder(messageWithPre);
            resultMessage.setSpan(new ForegroundColorSpan(getMessageColor()), 0, resultMessage.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            checkForLink(messageWithPre, resultMessage);

            for(ChatEmoji emote : message.getEmojies()) {
                for(String emotePosition : emote.getEmojiPositions()) {
                    String[] toAndFrom = emotePosition.split("-");
                    final int fromPosition = Integer.parseInt(toAndFrom[0]);
                    final int toPosition = Integer.parseInt(toAndFrom[1]);


                    final ImageSpan emoteSpan = new ImageSpan(context, emote.getEmojiBitmap(), emoteAlignment);
                    resultMessage.setSpan(emoteSpan, fromPosition + PREMESSAGE.length(), toPosition + 1 + PREMESSAGE.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                    holder.message.setTextIsSelectable(true);
                }
            }

            if (message.isHighlight()) {
                holder.message.setBackgroundColor(AppPrefs.getColorAttribute(R.attr.colorAccent, R.color.accent, context));
            }

            builder.append(resultMessage);
            builder.setSpan(new RelativeSizeSpan(getTextSize()), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.message.setText(builder);
            holder.message.setMovementMethod(LinkMovementMethod.getInstance());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void checkForLink(String message, SpannableStringBuilder spanbuilder) {
        linkMatcher = linkPattern.matcher(message);
        while(linkMatcher.find()) {
            final String url = linkMatcher.group(1);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    CustomTabsIntent.Builder mTabs = new CustomTabsIntent.Builder();
                    mTabs.build().launchUrl(context, Uri.parse(url));

                    mRecyclerView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                }
            };

            int start = message.indexOf(url);
            spanbuilder.setSpan(clickableSpan, start, start + url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private float getTextSize() {
        return 1f;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private int getNameColor(String colorFromAPI) {
        if (colorFromAPI.equals(EMPTY_MESSAGE) || colorFromAPI.equals(BLACK_TEXT)) {
                return ContextCompat.getColor(context, R.color.blue_700);
        }
        return Color.parseColor(colorFromAPI);
    }

    private int getMessageColor() {
        return ContextCompat.getColor(context, R.color.black_text);
    }

    public void add(ChatMessage message) {
        messages.add(message);

        notifyItemInserted(messages.size() - 1);
        checkSize();
        mRecyclerView.scrollToPosition(messages.size() - 1);
    }


    private void checkSize() {
        if(messages.size() > 500) {
            int messagesOverLimit = messages.size() - 500;
            for(int i = 0; i < messagesOverLimit; i++) {
                messages.remove(0);
                notifyItemRemoved(0);
            }
        }
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
    }

    @Override
    public void scheduleDrawable(Drawable drawable, Runnable runnable, long l) {

    }

    @Override
    public void unscheduleDrawable(Drawable drawable, Runnable runnable) {
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView message;

        public ContactViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.txt_message);
        }
    }
}
