package net.nousefor.wikiexplorer.model.action;

import android.content.Context;

import com.google.gson.Gson;

import net.nousefor.wikiexplorer.model.item.ItemNotification;

import java.util.HashMap;
import java.util.Locale;

public class ActionOpenWikipedia extends Action {

    private String WIKIDATA_GOTO_LINKED_PAGE = "https://www.wikidata.org/wiki/Special:GoToLinkedPage?site={LANG}&itemid={ITEM_ID}";

    public ActionOpenWikipedia() {
        labels = new HashMap<String, String>() {{
            put("en", "Wikipedia");
        }};
    }

    String getParmsJson(ItemNotification itemNotification) {
        Parms parms = new Parms();
        parms.itemId = itemNotification.itemId;
        parms.wikipediaUrl = itemNotification.wikipediaUrl;
        return new Gson().toJson(parms);
    }

    public boolean execute(String parmsJson, Context context) throws Exception {
        Parms p = new Gson().fromJson(parmsJson, Parms.class);

        String lang = Locale.getDefault().getLanguage();

        if (p.wikipediaUrl != null && p.wikipediaUrl.toLowerCase().startsWith("https://"))
            openIntent(p.wikipediaUrl, context);
        else
            openIntent(WIKIDATA_GOTO_LINKED_PAGE.replace("{LANG}", lang).replace("{ITEM_ID}", p.itemId), context);

        return true;
    }

    class Parms {
        String itemId;
        String wikipediaUrl;
    }
}
