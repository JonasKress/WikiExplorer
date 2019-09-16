package net.nosuefor.wikiexplorer.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_IGNORE = "IGNORE";
    public static final String ACTION_OPEN_WIKIPEDIA = "WIKIPEDIA";
    public static final String ACTION_OPEN_WIKIDATA = "WIKIDATA";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "receive" + intent.getAction());

        String id = intent.getStringExtra("id");
        String uri = null;

        switch (intent.getAction()) {
            case ACTION_OPEN_WIKIPEDIA:
                uri = "https://www.wikidata.org/wiki/Special:GoToLinkedPage?site=en&itemid=" + id;
                Log.d("Wikipedia", uri);
                Intent wikipediaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                wikipediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(wikipediaIntent);
                break;
            case ACTION_OPEN_WIKIDATA:
                uri = "https://www.wikidata.org/entity/" + id;
                Log.d("Wikidata", uri);
                Intent wikidataIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                wikidataIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(wikidataIntent);
                break;

        }

    }
}
