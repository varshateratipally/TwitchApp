package com.possiblemobile.twitchapp.views;


import android.content.res.Configuration;
import android.graphics.ColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.possiblemobile.twitchapp.model.ChannelInfo;
import com.possiblemobile.twitchapp.R;

import java.util.ArrayList;
import java.util.List;
import settings.AppPrefs;
import com.possiblemobile.twitchapp.tasks.SendMessageTask;



public class ChatFragment extends Fragment {

    private boolean chatStatusBarVisibility = true;

    private ChatAdapter mChatAdapter;
    private ChatManager chatManager;
    private ChannelInfo mChannelInfo;
    private AppPrefs appPrefs;

    private RelativeLayout mChatInputLayout;
    private RecyclerView mRecyclerView;
    private EditTextBackEvent 	mSendText;
    private ImageView mSendButton;
    private TextView mChatStatus;
    private View chatInputDivider;
    private FrameLayout mChatStatusBar;


    private ColorFilter defaultBackgroundColor;

    public static ChatFragment getInstance(Bundle args) {
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mRootView = inflater.inflate(R.layout.fragment_chat, container, false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setStackFromEnd(true);
        appPrefs = new AppPrefs();

        mSendText = mRootView.findViewById(R.id.send_message_textview);
        mSendButton = mRootView.findViewById(R.id.chat_send_ic);
        mRecyclerView = mRootView.findViewById(R.id.ChatRecyclerView);
        chatInputDivider = mRootView.findViewById(R.id.chat_input_divider);
        mChatInputLayout = mRootView.findViewById(R.id.chat_input);
        mChatInputLayout.bringToFront();
        mChatStatus = mRootView.findViewById(R.id.chat_status_text);
        mChatAdapter = new ChatAdapter(mRecyclerView, getActivity());
        mChatStatusBar = mRootView.findViewById(R.id.chat_status_bar);

        defaultBackgroundColor = mSendButton.getColorFilter();
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(null);

        mChannelInfo = getArguments().getParcelable(getString(R.string.stream_fragment_streamerInfo));

        if (!appPrefs.isUserLoggedIn()) {
            userNotLoggedIn();
        } else {
            setupChatInput();
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        final ChatFragment instance = this;
        chatManager = new ChatManager(getContext(), mChannelInfo.getName(), mChannelInfo.getChannelId(), new ChatManager.ChatCallback() {
            private boolean connected = false;

            private boolean isFragmentActive() {
                return instance != null && !instance.isDetached() && instance.isAdded();
            }

            @Override
            public void onMessage(ChatMessage message) {
                mRecyclerView.bringToFront();
                if (isFragmentActive())
                    addMessage(message);
            }

            @Override
            public void onConnecting() {
                if (isFragmentActive()) {
                    ChatFragment.this.showChatStatusBar();
                    mChatStatus.setText(getString(R.string.chat_status_connecting));
                }
            }

            @Override
            public void onReconnecting() {
                if (isFragmentActive()) {
                    ChatFragment.this.showChatStatusBar();
                    mChatStatus.setText(getString(R.string.chat_status_reconnecting));
                }
            }

            @Override
            public void onConnected() {
                if (isFragmentActive()) {
                    this.connected = true;
                    ChatFragment.this.showThenHideChatStatusBar();
                    mChatStatus.setText(getString(R.string.chat_status_connected));
                }
            }

            @Override
            public void onConnectionFailed() {
                if (isFragmentActive()) {
                    this.connected = false;
                    ChatFragment.this.showChatStatusBar();
                    mChatStatus.setText(getString(R.string.chat_status_connection_failed));
                }
            }

            @Override
            public void onRoomstateChange(boolean isR9K, boolean isSlow, boolean isSubsOnly) {
                if (isFragmentActive()) {
                    if (this.connected) {
                        ChatFragment.this.showThenHideChatStatusBar();
                    } else {
                        ChatFragment.this.showChatStatusBar();
                    }
                }
            }

            @Override
            public void onBttvEmoteIdFetched(List<Emoji> bttvChannel, List<Emoji> bttvGlobal) {

            }

        });

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        chatManager.stop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void showThenHideChatStatusBar() {
        this.showChatStatusBar();
        this.hideChatStatusBar();
    }

    private void showChatStatusBar() {
        if (!this.chatStatusBarVisibility) {
            this.mChatStatusBar.setVisibility(View.VISIBLE);
            this.chatStatusBarVisibility = true;
        }
    }


    private void hideChatStatusBar() {
        if (this.chatStatusBarVisibility) {
            this.mChatStatusBar.setVisibility(View.GONE);
            this.chatStatusBarVisibility = false;
        }
    }


    private void setupChatInput() {
        mChatInputLayout.bringToFront();
        chatInputDivider.bringToFront();
        mSendText.bringToFront();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mSendText.setOnEditTextImeBackListener(new EditTextBackEvent.EditTextImeBackListener() {
            @Override
            public void onImeBack(EditTextBackEvent ctrl, String text) {

            }
        });

        mSendText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });


        mSendText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mSendText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (mSendText.getText().length() > 0) {
                    mSendButton.setColorFilter(defaultBackgroundColor);
                    mSendButton.setClickable(true);
                } else {
                    mSendButton.setColorFilter(defaultBackgroundColor);
                    mSendButton.setClickable(false);
                }
            }
        });
        mSendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void userNotLoggedIn() {
        mChatInputLayout.setVisibility(View.GONE);
        chatInputDivider.setVisibility(View.GONE);
    }


    private void sendMessage() {
        final String message = mSendText.getText() + "";
        if (message.isEmpty()) {
            return;
        }
        mSendText.setText("");

        ChatMessage chatMessage = new ChatMessage(message, new AppPrefs().getTwitchName(), "#000000", false,false, false,
                new ArrayList<ChatEmoji>(),chatManager.getSubscriberIcon(), false);
        mChatAdapter.add(chatMessage);

        SendMessageTask sendMessageTask = new SendMessageTask(chatManager, chatMessage.toString());
        sendMessageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void addMessage(ChatMessage message) {
        mChatAdapter.add(message);
    }

}
