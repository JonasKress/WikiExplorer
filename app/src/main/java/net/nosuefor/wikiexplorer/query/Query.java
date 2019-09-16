package net.nosuefor.wikiexplorer.query;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Query {


    String endpoint = null;

    public Query(String endpoint) {
        this.endpoint = endpoint;
    }

    public ArrayList<Item> execute(String query, Location location) {

        query = query.replaceAll("\"\\[AUTO\\_COORDINATES\\]\"",
                "\"Point(" + location.getLongitude()
                        + " " + location.getLatitude() + ")\"^^geo:wktLiteral");


        query = endpoint + URLEncoder.encode(query);

        Log.d("query", query);
        return fetchQuery(query);
    }

    private ArrayList<Item> fetchQuery(String queryUrl) {
        ArrayList<Item> items = new ArrayList();

        try {
            URL url = new URL(queryUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
            String response = s.next();
            Log.d("main", response);
            urlConnection.disconnect();

            return createItems(response);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    private ArrayList<Item> createItems(String jsonString) {
        ArrayList<Item> items = new ArrayList();

        try {

            JSONObject json = new JSONObject(jsonString);
            JSONArray jsonRows = json.getJSONObject("results").getJSONArray("bindings");

            for (int i = 0; i < jsonRows.length(); i++) {

                JSONObject jsonRow = jsonRows.getJSONObject(i);
                Item item = new Item();
                item.id = jsonRow.getJSONObject("item").getString("value");
                item.imageUrl = jsonRow.getJSONObject("image").getString("value").replace("http://", "https://");
                item.label = jsonRow.getJSONObject("itemLabel").getString("value");
                item.description = jsonRow.getJSONObject("itemDescription").getString("value");
                item.location = jsonRow.getJSONObject("location").getString("value");
                item.distance = jsonRow.getJSONObject("distance").getLong("value");

                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }
}
