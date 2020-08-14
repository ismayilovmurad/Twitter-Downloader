package com.martiandeveloper.twitterdownloader.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.martiandeveloper.twitterdownloader.R;
import com.martiandeveloper.twitterdownloader.model.VideoModel;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<VideoModel> videoModelArrayList;
    public OnItemClickListener onItemClickListener;

    public VideoAdapter(Context context, ArrayList<VideoModel> videoModelArrayList) {
        this.context = context;
        this.videoModelArrayList = videoModelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(videoModelArrayList.get(position).getVideoTitle().length() > 10) {
            String text = videoModelArrayList.get(position).getVideoTitle().substring(11);
            holder.recyclerviewVideoItemTitleMTV.setText(text);
        }else{
            try {
                holder.recyclerviewVideoItemTitleMTV.setText(videoModelArrayList.get(position).getVideoTitle());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        try {
            holder.recyclerviewVideoItemDurationMTV.setText(videoModelArrayList.get(position).getVideoDuration());
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            if (videoModelArrayList.get(position).getVideoUri().getPath() != null) {
                holder.recyclerviewVideoItemThumbnailIV
                        .setImageBitmap(ThumbnailUtils.createVideoThumbnail(videoModelArrayList.get(position).getVideoUri().getPath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND));

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(videoModelArrayList.get(position).getVideoUri(), "video/*");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoModelArrayList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int pos, View v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView recyclerviewVideoItemThumbnailIV;
        final MaterialTextView recyclerviewVideoItemTitleMTV;
        final MaterialTextView recyclerviewVideoItemDurationMTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerviewVideoItemThumbnailIV = itemView.findViewById(R.id.recyclerview_video_item_thumbnailIV);
            recyclerviewVideoItemTitleMTV = itemView.findViewById(R.id.recyclerview_video_item_titleMTV);
            recyclerviewVideoItemDurationMTV = itemView.findViewById(R.id.recyclerview_video_item_durationMTV);

            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(getAdapterPosition(), v));
        }
    }
}
