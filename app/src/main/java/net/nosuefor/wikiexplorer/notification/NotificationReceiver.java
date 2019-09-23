package net.nosuefor.wikiexplorer.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_OPEN_MAP = "MAP";
    public static final String ACTION_OPEN_WIKIPEDIA = "WIKIPEDIA";
    public static final String ACTION_OPEN_WIKIDATA = "WIKIDATA";

    public static final String EXTRA_DATA = "EXTRA_DATA";

    @Override
    public void onReceive(Context context, Intent intent) {
        String data = intent.getStringExtra(EXTRA_DATA);
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
        }
        Log.d("reciever", uri);

        Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(openIntent);
    }
}
