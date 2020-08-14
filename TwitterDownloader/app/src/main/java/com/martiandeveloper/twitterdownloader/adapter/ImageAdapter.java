package com.martiandeveloper.twitterdownloader.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;
import com.martiandeveloper.twitterdownloader.R;
import com.martiandeveloper.twitterdownloader.model.ImageModel;
import com.martiandeveloper.twitterdownloader.model.VideoModel;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.EXTRA_STREAM;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ImageModel> imageModelArrayList;
    public OnItemClickListener onItemClickListener;

    public ImageAdapter(Context context, ArrayList<ImageModel> imageModelArrayList) {
        this.context = context;
        this.imageModelArrayList = imageModelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(imageModelArrayList.get(position).getImageTitle().length() > 10) {
            String text = imageModelArrayList.get(position).getImageTitle().substring(11);
            holder.recyclerviewImageItemTitlenMTV.setText(text);
        }else{
            try {
                holder.recyclerviewImageItemTitlenMTV.setText(imageModelArrayList.get(position).getImageTitle());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            Glide.with(context)
                    .load(imageModelArrayList.get(position).getImageUri().getPath())
                    .into(holder.recyclerviewImageItemThumbnailIV);
        }catch (Exception e){
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(Uri.parse(imageModelArrayList.get(position).getImageUri().getPath()), "image/jpg");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageModelArrayList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int pos, View v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView recyclerviewImageItemThumbnailIV;
        final MaterialTextView recyclerviewImageItemTitlenMTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerviewImageItemThumbnailIV = itemView.findViewById(R.id.recyclerview_image_item_thumbnailIV);
            recyclerviewImageItemTitlenMTV = itemView.findViewById(R.id.recyclerview_image_item_titleMTV);

            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(getAdapterPosition(), v));
        }
    }
}
