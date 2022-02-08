package com.sp.laceaid.uiNavDrawer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sp.laceaid.R;

public class SettingFragment extends Fragment {

    // instead of setting change to feedback

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.nav_drawer_fragment_setting, container, false);
    }

}