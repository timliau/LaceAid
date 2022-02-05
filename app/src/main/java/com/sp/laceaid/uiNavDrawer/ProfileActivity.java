package com.sp.laceaid.uiNavDrawer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.laceaid.MainActivity;
import com.sp.laceaid.R;
import com.sp.laceaid.User;
import com.sp.laceaid.login.screen.LoginOptionsActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageView arrow;
    private TextInputEditText firstName, lastName, newPw, currentPw, confirmPw;
    private TextInputLayout ly_firstName, ly_lastName, ly_newPw, ly_currentPw, ly_confirmPw;
    private TextView saveTV;
    private CardView save;
    private ProgressBar progressBar;

    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navdrawer_activity_profile);

        // hooks
        arrow = findViewById(R.id.arrow1);
        save = findViewById(R.id.btn_save);
        progressBar = findViewById(R.id.profileProgressBar);
        saveTV = findViewById(R.id.tv_save);

        ly_firstName = findViewById(R.id.txtly_first);
        ly_lastName = findViewById(R.id.txtly_last);
        ly_newPw = findViewById(R.id.txtly_new);
        ly_currentPw = findViewById(R.id.txtly_current);
        ly_confirmPw = findViewById(R.id.txtly_confirm);

        firstName = findViewById(R.id.txt_first);
        lastName = findViewById(R.id.txt_last);
        currentPw = findViewById(R.id.txt_current);
        newPw = findViewById(R.id.txt_new);
        confirmPw = findViewById(R.id.txt_confirm);


        // grab data from firebase realtime db
        user = FirebaseAuth.getInstance().getCurrentUser();
        // need to add the url as the default location is USA not SEA
        databaseReference = FirebaseDatabase.getInstance("https://lace-aid-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        userID = user.getUid();

        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userprofile = snapshot.getValue(User.class);

                if(userprofile != null) {
                    String firstNameStr = userprofile.getFirstName();
                    String lastNameStr = userprofile.getLastName();
                    firstName.setText(firstNameStr);
                    lastName.setText(lastNameStr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Cannot update profile", Toast.LENGTH_LONG).show();
            }
        });

        arrow.setOnClickListener(v -> finish());

        save.setOnClickListener(v->{
            saveTV.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            updateUser();

        });
    }

    private void updateUser() {
        String firstNameStr = firstName.getText().toString().trim();
        String lastNameStr = lastName.getText().toString().trim();
        String currentPwStr = currentPw.getText().toString();
        String newPwStr = newPw.getText().toString();
        String confirmPwStr = confirmPw.getText().toString();
        boolean needDelay = true;
        ly_firstName.setError(null);
        ly_lastName.setError(null);
        ly_currentPw.setError(null);
        ly_newPw.setError(null);
        ly_confirmPw.setError(null);

        if(firstNameStr.isEmpty()) {
            ly_firstName.setError("*Required");
            ly_firstName.requestFocus();
            return;
        }
        if(lastNameStr.isEmpty()) {
            ly_lastName.setError("*Required");
            ly_lastName.requestFocus();
            return;
        }

        if(!currentPwStr.isEmpty()){
            if(newPwStr.isEmpty()){
                ly_newPw.setError("*Required");
                ly_newPw.requestFocus();
                return;
            }
            if(confirmPwStr.isEmpty()){
                ly_confirmPw.setError("*Required");
                ly_confirmPw.requestFocus();
                return;
            }
            if(newPwStr.length() < 6){
                ly_newPw.setError("*Min 6 characters");
                ly_newPw.requestFocus();
                return;
            }
            if(!newPwStr.equals(confirmPwStr)){
                ly_confirmPw.setError("*Password does not match");
                ly_confirmPw.requestFocus();
                return;
            }
            if(newPwStr.equals(currentPwStr)){
                ly_newPw.setError("*Password cannot be the same as current one");
                ly_newPw.requestFocus();
                return;
            }
            updatePassword(currentPwStr, newPwStr);
            needDelay = false;
        }

        // update value in rtdb (for names only)
        databaseReference.child(userID).child("firstName").setValue(firstNameStr);
        databaseReference.child(userID).child("lastName").setValue(lastNameStr);

        // if not updating pw, put in some delay so the loading animation appear as it's too fast
        if(needDelay) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    saveTV.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                }
            }, 500);
        }


    }

    private void updatePassword(String currentPwStr, String newPwStr) {
        // before changing pw, re-authenticate the user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), currentPwStr);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // successfully authenticated, begin update
                        user.updatePassword(newPwStr)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                        currentPw.setText("");
                                        newPw.setText("");
                                        confirmPw.setText("");
                                        saveTV.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        saveTV.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        saveTV.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}