package net.nosuefor.wikiexplorer.query;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class QueryList {

    private final static String url = "https://jonaskress.github.io/WikiExplorer/data/queries.json";

    public static ArrayList<QueryItem> get() {

        return parseJson(fetchJson());
    }

    private static String fetchJson() {

        try {
            URL url = new URL(QueryList.url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
            String response = s.next();
            Log.d("QueryList", response);
            urlConnection.disconnect();

            return response;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ArrayList<QueryItem> parseJson(String jsonString) {
        ArrayList<QueryItem> items = new ArrayList();

        try {

            JSONArray jsonRows = new JSONArray(jsonString);

            for (int i = 0; i < jsonRows.length(); i++) {

                JSONObject jsonRow = jsonRows.getJSONObject(i);
                QueryItem item = new QueryItem();
                item.name = jsonRow.getString("name");
                item.query = jsonRow.getString("query");


                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }
}
