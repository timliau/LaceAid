package com.sp.laceaid.uiNavDrawer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.sp.laceaid.R;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navdrawer_activity_setting);
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}