package com.sp.laceaid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgorActivity extends AppCompatActivity {

    private EditText resetEmail;
    private Button send, backLoginBtn;
    private TextView login, linkResend;
    private ImageView arrow;
    private LinearLayout mainLayout, secondLayout, pw_error_msg, progressBar;
    private ProgressBar progressBar3;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgor);

        // hooks
        arrow = findViewById(R.id.arrow1);
        send = findViewById(R.id.send_button);
        login = findViewById(R.id.link_login_back);
        resetEmail = findViewById(R.id.reset_email);
        mainLayout = findViewById(R.id.forgor_main);
        secondLayout = findViewById(R.id.forgor_second);
        pw_error_msg = findViewById(R.id.pw_error_msg);
        progressBar = findViewById(R.id.progressBar2);
        backLoginBtn = findViewById(R.id.login_button_forgor);
        linkResend = findViewById(R.id.link_resend);
        progressBar3 = findViewById(R.id.progressBar3);

        mAuth = FirebaseAuth.getInstance();

        // get intent data
        String emailStr = getIntent().getStringExtra("EMAIL");
        resetEmail.setText(emailStr);

        arrow.setOnClickListener(v -> finish());

        login.setOnClickListener(v-> finish());

        backLoginBtn.setOnClickListener(v-> finish());

        send.setOnClickListener(v->{
            pw_error_msg.setVisibility(View.GONE);
            resetPassword();
        });

        linkResend.setOnClickListener(v->{
            String emailStr2 = resetEmail.getText().toString().trim();

            progressBar3.setVisibility(View.VISIBLE);

            mAuth.sendPasswordResetEmail(emailStr2).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    progressBar3.setVisibility(View.GONE);
                    Toast.makeText(ForgorActivity.this, "Email sent successfully, check your mailbox again", Toast.LENGTH_LONG).show();
                } else {
                    progressBar3.setVisibility(View.GONE);
                    Toast.makeText(ForgorActivity.this, "Invalid email", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void resetPassword() {
        String emailStr = resetEmail.getText().toString().trim();

        if(emailStr.isEmpty()) {
            resetEmail.setError("Email is required");
            resetEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            resetEmail.setError("Please provide a valid email");
            resetEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(emailStr).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.GONE);
                    secondLayout.setVisibility(View.VISIBLE);
                } else {
                    pw_error_msg.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}