package com.sp.laceaid.uiNavDrawer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.sp.laceaid.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navdrawer_activity_profile);
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}