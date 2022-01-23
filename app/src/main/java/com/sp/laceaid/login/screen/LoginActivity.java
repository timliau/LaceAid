package com.sp.laceaid.login.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sp.laceaid.HomeActivity;
import com.sp.laceaid.R;

public class LoginActivity extends AppCompatActivity{
    private Button login;
    private EditText email, password;
    private TextView forgotPw, signup;
    private ImageView arrow;
    private ProgressBar progressBar;
    private LinearLayout warningBanner;
    private boolean loginOptionExist;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // hooks
        arrow = findViewById(R.id.arrow1);
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        forgotPw = findViewById(R.id.link_forgotpw);
        signup = findViewById(R.id.link_login_back);
        login = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progressBar);
        warningBanner = findViewById(R.id.warning_banner);

        mAuth = FirebaseAuth.getInstance();

        // get intent data
        String emailStr = getIntent().getStringExtra("EMAIL");
        email.setText(emailStr);

        // check if this activity is started from loginOption
        loginOptionExist = getIntent().getBooleanExtra("LOGINOPTIONEXIST", false);

        arrow.setOnClickListener(v -> {
            if(loginOptionExist) finish();
            else {
                startActivity(new Intent(this, LoginOptionsActivity.class));
                finish();
            }
        });

        signup.setOnClickListener(v->{
            Intent intent = new Intent(this, SignupActivity.class);
            intent.putExtra("EMAIL", email.getText().toString().trim());
            startActivity(intent);
            finish();
        });

        login.setOnClickListener(v->{
            warningBanner.setVisibility(View.GONE);
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            loginUser();
        });

        forgotPw.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgorActivity.class);
            intent.putExtra("EMAIL", email.getText().toString().trim());
            startActivity(intent);
        });


    }

    private void loginUser() {
        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        if(emailStr.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email.setError("Please provide a valid email");
            email.requestFocus();
            return;
        }
        if(passwordStr.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return;
        }
        if(password.length() < 6) {
            password.setError("Min Password length should be 6 character");
            password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    // to finish() LoginOptionsActivity -> so onBackPressed doesn't go back to login activity
                    if(loginOptionExist) ((ResultReceiver)getIntent().getParcelableExtra("finisher")).send(1, new Bundle());

                    // check if the email is verified
                    if(user.isEmailVerified()){
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    } else {
                        user.sendEmailVerification();
                        Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                        intent.putExtra("IS_FROM_LOGIN", true);
                        intent.putExtra("EMAIL", emailStr);
                        intent.putExtra("PASSWORD", passwordStr);
                        startActivity(intent);
                    }
                    finish();

                } else  {
                    progressBar.setVisibility(View.GONE);
                    warningBanner.setVisibility(View.VISIBLE);
                    email.requestFocus();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(loginOptionExist) finish();
        else {
            startActivity(new Intent(this, LoginOptionsActivity.class));
            finish();
        }
    }
}