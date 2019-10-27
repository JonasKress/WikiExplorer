package net.nousefor.wikiexplorer.api;

import com.google.gson.Gson;

import net.nousefor.wikiexplorer.model.action.Action;
import net.nousefor.wikiexplorer.model.query.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class QueryList {

    private static final String JSON_URL = "https://jonaskress.github.io/WikiExplorer/data/queries.json";
    private static final String ACTION_PACKAGE = "net.nousefor.wikiexplorer.model.action.";

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
                item.sparql = jsonRow.getString("query");

                if (jsonRow.has("actions"))
                    item.actions = createQueryActions(jsonRow.getJSONArray("actions"));

                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    Action[] createQueryActions(JSONArray jsonActions) throws JSONException {
        ArrayList<Action> actions = new ArrayList<>();
        Class type = null;

        for (int i = 0; i < jsonActions.length(); i++) {
            JSONObject action = jsonActions.getJSONObject(i);

            try {
                type = Class.forName(ACTION_PACKAGE + action.getString("type"));
                Action a = (Action) new Gson().fromJson(action.toString(), type);
                actions.add(a);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return actions.toArray(new Action[actions.size()]);
    }
}
