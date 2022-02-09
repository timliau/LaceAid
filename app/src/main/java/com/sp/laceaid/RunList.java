package com.sp.laceaid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class RunList extends AppCompatActivity {
    ListView runList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_list);


        runList = findViewById(R.id.runList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
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
                while((line = reader.readLine()) != null){
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
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            // parsing JSON data here
            try {
                JSONObject jsonResult = new JSONObject(result);
                int success = jsonResult.getInt("success");
                if (success == 1){
//                    Toast.makeText(getApplicationContext(), "There are records in database", Toast.LENGTH_SHORT).show();
                    JSONArray records = jsonResult.getJSONArray("runRecords");
                    for (int i = 0; i < records.length(); i++){
                        JSONObject record = records.getJSONObject(i);
                        // Querying from db
                        int id = record.getInt("id");
                        String username = record.getString("username");
                        String timeElapsed = record.getString("timeElapsed");
                        Double totalDistance = record.getDouble("totalDistance");

                        String line = id + "-" + username + "-" + timeElapsed + "-" + totalDistance;
                        adapter.add(line);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "There are no records in database", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}