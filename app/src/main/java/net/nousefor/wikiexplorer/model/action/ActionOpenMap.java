package net.nousefor.wikiexplorer.model.action;

import android.content.Context;

import com.google.gson.Gson;

import net.nousefor.wikiexplorer.model.item.ItemNotification;

import java.util.HashMap;

public class ActionOpenMap extends Action {

    public ActionOpenMap() {
        labels = new HashMap<String, String>() {{
            put("en", "Map");
            put("de", "Karte");
        }};
    }

    String getParmsJson(ItemNotification itemNotification) {
        Parms parms = new Parms();
        parms.location = itemNotification.location;
        return new Gson().toJson(parms);
    }

    public boolean execute(String parmsJson, Context context) throws Exception {
        Parms p = new Gson().fromJson(parmsJson, Parms.class);
        String[] points = p.location.replace("Point(", "").replace(")", "").split(" ");

        String uri = "geo:" + points[1] + "," + points[0] + "?q=" + points[1] + "," + points[0];
        openIntent(uri, context);

        return true;
    }

    class Parms {
        String location;
    }
}
