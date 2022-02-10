package com.sp.laceaid.uiBottomNavBar.home;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.laceaid.Constants;
import com.sp.laceaid.R;
import com.sp.laceaid.User;
import com.sp.laceaid.login.screen.LoginOptionsActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link homeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class homeFragment extends Fragment {

    // firebase
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String userID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String address = "", name = "";
    public boolean connected = false;

    private MaterialCardView connect;
    private CardView tighten, tighten2;
    private TextView homeName;
    private ImageView Find, shoeicon;

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    BluetoothSocket mSocket;

    public homeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment homeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static homeFragment newInstance(String param1, String param2) {
        homeFragment fragment = new homeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        Toast.makeText(getActivity(), "" + connected, Toast.LENGTH_SHORT).show();
//        if (connected) {
//            connect.setCardBackgroundColor(0xffe1f7dd);
//            connect.setStrokeColor(0xffA1A1A1);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onCreate(savedInstanceState);

        // grab data from firebase realtime db
        user = FirebaseAuth.getInstance().getCurrentUser();
        // need to add the url as the default location is USA not SEA
        databaseReference = FirebaseDatabase.getInstance("https://lace-aid-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        userID = user.getUid();
        homeName = getView().findViewById(R.id.homeName);

        Find = getView().findViewById(R.id.Find);
        shoeicon = getView().findViewById(R.id.shoeicon);
        connect = getView().findViewById(R.id.connect);
        tighten = getView().findViewById(R.id.tighten);
        tighten2 = getView().findViewById(R.id.tighten2);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);

        if(sharedPreferences.getBoolean(Constants.IS_BT_CONNECTED, false)) {
            connect.setCardBackgroundColor(0xffe1f7dd);
            connect.setStrokeColor(0xffA1A1A1);
            Find.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary), android.graphics.PorterDuff.Mode.SRC_IN);
            tighten.setCardBackgroundColor(0xffa8e79b);
            tighten2.setCardBackgroundColor(0xff6BD755);
            shoeicon.setColorFilter(ContextCompat.getColor(getContext(), R.color.dark_grey), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // update name and email in nav drawer when user load (first time + only once)
        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userprofile = snapshot.getValue(User.class);

                if(userprofile != null) {
                    String name = userprofile.getFirstName();
                    String email = userprofile.getEmail();
                    homeName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Cannot update profile", Toast.LENGTH_LONG).show();
            }
        });


        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            Toast.makeText(getActivity(), "Your device does not support bluetooth", Toast.LENGTH_SHORT).show();
        }
        else {
            if (!myBluetooth.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    bluetooth_connect_device();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "error: ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tighten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    ConnectedThread mConnectedThread = new ConnectedThread(mSocket);
                    mConnectedThread.start();
                    mConnectedThread.write(("t").getBytes());
                    Toast.makeText(getActivity(), "Tightened", Toast.LENGTH_SHORT).show();
                    connect.setCardBackgroundColor(0xffe1f7dd);
                    connect.setStrokeColor(0xffA1A1A1);
                    Find.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary), android.graphics.PorterDuff.Mode.SRC_IN);
                    tighten.setCardBackgroundColor(0xffa8e79b);
                    tighten2.setCardBackgroundColor(0xff6BD755);
                    shoeicon.setColorFilter(ContextCompat.getColor(getContext(), R.color.dark_grey), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        });
    }


    private void bluetooth_connect_device() throws IOException {
        try {
            Toast.makeText(getActivity(), "Connecting", Toast.LENGTH_SHORT).show();
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
            address = myBluetooth.getAddress();
            pairedDevices = myBluetooth.getBondedDevices();
            if (pairedDevices.size()>0) {
                for(BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().equals("LaceAid v1")){
                        address = bt.getAddress().toString();
                        name = bt.getName().toString();
                    }
                }
            }
        }
        catch (Exception we) {
            Toast.makeText(getActivity(),"Error: " + we ,Toast.LENGTH_SHORT).show();
        }
        if (name.isEmpty()) {
            Toast.makeText(getActivity(),"Connection failed",Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // store the switch state
            editor.putBoolean(Constants.IS_BT_CONNECTED, false);

            // apply changes
            editor.apply();
        }
        else {
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
//            BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
//            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
//            btSocket.connect();
            BluetoothDevice mDevice = myBluetooth.getRemoteDevice(address);
            ConnectThread mConnectThread = new ConnectThread(mDevice);
            mConnectThread.start();
            connected = true;
            Toast.makeText(getActivity(),"Connected",Toast.LENGTH_SHORT).show();
            connect.setCardBackgroundColor(0xffe1f7dd);
            connect.setStrokeColor(0xffA1A1A1);

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // store the switch state
            editor.putBoolean(Constants.IS_BT_CONNECTED, true);

            // apply changes
            editor.apply();
        }
    }

//    private void tighten (String i) {
//        try {
//            if (btSocket!=null) {
//                btSocket.getOutputStream().write(i.toString().getBytes());
//            }
//        }
//        catch (Exception e) {
//            Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mSocket = tmp;
        }
        public void run() {
            myBluetooth.cancelDiscovery();
            try {
                mSocket.connect();
            } catch (IOException connectException) {
                try {
                    mSocket.close();
                } catch (IOException closeException) { }
                return;
            }

        }
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                int x = mmInStream.read();
                while (x != 100){};
            } catch (IOException e) { }
        }
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) { }
        }
    }

}