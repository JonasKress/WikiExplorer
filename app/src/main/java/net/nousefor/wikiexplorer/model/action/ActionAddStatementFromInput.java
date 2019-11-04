package net.nousefor.wikiexplorer.model.action;

import android.content.Context;

import androidx.core.app.RemoteInput;

import com.google.gson.Gson;

import net.nousefor.wikiexplorer.api.Wikidata;
import net.nousefor.wikiexplorer.model.item.ItemNotification;

public class ActionAddStatementFromInput extends Action {
    public String propertyId;
    public String valueFormat;

    RemoteInput getRemoteInput(Context context) {
        String label = getLabel();
        return new RemoteInput.Builder(REMOTE_INPUT_KEY)
                .setLabel(label)
                .build();
    }

    String getParmsJson(ItemNotification itemNotification) {
        Parms parms = new Parms();
        parms.notificationId = itemNotification.notificationId;
        parms.itemId = itemNotification.itemId;
        parms.propertyId = propertyId;
        parms.valueFormat = valueFormat;
        return new Gson().toJson(parms);
    }

    @Override
    public boolean execute(String parmsJson, Context context) throws Exception {
        Parms p = new Gson().fromJson(parmsJson, Parms.class);
        Wikidata w = new Wikidata();

        String value = getRemoteInputValue();
        if (valueFormat != null && !value.matches(p.valueFormat))
            throw new Exception("Wrong format!");

        if (!w.setProperty(p.itemId, p.propertyId, value))
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
        String valueFormat;
    }
}