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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sp.laceaid.R;

public class arFragment extends Fragment {

    private Button onCamera;

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

        onCamera.setOnClickListener(v->{
            // check camera permission
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            } else startAR();   // run if granted
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

}