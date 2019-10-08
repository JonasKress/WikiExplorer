package net.nousefor.wikiexplorer.notification;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import net.nousefor.wikiexplorer.api.Wikidata;

import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_OPEN_MAP = "MAP";
    public static final String ACTION_OPEN_WIKIPEDIA = "WIKIPEDIA";
    public static final String ACTION_OPEN_WIKIDATA = "WIKIDATA";
    public static final String ACTION_EDIT_LABEL = "EDIT_LABEL";

    public static final String REMOTE_INPUT_KEY = "REMOTE_INPUT_KEY";


    public static final String EXTRA_DATA = "EXTRA_DATA_STRING";
    public static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String data = extras.getString(EXTRA_DATA);
        int notificationId = extras.getInt(EXTRA_NOTIFICATION_ID, 0);
        String uri = null;

        switch (intent.getAction()) {
            case ACTION_OPEN_WIKIPEDIA:
                String lang = Locale.getDefault().getLanguage();
                uri = "https://www.wikidata.org/wiki/Special:GoToLinkedPage?site=" + lang + "&itemid=" + data;
                break;
            case ACTION_OPEN_WIKIDATA:
                uri = "https://www.wikidata.org/entity/" + data;
                break;
            case ACTION_OPEN_MAP:
                String[] points = data.replace("Point(", "").replace(")", "").split(" ");
                uri = "geo:" + points[1] + "," + points[0] + "?q=" + points[1] + "," + points[0];
                break;
            case ACTION_EDIT_LABEL:
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                String input = remoteInput.getCharSequence(REMOTE_INPUT_KEY).toString();
                Log.e("remoteInput", input);

                Wikidata w = new Wikidata();
                if (w.setLabel(data, "en", input))
                    (new Notification(context)).showAfterEditNotification(notificationId, null);
                else
                    (new Notification(context)).showAfterEditNotification(notificationId, "Sorry, something went wrong!");

                return;
        }

        if (uri != null) {
            Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openIntent);
        }
    }
}
