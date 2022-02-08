package com.sp.laceaid.uiBottomNavBar.ar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.laceaid.R;

public class arFragment extends Fragment {

    private Button onCamera;
    private ImageView node, map;
    private TextView meas_cm, meas_us, meas_uk, meas_eur;

    // firebase
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String userID;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        onCamera = getView().findViewById(R.id.onCamera);
        node = getView().findViewById(R.id.iv_nodes);
        map = getView().findViewById(R.id.iv_map);
        meas_cm = getView().findViewById(R.id.meas_cm);
        meas_eur = getView().findViewById(R.id.meas_eur);
        meas_uk = getView().findViewById(R.id.meas_uk);
        meas_us = getView().findViewById(R.id.meas_us);

        // grab data from firebase realtime db
        user = FirebaseAuth.getInstance().getCurrentUser();
        // need to add the url as the default location is USA not SEA
        databaseReference = FirebaseDatabase.getInstance("https://lace-aid-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        userID = user.getUid();

        onCamera.setOnClickListener(v->{
            // check camera permission
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            } else startAR();   // run if granted
        });

        map.setOnClickListener(v->{
            node.setVisibility(View.VISIBLE);
            node.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
        });
    }

    // when permission is granted do smt
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 100) {
            if (grantResults.length>0) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startAR();
                } else
                    Toast.makeText(getActivity(),"Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startAR(){
        //Toast.makeText(getActivity(),"AR CAMERA", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), MeasureActivity.class);
        startActivity(intent);
    }

    public void checkSize(double footLength) {
        int us = 4, eur = 37, uk = 3;
        double min = 22.7, max = 31.0;

        int k = (int) Math.round((footLength - min) / 0.65);

        if(footLength >= min && footLength <= max) {
            meas_cm.setText("" + footLength);
            meas_us.setText(k + us + "");
            meas_eur.setText(k + eur + "");
            meas_uk.setText(k + uk + "");
        } else {
            meas_cm.setText("" + footLength);
            meas_us.setText("?");
            meas_eur.setText("?");
            meas_uk.setText("?");
        }
    }

    // can use onStart too
    @Override
    public void onResume() {
        super.onResume();

        // get saved measurement from rtdb
        databaseReference.child(userID).child("footLength").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // check if length value exist in rtdb
                try{
                    double footLength = (Double) snapshot.getValue();

                    checkSize(footLength);

                }catch (Exception ignored) {

                };
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Cannot get measurement data", Toast.LENGTH_LONG).show();
            }
        });
    }
}