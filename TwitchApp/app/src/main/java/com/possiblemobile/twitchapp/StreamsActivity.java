package com.possiblemobile.twitchapp;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.possiblemobile.twitchapp.adapter.StreamsAdapter;
import com.possiblemobile.twitchapp.model.StreamInfo;
import com.possiblemobile.twitchapp.model.StreamItem;
import com.possiblemobile.twitchapp.service.TwitchClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreamsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String query;

    @BindView(R.id.stream_search_query)
    public EditText streamSearchQuery;


    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycle_view_stream_search)
    RecyclerView recyclerView;
    @BindView(R.id.stream_progress)
    ProgressBar progressBar;
    private List<StreamItem> streamItems;
    private StreamsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streams);
        ButterKnife.bind(this);
        streamSearchQuery.setOnKeyListener(new View.OnKeyListener() {
                                                @Override
                                               public boolean onKey(View v, int keyCode, KeyEvent event) {
                                                   // If the event is a key-down event on the "enter" buttoquery = streamSearchQuery.getText().toString();
                                                    query = streamSearchQuery.getText().toString();
                                                    if (query!=null) {
                                                        startLoadings();
                                                        loadStreams();
                                                    } else {
                                                        startLoadings();
                                                        loadStreams();
                                                    }
                                                    return true;
                                               }
        }
        );
        initRecyclerView();
        initSwipeRefreshLayout();
        stopLoadings();

    }


    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        streamItems = new ArrayList<>();
        adapter = new StreamsAdapter(streamItems);
        recyclerView.setAdapter(adapter);
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void loadStreams(String query) {

            TwitchClient.getTwitchClient().getStreams(query,"vgl10ogqr6s8xqotaxc5256log6txm" ).enqueue(new Callback<StreamInfo>() {
                @Override
                public void onResponse(Call<StreamInfo> call, Response<StreamInfo> response) {
                    if (response.isSuccessful())
                        if (response.body().getStreamItemList().size() == 0) {
                            loadStreams();
                            return;
                        }
                        updateList(response.body().getStreamItemList());
                }

                @Override
                public void onFailure(Call<StreamInfo> call, Throwable t) {
                    stopLoadings();

                }
            });
    }

    private void loadStreams() {
        TwitchClient.getTwitchClient().getTopStreams("vgl10ogqr6s8xqotaxc5256log6txm" ).enqueue(new Callback<StreamInfo>() {
            @Override
            public void onResponse(Call<StreamInfo> call, Response<StreamInfo> response) {
                if (response.isSuccessful())
                    updateList(response.body().getStreamItemList());
            }

            @Override
            public void onFailure(Call<StreamInfo> call, Throwable t) {
                stopLoadings();

            }
        });
    }

    private void updateList(List<StreamItem> list) {
        streamItems.clear();
        streamItems.addAll(list);
        adapter.notifyDataSetChanged();
        stopLoadings();
    }

    @Override
    public void onRefresh() {
        loadStreams(query);
    }

    private void stopLoadings() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void startLoadings() {
        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);
    }

}
