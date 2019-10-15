package net.nousefor.wikiexplorer.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.api.Wikidata;
import net.nousefor.wikiexplorer.api.Wikipedia;
import net.nousefor.wikiexplorer.helper.Format;
import net.nousefor.wikiexplorer.model.Item;
import net.nousefor.wikiexplorer.model.ItemNotification;
import net.nousefor.wikiexplorer.model.Summary;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ItemNotificationsGeneratorThread extends Thread {
    private static final String PREFERENCES_NAME = "BACKGROUND_SERVICE_PREFERENCES";
    private static final String IMAGE_PLACEHOLDER_URL = "https://upload.wikimedia.org/wikipedia/commons/4/49/Medium_placeholder.png";
    private static final String COMMONS_IMAGE_URL_PREFIX = "https://upload.wikimedia.org/";

    private Context context;
    private Notifications notifications;
    private ArrayList<Item> items = new ArrayList<>();

    //preferences
    private boolean enableWikipediaSummary = false;
    private boolean enableUniqueMode = false;
    private boolean enableEditing = false;

    private boolean isDestroyed = false;

    public ItemNotificationsGeneratorThread(Context context) {
        this.context = context;
        notifications = new Notifications(context);
        updatePreferences();
    }

    public static void resetNotificationMemory(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    public void updatePreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        enableUniqueMode = sharedPreferences.getBoolean(context.getString(R.string.notifications_unique), enableUniqueMode);
        enableEditing = sharedPreferences.getBoolean(context.getString(R.string.notifications_enable_edit), enableEditing);

        enableWikipediaSummary = sharedPreferences.getBoolean(context.getString(R.string.notifications_wikipedia_summary), enableWikipediaSummary);
    }

    public void addItems(List<Item> items) {
        this.items.addAll(items);
    }

    private ItemNotification createItemNotification(Item item) {
        ItemNotification n = new ItemNotification();
        Summary s = null;
        String imageUrl = item.imageUrl;

        n.notificationId = Integer.parseInt(item.id.replace("Q", ""));
        n.titleCollapsed = item.label + " (" + Format.distance(item.distance) + ")";
        n.descriptionCollapsed = item.description;

        n.titleExpanded = n.titleCollapsed;
        n.descriptionExpanded = n.descriptionCollapsed;

        n.wikipediaUrl = item.siteLink;
        n.itemId = item.id;
        n.location = item.location;

        if (enableWikipediaSummary) {
            s = getWikipediaSummary(item);
            if (s != null) {
                if (s.text != null)
                    n.descriptionExpanded = s.text;

                if (imageUrl == null)
                    imageUrl = s.imageUrl;
            }
        }

        if (imageUrl != null) {
            try {
                URL url = new URL(imageUrl);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                n.imageCollapsed = bmp;
                n.imageExpanded = bmp;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        n.actions = createItemNotificationActions(n, item, s);

        return n;
    }

    private NotificationCompat.Action[] createItemNotificationActions(ItemNotification itemNotification, Item item, Summary summary) {
        ArrayList<NotificationCompat.Action> actions = new ArrayList<>();

        if (enableEditing && item.id != null) {
            Wikidata w = new Wikidata();

            if (item.imageUrl == null
                    && summary != null
                    && summary.imageUrl != null
                    && summary.imageUrl.startsWith(COMMONS_IMAGE_URL_PREFIX)
                    && w.getImage(item.id) == null) {

                itemNotification.imageUrl = summary.imageUrl;
                actions.add(notifications.createAction(itemNotification, Notifications.ACTION_EDIT_IMAGE));
            }

            if (w.getDescription(item.id, "en") == null) {
                actions.add(notifications.createAction(itemNotification, Notifications.ACTION_EDIT_DESCRIPTION));
            }

            if (w.getLabel(item.id, "en") == null) {
                actions.add(notifications.createAction(itemNotification, Notifications.ACTION_EDIT_LABEL));
            }
        }

        actions.add(notifications.createAction(itemNotification, Notifications.ACTION_OPEN_WIKIPEDIA));
        actions.add(notifications.createAction(itemNotification, Notifications.ACTION_OPEN_WIKIDATA));
        actions.add(notifications.createAction(itemNotification, Notifications.ACTION_OPEN_MAP));

        return actions.toArray(new NotificationCompat.Action[actions.size()]);
    }

    private Summary getWikipediaSummary(Item item) {
        if (item.siteLink == null)
            return null;

        try {
            String url = item.siteLink.substring(item.siteLink.indexOf("https://"), item.siteLink.indexOf("/wiki/"));
            String title = item.siteLink.substring(item.siteLink.indexOf("/wiki/")).replace("/wiki/", "");

            return (new Wikipedia()).getSummary(url, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createNotification(Item item) {
        notifications.showItemNotification(createItemNotification(item));
    }

    private boolean isDestroyed() {
        if (Thread.currentThread().isInterrupted()) {
            return true;
        }

        return isDestroyed;
    }

    public ItemNotificationsGeneratorThread setDestroyed(boolean destroyed) {
        isDestroyed = destroyed;
        return this;
    }

    private boolean isSkippedNotification(String qId) {
        if (!enableUniqueMode)
            return false;

        if (isInNotificationMemory(qId))
            return true;

        addToNotificationMemory(qId);
        return false;
    }

    private void doSleep(long s) {
        try {
            Thread.sleep(s);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        super.run();

        while (true) {
            if (items.size() == 0) {
                doSleep(2000);
                continue;
            }
            Item item = items.get(0);
            items.remove(0);

            if (isDestroyed())
                break;
            if (isSkippedNotification(item.id))
                continue;
            createNotification(item);

            doSleep(1000);
        }
    }

    private boolean isInNotificationMemory(String qId) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);
        return settings.getBoolean(qId, false);
    }

    private void addToNotificationMemory(String qId) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(qId, true);
        editor.apply();
    }
}
