package com.martiandeveloper.twitterdownloader.model;

import android.net.Uri;

public class ImageModel {
    private String imageTitle;
    private Uri imageUri;

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}
