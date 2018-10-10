package com.possiblemobile.twitchapp.views;

import android.content.Context;
import android.content.DialogInterface;
import com.possiblemobile.twitchapp.tasks.GetLiveStreamURLTask;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.VideoView;


import com.possiblemobile.twitchapp.StreamVideoActivity;
import com.possiblemobile.twitchapp.model.ChannelInfo;


import com.possiblemobile.twitchapp.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;


public class StreamFragment extends Fragment {

    private int HIDE_ANIMATION = 3000;

    private final Handler delayAnimationHandler = new Handler();
    public boolean embedded = false;
    public ChannelInfo mChannelInfo;
    public String vodId;
    public boolean
            audioViewVisible = false,
            chatOnlyViewVisible = false,
            autoPlay = true,
            hasPaused = false;
    private boolean previewInbackGround = false;

    private VideoView mVideoView;
    private String videoURL;
    private Toolbar mToolbar;
    private RelativeLayout mControlToolbar,
            mVideoWrapper;
    private FrameLayout mPlayPauseWrapper;
    private ImageView mPauseIcon,
            mPlayIcon,
            mPreview;
    private TextView mCurrentViewersView;
    private AppCompatActivity mActivity;
    private Snackbar snackbar;
    private ProgressBar mBufferingView;


    private BottomSheetDialog mProfileBottomSheet;
    private ViewGroup rootView;
    private MenuItem optionsMenuItem;
    private View mClickIntercepter;
    private final Runnable hideAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null)
                hideVideoInterface();
        }
    };
    private int originalCtrlToolbarPadding,
            originalMainToolbarPadding;
    public static StreamFragment newInstance(Bundle args) {
        StreamFragment fragment = new StreamFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Bundle args = getArguments();
        setHasOptionsMenu(true);

        if (args != null) {
            mChannelInfo = args.getParcelable(getString(R.string.stream_fragment_streamerInfo));
            vodId = args.getString(getString(R.string.stream_fragment_vod_id));
            autoPlay = args.getBoolean(getString(R.string.stream_fragment_autoplay));

        }

        final View mRootView = inflater.inflate(R.layout.fragment_stream, container, false);
        mRootView.requestLayout();

        if (getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }


        if (mChannelInfo == null) {
            if (getActivity() != null) {
                getActivity().finish();
            }
            return rootView;
        }

        rootView = (ViewGroup) mRootView;
        mToolbar = mRootView.findViewById(R.id.main_toolbar);
        mControlToolbar = mRootView.findViewById(R.id.control_toolbar_wrapper);
        mVideoWrapper = mRootView.findViewById(R.id.video_wrapper);
        mVideoView = mRootView.findViewById(R.id.VideoView);
        mPlayPauseWrapper = mRootView.findViewById(R.id.play_pause_wrapper);
        mPlayIcon = mRootView.findViewById(R.id.ic_play);
        mPauseIcon = mRootView.findViewById(R.id.ic_pause);
        mPreview = mRootView.findViewById(R.id.preview);
        mBufferingView = mRootView.findViewById(R.id.circle_progress);
        mCurrentViewersView = mRootView.findViewById(R.id.txtViewViewers);
        mActivity = ((AppCompatActivity) getActivity());
        mClickIntercepter = mRootView.findViewById(R.id.click_intercepter);
        View mCurrentViewersWrapper = mRootView.findViewById(R.id.viewers_wrapper);

        setPreviewAndCheckForSharedTransition();
        setupToolbar();
        setupProfileBottomSheet();

        mPlayPauseWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPauseWrapper.getAlpha() < 0.5f) {
                    return;
                }

                try {
                    if (mVideoView.isPlaying()) {
                        pauseStream();
                    } else if (!mVideoView.isPlaying()) {
                        resumeStream();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    startStreamWithQuality(videoURL);
                }
            }
        });
        mVideoWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delayAnimationHandler.removeCallbacks(hideAnimationRunnable);
                if (isVideoInterfaceShowing()) {
                    hideVideoInterface();
                } else {
                    showVideoInterface();

                    if (mVideoView.isPlaying()) {
                        delayHiding();
                    }

                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setAndroidUiMode();
                        }
                    }, HIDE_ANIMATION);
                }
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                playbackFailed();
                return true;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START || what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        mBufferingView.setVisibility(View.GONE);
                        hideVideoInterface();
                        delayHiding();
                        if (!previewInbackGround) {
                            hidePreview();
                        }
                    }

                    if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        mBufferingView.setVisibility(View.VISIBLE);
                        delayAnimationHandler.removeCallbacks(hideAnimationRunnable);
                        showVideoInterface();

                    }

                    return true;                }
            });
        }

        mRootView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0) {
                    showVideoInterface();
                    delayHiding();
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setAndroidUiMode();
                        }
                    }, HIDE_ANIMATION);
                }
            }
        });

        View mTimeController = mRootView.findViewById(R.id.time_controller);
        mTimeController.setVisibility(View.INVISIBLE);

        if (args != null && args.containsKey(getString(R.string.stream_fragment_viewers))) {
            mCurrentViewersView.setText("" + args.getInt(getString(R.string.stream_fragment_viewers)));
        } else {
            mCurrentViewersWrapper.setVisibility(View.GONE);
        }
        keepScreenOn();
        if (autoPlay || vodId != null) {
            startStreamWithQuality(videoURL);
        }
        return mRootView;
    }

    private void hidePreview() {
        bringToBack(mPreview);
        previewInbackGround = true;
    }

    public static void bringToBack(final View v) {
        final ViewGroup parent = (ViewGroup) v.getParent();
        if (null != parent) {
            parent.removeView(v);
            parent.addView(v, 0);
        }
    }


    public void backPressed() {
        mVideoView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();

        originalMainToolbarPadding = mToolbar.getPaddingRight();
        originalCtrlToolbarPadding = mControlToolbar.getPaddingRight();

        if (!audioViewVisible && hasPaused) {
            startStreamWithQuality(videoURL);
        }

        if (!chatOnlyViewVisible) {
            showVideoInterface();
            updateUI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hasPaused = true;
    }

    @Override
    public void onStop() {
        super.onStop();

        mBufferingView.setVisibility(View.GONE);
        pauseStream();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void profileButtonClicked() {
        mProfileBottomSheet.show();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!embedded) {
            optionsMenuItem = menu.findItem(R.id.menu_item_options);
            optionsMenuItem.setVisible(false);
            optionsMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isVideoInterfaceShowing()) {
            mVideoWrapper.performClick();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_item_profile:
                profileButtonClicked();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setPreviewAndCheckForSharedTransition() {
        final Intent intent = getActivity().getIntent();
        if (intent.hasExtra(getString(R.string.stream_preview_url))) {
            String imageUrl = intent.getStringExtra(getString(R.string.stream_preview_url));

            if (imageUrl == null || imageUrl.isEmpty()) {
                return;
            }

            RequestCreator creator = Picasso.with(getContext()).load(imageUrl);
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mPreview.setImageBitmap(bitmap);
                }

                public void onBitmapFailed(Drawable errorDrawable) {
                }

                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            creator.into(target);
        }
    }

    private void updateUI() {
        setAndroidUiMode();
        keepControlIconsInView();
        setVideoViewLayout();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        setAndroidUiMode();
    }


    private void setAndroidUiMode() {
        if (getActivity() == null) {
            return;
        }

        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);

    }

    private void setVideoViewLayout() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        RelativeLayout.LayoutParams layoutWrapper = (RelativeLayout.LayoutParams) mVideoWrapper.getLayoutParams();

            layoutWrapper.height = (int) Math.ceil(1.0 * width / (16.0 / 9.0));

        layoutWrapper.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        mVideoWrapper.setLayoutParams(layoutWrapper);
    }

    private void delayHiding() {
        delayAnimationHandler.postDelayed(hideAnimationRunnable, HIDE_ANIMATION);
    }


    public boolean isVideoInterfaceShowing() {
        return mControlToolbar.getAlpha() == 1f;
    }

    private void hideVideoInterface() {
        if (mToolbar != null && !audioViewVisible && !chatOnlyViewVisible) {
            mToolbar.animate().alpha(0f).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            mControlToolbar.animate().alpha(0f).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            mPlayPauseWrapper.animate().alpha(0f).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            changeVideoControlClickablity(false);
        }
    }


    protected void showVideoInterface() {
        int MaintoolbarY = 0, CtrlToolbarY = 0;
        mControlToolbar.setTranslationY(-CtrlToolbarY);
        mControlToolbar.animate().alpha(1f).start();
        mToolbar.setTranslationY(MaintoolbarY);
        mToolbar.animate().alpha(1f).start();
        mPlayPauseWrapper.animate().alpha(1f).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        changeVideoControlClickablity(true);
    }

    private void changeVideoControlClickablity(boolean clickable) {
        mClickIntercepter.setVisibility(clickable ? View.GONE : View.VISIBLE);
        mClickIntercepter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoWrapper.performClick();
            }
        });
    }


    private void keepControlIconsInView() {
            int ctrlPadding = originalCtrlToolbarPadding;
            int mainPadding = originalMainToolbarPadding;

            mToolbar.setPadding(0, 0, mainPadding, 0);
            mControlToolbar.setPadding(0, 0, ctrlPadding, 0);

    }

    private void pauseStream() {
        showPlayIcon();

        delayAnimationHandler.removeCallbacks(hideAnimationRunnable);


            mVideoView.pause();

        releaseScreenOn();
    }

    private void resumeStream() {
        showPauseIcon();
        mBufferingView.setVisibility(View.VISIBLE);


            if (vodId == null) {
                mVideoView.resume();
            }

            mVideoView.start();

        keepScreenOn();
    }

    protected void startStreamWithQuality(String quality) {
        if (videoURL == null) {
            startStreamWithTask();
        } else {
            playUrl(quality);
            showPauseIcon();
            mBufferingView.setVisibility(View.VISIBLE);

        }
    }

    private void startStreamWithTask() {
        GetLiveStreamURLTask.AsyncResponse callback = new GetLiveStreamURLTask.AsyncResponse() {
            @Override
            public void finished(String url) {
                try {
                    if(!url.isEmpty()) {
                        videoURL = url;
                        startStreamWithQuality(videoURL);
                    }
                } catch (IllegalStateException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };
        GetLiveStreamURLTask task = new GetLiveStreamURLTask(callback);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mChannelInfo.getName());
    }

    private void playbackFailed() {
        mBufferingView.setVisibility(View.GONE);
        if (vodId == null) {
            showSnackbar("Stream failed");
        }
    }

    private void showSnackbar(String message) {
        if (getActivity() != null && !isDetached()) {
            View mainView = ((StreamVideoActivity) getActivity()).getMainContentLayout();

            if ((snackbar == null || !snackbar.isShown()) && mainView != null) {
                snackbar = Snackbar.make(mainView, message, 100);
                snackbar.show();
            }
        }

    }

    private void playUrl(String url) {
        mVideoView.setVideoPath(url);
        resumeStream();
    }


    private void keepScreenOn() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private void releaseScreenOn() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private BottomSheetBehavior getDefaultBottomSheetBehaviour(View bottomSheetView) {
        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        behavior.setPeekHeight(getContext().getResources().getDisplayMetrics().heightPixels / 3);
        return behavior;
    }

    private void setupProfileBottomSheet() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.stream_profile_preview, null);
        mProfileBottomSheet = new BottomSheetDialog(getContext());
        mProfileBottomSheet.setContentView(v);
        final BottomSheetBehavior behavior = getDefaultBottomSheetBehaviour(v);

        mProfileBottomSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        TextView mNameView = mProfileBottomSheet.findViewById(R.id.twitch_name);
        TextView mFollowers = mProfileBottomSheet.findViewById(R.id.txt_followers);
        TextView mViewers = mProfileBottomSheet.findViewById(R.id.txt_viewers);
        ImageView mFullProfileButton = mProfileBottomSheet.findViewById(R.id.full_profile_icon);

        mNameView.setText(mChannelInfo.getDisplayName());
        mFollowers.setText(mChannelInfo.getFollowers() + "");
        mViewers.setText(mChannelInfo.getViewers() + "");

        mFullProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileBottomSheet.dismiss();

            }
        });

    }


    private void setupToolbar() {
        mToolbar.setPadding(0, 0, dpToPixels(getActivity(), 5), 0);
        setHasOptionsMenu(true);
        mActivity.setSupportActionBar(mToolbar);
        mActivity.getSupportActionBar().setTitle(mChannelInfo.getDisplayName());
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.bringToFront();
    }

    public static int dpToPixels(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void showPauseIcon() {
        if (mPauseIcon.getVisibility() == View.GONE) {
            mPauseIcon.setVisibility(View.VISIBLE);
            mPlayIcon.setVisibility(View.GONE);
        }
    }

    private void showPlayIcon() {
        if (mPauseIcon.getVisibility() != View.GONE) {
            mPlayIcon.setVisibility(View.VISIBLE);
            mPauseIcon.setVisibility(View.GONE);
        }
    }

}
