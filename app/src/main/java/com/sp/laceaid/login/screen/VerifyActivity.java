package com.sp.laceaid.login.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sp.laceaid.HomeActivity;
import com.sp.laceaid.R;

public class VerifyActivity extends AppCompatActivity {

    private TextView chooseEmail, verificationEmail;
    private boolean isFromLogin;
    private ImageView arrow;
    private String emailStr, passwordStr;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        // hooks
        chooseEmail = findViewById(R.id.tv_chooseEmail);
        arrow = findViewById(R.id.arrow1);
        verificationEmail = findViewById(R.id.tv_verificationEmail);

        mAuth = FirebaseAuth.getInstance();

        // get intent
        isFromLogin = getIntent().getBooleanExtra("IS_FROM_LOGIN", true);
        emailStr = getIntent().getStringExtra("EMAIL");
        passwordStr = getIntent().getStringExtra("PASSWORD");

        String newText = verificationEmail.getText().toString() + " to " + emailStr;
        verificationEmail.setText(newText);

        chooseEmail.setOnClickListener(v->{
            if(isFromLogin) startActivity(new Intent(this, LoginActivity.class));
            else startActivity(new Intent(this, SignupActivity.class));
            finish();
        });

        arrow.setOnClickListener(v->{
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                // check if the email is verified
                if(user.isEmailVerified()){
                    startActivity(new Intent(VerifyActivity.this, HomeActivity.class));
                    finish();
                }

            }
        });
    }
}