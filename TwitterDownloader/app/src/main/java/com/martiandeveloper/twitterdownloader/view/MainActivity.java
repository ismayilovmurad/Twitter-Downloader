package com.martiandeveloper.twitterdownloader.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esafirm.rxdownloader.RxDownloader;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.martiandeveloper.twitterdownloader.R;
import com.martiandeveloper.twitterdownloader.adapter.ImageAdapter;
import com.martiandeveloper.twitterdownloader.adapter.VideoAdapter;
import com.martiandeveloper.twitterdownloader.model.ImageModel;
import com.martiandeveloper.twitterdownloader.model.VideoModel;
import com.martiandeveloper.twitterdownloader.tools.Constant;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialToolbar activityMainMainTB;
    private TextInputEditText activityMainTweetUrlTIE;
    private MaterialButton activityMainPasteUrlMBTN, activityMainDownloadMBTN;
    private ProgressDialog progressDialog;
    private RecyclerView activityMainVideoRV;
    private RecyclerView activityMainImageRV;
    public static ArrayList<VideoModel> videoModelArrayList;
    public static ArrayList<ImageModel> imageModelArrayList;
    private RadioGroup activityMainMainRG;
    private VideoAdapter videoAdapter;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_main);
        initViews();
        setToolbar();
        setProgressDialog();
        setListeners();
        setTwitterConfig();
        setRecyclerView();
        checkPermissions();
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                detectIntent();
                getVideosFromStorage();
                getImagesFromStorage();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            detectIntent();
            getVideosFromStorage();
            getImagesFromStorage();
        }
    }

    private void initViews() {
        activityMainMainTB = findViewById(R.id.activity_main_mainTB);
        activityMainTweetUrlTIE = findViewById(R.id.activity_main_tweetUrlTIE);
        activityMainPasteUrlMBTN = findViewById(R.id.activity_main_pasteUrlMBTN);
        activityMainDownloadMBTN = findViewById(R.id.activity_main_downloadMBTN);
        activityMainVideoRV = findViewById(R.id.activity_main_videoRV);
        activityMainImageRV = findViewById(R.id.activity_main_imageRV);
        videoModelArrayList = new ArrayList<>();
        imageModelArrayList = new ArrayList<>();
        activityMainMainRG = findViewById(R.id.activity_main_mainRG);
    }

    private void setToolbar() {
        setSupportActionBar(activityMainMainTB);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setTwitterConfig() {
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(Constant.TWITTER_KEY, Constant.TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    private void setProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.fetching_video));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    private void setListeners() {
        activityMainPasteUrlMBTN.setOnClickListener(this);
        activityMainDownloadMBTN.setOnClickListener(this);
        activityMainMainRG.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.activity_main_videoGifRB:
                    activityMainVideoRV.setVisibility(View.VISIBLE);
                    activityMainImageRV.setVisibility(View.GONE);
                    break;
                case R.id.activity_main_imageRB:
                    activityMainImageRV.setVisibility(View.VISIBLE);
                    activityMainVideoRV.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void setRecyclerView() {
        activityMainVideoRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        activityMainVideoRV.setItemAnimator(new DefaultItemAnimator());
        videoAdapter = new VideoAdapter(this, videoModelArrayList);
        activityMainVideoRV.setAdapter(videoAdapter);

        activityMainImageRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        activityMainImageRV.setItemAnimator(new DefaultItemAnimator());
        imageAdapter = new ImageAdapter(this, imageModelArrayList);
        activityMainImageRV.setAdapter(imageAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_pasteUrlMBTN:
                paste();
                break;
            case R.id.activity_main_downloadMBTN:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    download();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.please_grant_the_permission_and_try_again), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void paste() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            assert clipboardManager != null;
            assert clipboardManager.getPrimaryClip() != null;
            CharSequence textToPaste = clipboardManager.getPrimaryClip().getItemAt(0).getText();
            activityMainTweetUrlTIE.setText(textToPaste);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void download() {
        String fileName;

        if (activityMainTweetUrlTIE.getText() != null) {
            if (activityMainTweetUrlTIE.getText().length() > 0) {
                if (activityMainTweetUrlTIE.getText().toString().contains("twitter.com/")) {
                    Long id = getTweetId(activityMainTweetUrlTIE.getText().toString());
                    fileName = String.valueOf(id);

                    if (id != null) {
                        getTweet(id, fileName);
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.the_url_is_not_contains_any_video_or_gif_file), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.the_url_is_not_contains_any_video_or_gif_file), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.please_enter_url), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.please_enter_url), Toast.LENGTH_SHORT).show();
        }
    }

    private void getTweet(Long id, String fileName) {
        progressDialog.show();

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<Tweet> tweetCall = statusesService.show(id, null, null, null);
        tweetCall.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {

                if (result.data.extendedEntities == null && result.data.entities.media == null) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.the_url_is_not_contains_any_video_or_gif_file), Toast.LENGTH_SHORT).show();
                } else if (result.data.extendedEntities != null && result.data.extendedEntities.media.size() > 0) {
                    if (!(result.data.extendedEntities.media.get(0).type).equals("video") &&
                            !(result.data.extendedEntities.media.get(0).type).equals("animated_gif") && !(result.data.extendedEntities.media.get(0).type).equals("photo")) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.the_url_is_not_contains_any_video_or_gif_file), Toast.LENGTH_SHORT).show();
                    } else {
                        String filename = fileName;
                        String url;

                        if ((result.data.extendedEntities.media.get(0).type).equals("video") ||
                                (result.data.extendedEntities.media.get(0).type).equals("animated_gif")) {

                            filename = filename + ".mp4";

                            try {
                                url = result.data.extendedEntities.media.get(0).videoInfo.variants.get(0).url;
                                downloadVideo(url, filename);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            /*int i = 0;

                            try {
                                while (result.data.extendedEntities.media.get(0).videoInfo.variants.get(i) != null) {
                                    url = result.data.extendedEntities.media.get(0).videoInfo.variants.get(i).url;
                                    downloadVideo(url, filename);
                                    i++;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }*/

                            /*while (!url.contains(".mp4")) {
                                try {
                                    if (result.data.extendedEntities.media.get(0).videoInfo.variants.get(i) != null) {
                                        url = result.data.extendedEntities.media.get(0).videoInfo.variants.get(i).url;
                                        Log.d("TAG", "url2 is " + url);
                                        i += 1;
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    downloadVideo(url, filename);
                                }
                            }
                            downloadVideo(url, filename);*/
                        } else if ((result.data.extendedEntities.media.get(0).type).equals("photo")) {
                            filename = filename + ".jpg";

                            try {
                                url = result.data.extendedEntities.media.get(0).mediaUrlHttps;
                                downloadImage(url, filename);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            /*int i = 0;

                            try {
                                while (result.data.extendedEntities.media.get(i) != null) {
                                    url = result.data.extendedEntities.media.get(i).mediaUrlHttps;
                                    downloadImage(url, filename);
                                    i++;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }*/
                            /*url = result.data.extendedEntities.media.get(i).mediaUrlHttps;

                            while (!url.contains(".jpg")) {
                                try {
                                    if (result.data.extendedEntities.media.get(i).mediaUrlHttps != null) {
                                        url = result.data.extendedEntities.media.get(i).mediaUrlHttps;
                                        Log.d("TAG", "url2 is " + url);
                                        i += 1;
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    downloadImage(url, filename);
                                }
                            }
                            downloadImage(url, filename);*/
                        }
                    }
                }

            }

            @Override
            public void failure(TwitterException exception) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, getResources().getString(R.string.request_failed_please_check_your_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadVideo(String url, String filename) {
        try {
            filename = "td_vid" + filename;
            RxDownloader rxDownloader = new RxDownloader(MainActivity.this);
            rxDownloader.download(url, filename, "video/*", true)
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(String s) {
                        }

                        @Override
                        public void onError(Throwable e) {
                            progressDialog.hide();
                            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.download_completed), Toast.LENGTH_SHORT).show();
                            getVideosFromStorage();
                        }
                    });

            progressDialog.hide();
            Toast.makeText(this, getResources().getString(R.string.download_started), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            progressDialog.hide();
            Toast.makeText(this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
        }
    }

    private void downloadImage(String url, String filename) {
        try {
            filename = "td_img" + filename;
            RxDownloader rxDownloader = new RxDownloader(MainActivity.this);
            rxDownloader.download(url, filename, "image/jpg", true)
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(String s) {
                        }

                        @Override
                        public void onError(Throwable e) {
                            progressDialog.hide();
                            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.download_completed), Toast.LENGTH_SHORT).show();
                            getImagesFromStorage();
                        }
                    });

            progressDialog.hide();
            Toast.makeText(this, getResources().getString(R.string.download_started), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            progressDialog.hide();
            Toast.makeText(this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
        }
    }

    private Long getTweetId(String s) {
        try {
            String[] split = s.split("\\/");
            String id = split[5].split("\\?")[0];
            return Long.parseLong(id);
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.the_url_is_not_contains_any_video_or_gif_file), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public void detectIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {

            if ("text/plain".equals(type)) {
                handleSharedText(intent);
            }
        }
    }

    private void handleSharedText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            try {
                if (sharedText.split("\\ ").length > 1) {
                    activityMainTweetUrlTIE.setText(sharedText.split("\\ ")[4]);

                } else {
                    activityMainTweetUrlTIE.setText(sharedText.split("\\ ")[0]);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void getVideosFromStorage() {
        videoModelArrayList.clear();

        try {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            @SuppressLint("Recycle") Cursor cursor = contentResolver.query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {

                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    @SuppressLint("InlinedApi") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    if (title.startsWith("td_vid")) {
                        VideoModel videoModel = new VideoModel();
                        videoModel.setVideoTitle(title);
                        videoModel.setVideoUri(Uri.parse(data));
                        videoModel.setVideoDuration(timeConversion(Long.parseLong(duration)));
                        videoModelArrayList.add(videoModel);
                    }

                } while (cursor.moveToNext());
            }

            VideoAdapter videoAdapter = new VideoAdapter(this, videoModelArrayList);
            activityMainVideoRV.swapAdapter(videoAdapter, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getImagesFromStorage() {
        imageModelArrayList.clear();

        try {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            @SuppressLint("Recycle") Cursor cursor = contentResolver.query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {

                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    if (title.startsWith("td_img")) {
                        ImageModel imageModel = new ImageModel();
                        imageModel.setImageTitle(title);
                        imageModel.setImageUri(Uri.parse(data));
                        imageModelArrayList.add(imageModel);
                    }

                } while (cursor.moveToNext());
            }

            ImageAdapter imageAdapter = new ImageAdapter(this, imageModelArrayList);
            activityMainImageRV.swapAdapter(imageAdapter, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    public String timeConversion(long value) {
        String videoTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            videoTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            videoTime = String.format("%02d:%02d", mns, scs);
        }
        return videoTime;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    detectIntent();
                    getVideosFromStorage();
                    getImagesFromStorage();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}