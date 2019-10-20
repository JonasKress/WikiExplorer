package net.nousefor.wikiexplorer.model.action;

import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import net.nousefor.wikiexplorer.api.Wikidata;
import net.nousefor.wikiexplorer.model.item.ItemNotification;

public class ActionAddLabelIfNotExists extends ActionAddLabel {

    private final Wikidata wikidata = new Wikidata();

    public NotificationCompat.Action createAction(ItemNotification itemNotification, Context context) {
        if (wikidata.getLabel(itemNotification.itemId, language) != null)
            return null;

        return super.createAction(itemNotification, context);
    }

    @Override
    public boolean execute(String parmsJson, Context context) throws Exception {
        Parms p = new Gson().fromJson(parmsJson, Parms.class);

        if (wikidata.getLabel(p.itemId, p.language) != null)
            throw new Exception("Sorry, something went wrong!");

        return super.execute(parmsJson, context);
    }

}