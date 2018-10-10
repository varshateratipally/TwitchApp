package com.possiblemobile.twitchapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.possiblemobile.twitchapp.LiveStreamActivity;
import com.possiblemobile.twitchapp.model.StreamItem;

import java.util.List;
import com.possiblemobile.twitchapp.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StreamsAdapter extends RecyclerView.Adapter<StreamsAdapter.ViewHolder>{
    private List<StreamItem> streamItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_view)
        ImageView imageView;
        @BindView(R.id.txt_stream_title)
        TextView streamTitle;
        @BindView(R.id.stream_item)
        RelativeLayout relItem;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public StreamsAdapter(List<StreamItem> streamItemList) {
        this.streamItems = streamItemList;
    }

    @Override
    public StreamsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stream, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StreamsAdapter.ViewHolder holder, int position) {
        final StreamItem streamItem = streamItems.get(position);
        loadImage(streamItem.getPreview().getTemplate(), holder.imageView);
        loadText(streamItem.getChannel().getDisplayName(), holder.streamTitle);
        loadDetails(streamItem, holder.relItem);
    }

    @Override
    public int getItemCount() {
        return streamItems.size();
    }

    private void loadImage(String url, ImageView img) {
        Picasso.with(img.getContext())
                .load(getBoxUrl(url,img.getContext().getResources().getDisplayMetrics().density))
                .into(img);
    }

    public static String getBoxUrl(String url, float density) {
        String width = String.valueOf((int) (152 * density));
        String height = String.valueOf((int) (218 * density));
        return url.replace("{width}", width).replace("{height}", height);
    }

    private void loadText(String text, TextView txt) {
        txt.setText(text);
    }

    private void loadDetails(final StreamItem streamItem, final View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.getContext().startActivity(getDetailIntent(streamItem, v.getContext()));
            }
        });
    }

    private Intent getDetailIntent(StreamItem streamItem, Context ctx) {
        return LiveStreamActivity.createLiveStreamIntent(streamItem,false, ctx);
    }
}
