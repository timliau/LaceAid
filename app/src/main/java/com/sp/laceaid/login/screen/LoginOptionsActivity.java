package com.sp.laceaid.login.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.sp.laceaid.R;

public class LoginOptionsActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_login_options);

        Button loginOption = findViewById(R.id.button_login_option);
        Button signupOption = findViewById(R.id.button_signup_option);

        loginOption.setOnClickListener(this);
        signupOption.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.button_login_option:
                intent = new Intent(this, LoginActivity.class);
                // to finish() LoginOptionsActivity when user successfully log in
                intent.putExtra("finisher", new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        LoginOptionsActivity.this.finish();
                    }
                });
                intent.putExtra("LOGINOPTIONEXIST", true);
                startActivity(intent);
                break;
            case R.id.button_signup_option:
                intent = new Intent(this, SignupActivity.class);
                intent.putExtra("finisher", new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        LoginOptionsActivity.this.finish();
                    }
                });
                intent.putExtra("LOGINOPTIONEXIST", true);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // to prevent splash from showing again when back button is pressed
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }

}