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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.sp.laceaid.R;
import com.sp.laceaid.User;

public class SignupActivity extends AppCompatActivity {

    private Button signup;
    private ImageView arrow;
    private TextView login;
    private EditText name, email, password;
    private RelativeLayout progress_bar;
    private LinearLayout failed_warning;
    private boolean loginOptionExist;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_signup);

        // hooks
        arrow = findViewById(R.id.arrow1);
        login = findViewById(R.id.link_login);
        signup = findViewById(R.id.login_button);
        name = findViewById(R.id.signup_name);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.login_password);
        progress_bar = findViewById(R.id.progress_bar);
        failed_warning = findViewById(R.id.warning_banner);

        mAuth = FirebaseAuth.getInstance();

        // get intent data
        String emailStr = getIntent().getStringExtra("EMAIL");
        email.setText(emailStr);

        // check if this activity is started from loginOption
        loginOptionExist = getIntent().getBooleanExtra("LOGINOPTIONEXIST", false);

        arrow.setOnClickListener(v->{
            if(loginOptionExist) finish();
            else {
                startActivity(new Intent(this, LoginOptionsActivity.class));
                finish();
            }
        });

        login.setOnClickListener(v->{
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("EMAIL", email.getText().toString().trim());
            startActivity(intent);
            finish();
        });

        signup.setOnClickListener(v->{
            failed_warning.setVisibility(View.GONE);
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            // inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            // use this method in instances where there might not be focus (e.g. onPause(), etc
            registerUser();
        });
    }

    private void registerUser() {
        String emailStr = email.getText().toString().trim();
        String nameStr = name.getText().toString();
        String passwordStr = password.getText().toString().trim();

        if(nameStr.isEmpty()) {
            name.setError("Name is required");
            name.requestFocus();
            return;
        }
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

        progress_bar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(task -> {

                       if(task.isSuccessful()) {
                           User user = new User(nameStr, emailStr);

                           FirebaseDatabase.getInstance("https://lace-aid-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users")
                                   .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                   .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if(task.isSuccessful()) {

                                       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                       user.sendEmailVerification();

                                       // to finish() LoginOptionsActivity -> so onBackPressed doesn't go back
                                       if(loginOptionExist) ((ResultReceiver)getIntent().getParcelableExtra("finisher")).send(1, new Bundle());

                                       progress_bar.setVisibility(View.GONE);
                                       Intent intent = new Intent(SignupActivity.this, VerifyActivity.class);
                                       intent.putExtra("IS_FROM_LOGIN", false);
                                       intent.putExtra("EMAIL", emailStr);
                                       intent.putExtra("PASSWORD", passwordStr);
                                       startActivity(intent);
                                       finish();
                                   }
                                   else {
                                       progress_bar.setVisibility(View.GONE);
                                       failed_warning.setVisibility(View.VISIBLE);
                                   }
                               }
                           });
                       }
                       else {
                           progress_bar.setVisibility(View.GONE);
                           failed_warning.setVisibility(View.VISIBLE);
                       }
                   });


    }

    @Override
    public void onBackPressed() {
        // if it's not from VerifyActivity do finish() otherwise have to start new activity as it has been previously killed
        if(loginOptionExist) finish();
        else {
            startActivity(new Intent(this, LoginOptionsActivity.class));
            finish();
        }
    }
}