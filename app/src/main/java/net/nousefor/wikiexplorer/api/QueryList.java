package net.nousefor.wikiexplorer.api;

import net.nousefor.wikiexplorer.model.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class QueryList {

    private static final String JSON_URL = "https://jonaskress.github.io/WikiExplorer/data/queries.json";

    public ArrayList<Query> get() {
        return parseJson((new Api(JSON_URL).get()));
    }

    private ArrayList<Query> parseJson(String jsonString) {
        ArrayList<Query> items = new ArrayList();

        try {
            JSONArray jsonRows = new JSONArray(jsonString);
            for (int i = 0; i < jsonRows.length(); i++) {
                JSONObject jsonRow = jsonRows.getJSONObject(i);
                Query item = new Query();
                item.name = jsonRow.getString("name");
                item.query = jsonRow.getString("query");

                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }
}
