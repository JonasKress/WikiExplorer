package net.nousefor.wikiexplorer.api;

import android.location.Location;
import android.util.Log;

import net.nousefor.wikiexplorer.model.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SparqlQuery {

    private Api api;

    public SparqlQuery(String endpoint) {

        this.api = new Api(endpoint);
    }

    public ArrayList<Item> execute(String query, Location location) {

        query = query.replaceAll("\"\\[AUTO\\_COORDINATES\\]\"",
                "\"Point(" + location.getLongitude()
                        + " " + location.getLatitude() + ")\"^^geo:wktLiteral");

        Log.d(this.toString(), "Execute query" + query);
        return createItems(api.get(api.encode(query)));
    }


    private ArrayList<Item> createItems(String jsonString) {
        ArrayList<Item> items = new ArrayList();

        try {

            JSONObject json = new JSONObject(jsonString);
            JSONArray jsonRows = json.getJSONObject("results").getJSONArray("bindings");

            Log.d(this.toString(), "Processing rows:" + jsonRows.length());
            for (int i = 0; i < jsonRows.length(); i++) {

                try {
                    JSONObject jsonRow = jsonRows.getJSONObject(i);
                    Item item = new Item();
                    item.id = jsonRow.getJSONObject("item").getString("value");
                    item.id = "Q" + item.id.substring(item.id.indexOf("/Q") + 2);

                    item.label = jsonRow.getJSONObject("itemLabel").getString("value");

                    if (jsonRow.has("itemDescription"))
                        item.description = jsonRow.getJSONObject("itemDescription").getString("value");
                    else
                        item.description = "";

                    if (jsonRow.has("image"))
                        item.imageUrl = jsonRow.getJSONObject("image").getString("value").replace("http://", "https://");
                    if (jsonRow.has("sitelink"))
                        item.siteLink = jsonRow.getJSONObject("sitelink").getString("value");

                    item.location = jsonRow.getJSONObject("location").getString("value");
                    item.distance = jsonRow.getJSONObject("distance").getDouble("value") * 1000;
                    items.add(item);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }
}
