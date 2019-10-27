package net.nousefor.wikiexplorer.model.action;

import android.content.Context;

import com.google.gson.Gson;

import net.nousefor.wikiexplorer.api.Wikidata;
import net.nousefor.wikiexplorer.model.item.ItemNotification;

public class ActionAddStatement extends Action {
    public String propertyId;
    public String value;

    String getParmsJson(ItemNotification itemNotification) {
        Parms parms = new Parms();
        parms.notificationId = itemNotification.notificationId;
        parms.itemId = itemNotification.itemId;
        parms.propertyId = propertyId;
        parms.value = value;
        return new Gson().toJson(parms);
    }

    @Override
    public boolean execute(String parmsJson, Context context) throws Exception {
        Parms p = new Gson().fromJson(parmsJson, Parms.class);
        Wikidata w = new Wikidata();

        if (!w.setProperty(p.itemId, p.propertyId, p.value))
            throw new Exception("Sorry, something went wrong!");

        return true;
    }

    @Override
    public boolean isEditAction() {
        return true;
    }

    class Parms {
        int notificationId;
        String itemId;
        String propertyId;
        String value;
    }
}