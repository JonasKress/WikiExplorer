package net.nousefor.wikiexplorer.model.action;

import android.content.Context;

import androidx.core.app.RemoteInput;

import com.google.gson.Gson;

import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.api.Wikidata;
import net.nousefor.wikiexplorer.model.item.ItemNotification;

import java.util.HashMap;

public class ActionAddLabel extends Action {

    public String language;

    public ActionAddLabel() {
        labels = new HashMap<String, String>() {{
            put("en", "Add label");
        }};
    }

    RemoteInput getRemoteInput(Context context) {
        String label = context.getString(R.string.ui_notification_add_label_en);
        return new RemoteInput.Builder(REMOTE_INPUT_KEY)
                .setLabel(label)
                .build();
    }

    String getParmsJson(ItemNotification itemNotification) {
        Parms parms = new Parms();
        parms.itemId = itemNotification.itemId;
        parms.language = language;
        return new Gson().toJson(parms);
    }

    @Override
    public boolean execute(String parmsJson, Context context) throws Exception {
        Parms p = new Gson().fromJson(parmsJson, Parms.class);
        Wikidata w = new Wikidata();

        if (!w.setLabel(p.itemId, p.language, getRemoteInputValue()))
            throw new Exception("Sorry, something went wrong!");

        return true;
    }

    @Override
    public boolean isEditAction() {
        return true;
    }

    class Parms {
        String itemId;
        String language;
    }
}