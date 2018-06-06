package com.example.amarjeet.camera2api;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

/**
 * Created by Amarjeet on 05-06-2018.
 */

public class SecActivity extends AppCompatActivity {
    private static final String TAG = "OtherActivity";
    private ImageView imageView;
    private VideoView mvideoView;
    private String imguri, viduri;
    private RelativeLayout mRelativeLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sec_activity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        imageView = (ImageView)findViewById(R.id.img_view);

        Log.d(TAG, "Reached OtherActivity");

        imguri = getIntent().getExtras().getString("ImageUri");

        //transition
        if(Build.VERSION.SDK_INT > 21) {
            Fade fade = new Fade();
            fade.setDuration(500);
            getWindow().setEnterTransition(fade);

            Fade fadeexit = new Fade();
            fadeexit.setDuration(1000);
            getWindow().setExitTransition(fade);
        }

        if(imguri!=null) {
            Log.d(TAG, imguri);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(BitmapFactory.decodeFile(imguri));
        }

        mvideoView = (VideoView)findViewById(R.id.videoView);
        mRelativeLayout = (RelativeLayout)findViewById(R.id.rellayout);

        viduri = getIntent().getExtras().getString("VideoUri");
        if(viduri!=null) {
            Log.d(TAG, viduri);
            mRelativeLayout.setVisibility(View.VISIBLE);
            showVideo(viduri,mvideoView);
        }
    }



    private void showVideo(String videoUri, final VideoView videoView) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d(TAG, metrics.heightPixels + "     " + metrics.widthPixels);
        android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) videoView.getLayoutParams();
        params.width =  metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        videoView.setLayoutParams(params);

        videoView.setVisibility(View.VISIBLE);
        try {
            videoView.setMediaController(null);
            videoView.setVideoURI(Uri.parse(videoUri));
        } catch (Exception e){
            e.printStackTrace();
        }
        videoView.requestFocus();
        //videoView.setZOrderOnTop(true);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {

                videoView.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });
    }
}
