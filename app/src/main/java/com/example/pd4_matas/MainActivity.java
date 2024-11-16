package com.example.pd4_matas;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private ListView listViewRates;
    private ArrayList<String> ratesList;
    private ArrayAdapter<String> adapter;

    private static final String TAG = "MainActivity"; // Used for logging, no need to externalize

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewRates = findViewById(R.id.listViewRates);
        ratesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ratesList);
        listViewRates.setAdapter(adapter);

        // Fetch exchange rates using API URL from strings.xml
        String apiUrl = getString(R.string.api_url);
        new FetchExchangeRatesTask().execute(apiUrl);
    }

    private class FetchExchangeRatesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                Log.d(TAG, getString(R.string.log_fetched_data, result.toString()));

            } catch (Exception e) {
                Log.e(TAG, getString(R.string.log_fetch_error), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.isEmpty()) {
                Toast.makeText(MainActivity.this, getString(R.string.fetch_error_message), Toast.LENGTH_SHORT).show();
                Log.e(TAG, getString(R.string.log_result_empty));
                return;
            }
            parseExchangeRates(result);
        }
    }

    private void parseExchangeRates(String json) {
        try {
            // Clear the existing rates
            ratesList.clear();

            // Parse the JSON object
            JSONObject response = new JSONObject(json);
            JSONObject rates = response.getJSONObject("rates");

            // Iterate over the keys and values
            Iterator<String> keys = rates.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = rates.getString(key);
                ratesList.add(getString(R.string.rate_format, key, value));
            }

            // Notify the adapter about data changes
            adapter.notifyDataSetChanged();
            Log.d(TAG, getString(R.string.log_listview_updated));

        } catch (Exception e) {
            Log.e(TAG, getString(R.string.log_parse_error), e);
            Toast.makeText(this, getString(R.string.parse_error_message), Toast.LENGTH_SHORT).show();
        }
    }
}
