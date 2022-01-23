package com.sp.laceaid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

import com.sp.laceaid.login.screen.LoginOptionsActivity;

public class SplashActivity extends AppCompatActivity {
    //private final int DURATION = (int)(1000 * 2);
    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   // hide status bar
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.secondary));  // change status bar colour

        /*
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, Log_in_Activity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, DURATION);
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView = findViewById(R.id.videoView);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.new_splash);
        videoView.setVideoURI(video);

        // this function will run when the video ends
        videoView.setOnCompletionListener(mp -> {
            videoView.setVisibility(View.GONE);    // make the videoView invisible to hide the hideous white screen at the end
            startActivity(new Intent(SplashActivity.this, LoginOptionsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        videoView.setZOrderOnTop(true); // hide initial black screen
        videoView.start();    // start the video
    }
}