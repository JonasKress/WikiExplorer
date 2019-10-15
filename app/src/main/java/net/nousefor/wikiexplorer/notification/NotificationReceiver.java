package net.nousefor.wikiexplorer.notification;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import net.nousefor.wikiexplorer.api.Wikidata;

import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_OPEN_MAP = "MAP";
    public static final String ACTION_OPEN_WIKIPEDIA = "WIKIPEDIA";
    public static final String ACTION_OPEN_WIKIDATA = "WIKIDATA";

    public static final String ACTION_EDIT_LABEL = "EDIT_LABEL";
    public static final String ACTION_EDIT_DESCRIPTION = "EDIT_DESCRIPTION";
    public static final String ACTION_EDIT_IMAGE = "EDIT_IMAGE";


    public static final String REMOTE_INPUT_KEY = "REMOTE_INPUT_KEY";

    public static final String EXTRA_DATA = "EXTRA_DATA_STRING";
    public static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";
    public static final String EXTRA_ITEM_ID = "EXTRA_ITEM_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String data = extras.getString(EXTRA_DATA);
        int notificationId = extras.getInt(EXTRA_NOTIFICATION_ID, 0);
        String itemId = extras.getString(EXTRA_ITEM_ID);

        String uri = null;
        Wikidata w = new Wikidata();
        String lang = Locale.getDefault().getLanguage();

        switch (intent.getAction()) {
            case ACTION_OPEN_WIKIPEDIA:
                if (data.toLowerCase().startsWith("https://"))
                    uri = data;
                else
                    uri = "https://www.wikidata.org/wiki/Special:GoToLinkedPage?site=" + lang + "&itemid=" + itemId;
                break;
            case ACTION_OPEN_WIKIDATA:
                uri = "https://www.wikidata.org/entity/" + itemId;
                break;
            case ACTION_OPEN_MAP:
                String[] points = data.replace("Point(", "").replace(")", "").split(" ");
                uri = "geo:" + points[1] + "," + points[0] + "?q=" + points[1] + "," + points[0];
                break;
            case ACTION_EDIT_LABEL:
                if (w.setLabel(itemId, "en", getRemoteInput(intent)))
                    (new Notifications(context)).showAfterEditNotification(notificationId, null);
                else
                    (new Notifications(context)).showAfterEditNotification(notificationId, "Sorry, something went wrong!");
                return;
            case ACTION_EDIT_DESCRIPTION:
                if (w.setDescription(itemId, "en", getRemoteInput(intent)))
                    (new Notifications(context)).showAfterEditNotification(notificationId, null);
                else
                    (new Notifications(context)).showAfterEditNotification(notificationId, "Sorry, something went wrong!");
                return;
            case ACTION_EDIT_IMAGE:
                String imageUrl = data.substring(data.lastIndexOf("/") + 1);
                if (w.setImage(itemId, imageUrl))
                    (new Notifications(context)).showAfterEditNotification(notificationId, null);
                else
                    (new Notifications(context)).showAfterEditNotification(notificationId, "Sorry, something went wrong!");
                return;
        }

        if (uri != null) {
            Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openIntent);
        }
    }

    private String getRemoteInput(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        return remoteInput.getCharSequence(REMOTE_INPUT_KEY).toString();

    }
}
