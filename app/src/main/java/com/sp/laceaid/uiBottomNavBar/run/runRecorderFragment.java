package com.sp.laceaid.uiBottomNavBar.run;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sp.laceaid.R;
import com.sp.laceaid.RecordRunActivity;
import com.sp.laceaid.RunList;
import com.sp.laceaid.uiBottomNavBar.ar.MeasureActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;


public class runRecorderFragment extends Fragment {
    ListView runList;
    ArrayAdapter<String> adapter;
    TextView UID;
    Button addButton;

    // firebase
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String userID;


    public runRecorderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_run_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // grab data from firebase realtime db
        user = FirebaseAuth.getInstance().getCurrentUser();
        // need to add the url as the default location is USA not SEA
        databaseReference = FirebaseDatabase.getInstance("https://lace-aid-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        userID = user.getUid();

        addButton = getView().findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RecordRunActivity.class);
                startActivity(intent);
            }
        });

        UID = getView().findViewById(R.id.tv_UID);
        UID.setText(userID);
        runList = getView().findViewById(R.id.runList);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        runList.setAdapter(adapter);
        new Connection().execute();
    }

    class Connection extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            String host = "http://222.164.5.103/laceaid/runRecords.php";
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(host));
                HttpResponse response = client.execute(request);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                StringBuffer stringBuffer = new StringBuffer("");

                String line = "";
                while ((line = reader.readLine()) != null) {
                    stringBuffer.append(line);
                    break;
                }
                reader.close();
                result = stringBuffer.toString();

            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
            // parsing JSON data here
            try {
                JSONObject jsonResult = new JSONObject(result);
                int success = jsonResult.getInt("success");
                if (success == 1) {
//                    Toast.makeText(getApplicationContext(), "There are records in database", Toast.LENGTH_SHORT).show();
                    JSONArray records = jsonResult.getJSONArray("runRecords");
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject record = records.getJSONObject(i);
                        // Querying from db
                        int id = record.getInt("id");
                        String username = record.getString("username");
                        String timeElapsed = record.getString("timeElapsed");
                        Double totalDistance = record.getDouble("totalDistance");

                        if (username == userID){
                            String line = id + "-" + username + "-" + timeElapsed + "-" + totalDistance;
                            adapter.add(line);
                        }


                    }
                } else {
                    Toast.makeText(getContext(), "There are no records in database", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}