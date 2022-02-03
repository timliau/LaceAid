package com.sp.laceaid.uiNavDrawer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.os.Bundle;

import com.sp.laceaid.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navdrawer_activity_about);
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}