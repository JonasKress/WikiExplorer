package net.nousefor.wikiexplorer.api;

import net.nousefor.wikiexplorer.model.action.Action;
import net.nousefor.wikiexplorer.model.action.ActionOpenWikidata;
import net.nousefor.wikiexplorer.model.action.ActionOpenWikipedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class QueryListTest {

    JSONArray getActions() {
        try {
            JSONObject o = new JSONObject();
            o.put("type", "ActionOpenWikidata");

            JSONObject o1 = new JSONObject();
            o1.put("type", "ActionOpenWikipedia");

            JSONArray a = new JSONArray();
            a.put(o);
            a.put(o1);
            return a;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Test
    public void createQueryActions() throws JSONException {
        QueryList l = new QueryList();
        Action[] actions = l.createQueryActions(getActions());

        assertTrue(actions.length == 2);
        assertTrue(actions[0] instanceof ActionOpenWikidata);
        assertTrue(actions[1] instanceof ActionOpenWikipedia);
    }
}