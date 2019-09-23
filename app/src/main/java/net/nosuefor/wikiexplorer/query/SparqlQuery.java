package net.nosuefor.wikiexplorer.query;

import android.location.Location;

import net.nosuefor.wikiexplorer.model.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SparqlQuery {

    Api api = null;

    public SparqlQuery(String endpoint) {

        this.api = new Api(endpoint);
    }

    public ArrayList<Item> execute(String query, Location location) {

        query = query.replaceAll("\"\\[AUTO\\_COORDINATES\\]\"",
                "\"Point(" + location.getLongitude()
                        + " " + location.getLatitude() + ")\"^^geo:wktLiteral");

        return createItems(api.getResult(query));
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
                item.distance = jsonRow.getJSONObject("distance").getDouble("value") * 1000;

                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }
}
