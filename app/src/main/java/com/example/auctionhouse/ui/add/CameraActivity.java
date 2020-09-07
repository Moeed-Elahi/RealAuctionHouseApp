package com.example.auctionhouse.ui.add;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {
    private final static String CAPTURED_PHOTO_PATH = "photoPath";
    private final static String CAPTURED_PHOTO_URI = "imageUri";

    private String photoPath = null;
    private Uri imageUri = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (photoPath != null) {
            outState.putString(CAPTURED_PHOTO_PATH, photoPath);
        }
        if (imageUri != null) {
            outState.putString(CAPTURED_PHOTO_URI, imageUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH)) {
            photoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH);
        }
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_URI)) {
            imageUri = Uri.parse(savedInstanceState.getString(CAPTURED_PHOTO_URI));
        }
        super.onRestoreInstanceState(savedInstanceState);

    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}
