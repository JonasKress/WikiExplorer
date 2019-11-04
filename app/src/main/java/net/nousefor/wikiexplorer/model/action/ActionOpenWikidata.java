package net.nousefor.wikiexplorer.model.action;

import android.content.Context;

import com.google.gson.Gson;

import java.util.HashMap;

public class ActionOpenWikidata extends Action {
    private String WIKIDATA_ENTITY_URI_PREFIX = "https://www.wikidata.org/entity/";

    public ActionOpenWikidata() {
        labels = new HashMap<String, String>() {{
            put("en", "Wikidata");
        }};
    }

    public boolean execute(String parmsJson, Context context) throws Exception {
        Action.Parms p = new Gson().fromJson(parmsJson, Action.Parms.class);
        openIntent(WIKIDATA_ENTITY_URI_PREFIX + p.itemId, context);

        return true;
    }
}
