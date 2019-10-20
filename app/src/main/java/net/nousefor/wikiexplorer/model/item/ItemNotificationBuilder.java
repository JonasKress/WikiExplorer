package net.nousefor.wikiexplorer.model.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;

import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.api.Wikipedia;
import net.nousefor.wikiexplorer.helper.Format;
import net.nousefor.wikiexplorer.model.Summary;
import net.nousefor.wikiexplorer.model.action.Action;
import net.nousefor.wikiexplorer.model.action.ActionAddDescriptionIfNotExists;
import net.nousefor.wikiexplorer.model.action.ActionAddLabelIfNotExists;
import net.nousefor.wikiexplorer.model.action.ActionAddStatementIfNotExist;
import net.nousefor.wikiexplorer.model.action.ActionOpenMap;
import net.nousefor.wikiexplorer.model.action.ActionOpenWikidata;
import net.nousefor.wikiexplorer.model.action.ActionOpenWikipedia;
import net.nousefor.wikiexplorer.model.query.Query;
import net.nousefor.wikiexplorer.notification.Notifications;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ItemNotificationBuilder {
    private static final String COMMONS_IMAGE_URL_PREFIX = "https://upload.wikimedia.org/";
    private static final String PROPERTY_ID_IMAGE = "P18";
    private final Notifications notifications;


    private boolean enableWikipediaSummary = true;
    private boolean enableEditing = false;
    private Context context;

    public ItemNotificationBuilder(Context context) {
        this.context = context;
        notifications = new Notifications(context);
    }


    public void setEnableWikipediaSummary(boolean enableWikipediaSummary) {
        this.enableWikipediaSummary = enableWikipediaSummary;
    }

    public void setEnableEditing(boolean enableEditing) {
        this.enableEditing = enableEditing;
    }

    public ItemNotification build(Item item, Query query) {
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

        n.query = query;
        n.actions = createItemNotificationActions(n, item, s);

        return n;
    }

    private NotificationCompat.Action[] createItemNotificationActions(ItemNotification itemNotification, Item item, Summary summary) {
        ArrayList<NotificationCompat.Action> actions = new ArrayList<>();

        if (itemNotification.query.actions != null) {
            for (Action action : itemNotification.query.actions) {
                if (action.isEditAction() && enableEditing == false)
                    continue;

                actions.add(action.createAction(itemNotification, context));
            }
        }

        if (enableEditing && item.id != null) {

            if (item.imageUrl == null
                    && summary != null
                    && summary.imageUrl != null
                    && summary.imageUrl.startsWith(COMMONS_IMAGE_URL_PREFIX)
            ) {

                ActionAddStatementIfNotExist addImageAction = new ActionAddStatementIfNotExist();
                addImageAction.propertyId = PROPERTY_ID_IMAGE;
                addImageAction.value = summary.imageUrl;
                addImageAction.labels = new HashMap<String, String>() {{
                    put("en", context.getString(R.string.ui_notification_add_image));
                }};

                actions.add(addImageAction.createAction(itemNotification, context));
            }

            ActionAddDescriptionIfNotExists addDescriptionAction = new ActionAddDescriptionIfNotExists();
            addDescriptionAction.language = "en";
            actions.add(addDescriptionAction.createAction(itemNotification, context));

            ActionAddLabelIfNotExists addLabelAction = new ActionAddLabelIfNotExists();
            addLabelAction.language = "en";
            actions.add(addLabelAction.createAction(itemNotification, context));
        }

        actions.add(new ActionOpenWikipedia().createAction(itemNotification, context));
        actions.add(new ActionOpenWikidata().createAction(itemNotification, context));
        actions.add(new ActionOpenMap().createAction(itemNotification, context));

        actions.removeAll(Collections.singleton(null));
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
}
