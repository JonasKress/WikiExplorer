package net.nousefor.wikiexplorer.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import net.nousefor.wikiexplorer.MainActivity;
import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.model.ItemNotification;

public class Notifications {

    public static final String ACTION_OPEN_MAP = "MAP";
    public static final String ACTION_OPEN_WIKIPEDIA = "WIKIPEDIA";
    public static final String ACTION_OPEN_WIKIDATA = "WIKIDATA";

    public static final String ACTION_EDIT_LABEL = "EDIT_LABEL";
    public static final String ACTION_EDIT_DESCRIPTION = "EDIT_DESCRIPTION";
    public static final String ACTION_EDIT_IMAGE = "EDIT_IMAGE";

    private static final String CHANNEL_ID = "Wiki Explorer";

    private Context context;

    public Notifications(Context context) {
        this.context = context;
        createNotificationChannel();

    }

    private Bundle getExtrasBundle(int notificationId, String itemId, String data) {
        Bundle extras = new Bundle();
        extras.putInt(NotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        extras.putString(NotificationReceiver.EXTRA_ITEM_ID, itemId);
        extras.putString(NotificationReceiver.EXTRA_DATA, data);

        return extras;
    }

    public NotificationCompat.Action createAction(ItemNotification itemNotification, String action) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        int icon = R.drawable.ic_edit_black_24dp;
        Bundle extras = getExtrasBundle(itemNotification.notificationId, itemNotification.itemId, null);
        RemoteInput remoteInput = null;
        String label;

        switch (action) {
            case ACTION_OPEN_WIKIDATA:
                label = context.getString(R.string.ui_notification_wikidata);
                intent.setAction(NotificationReceiver.ACTION_OPEN_WIKIDATA);
                break;
            case ACTION_OPEN_WIKIPEDIA:
                label = context.getString(R.string.ui_notification_wikipedia);
                intent.setAction(NotificationReceiver.ACTION_OPEN_WIKIPEDIA);
                if (itemNotification.wikipediaUrl != null)
                    extras = getExtrasBundle(itemNotification.notificationId, itemNotification.itemId, itemNotification.wikipediaUrl);
                break;
            case ACTION_OPEN_MAP:
                label = context.getString(R.string.ui_notification_map);
                intent.setAction(NotificationReceiver.ACTION_OPEN_MAP);
                extras = getExtrasBundle(itemNotification.notificationId, itemNotification.itemId, itemNotification.location);
                break;
            case ACTION_EDIT_IMAGE:
                label = context.getString(R.string.ui_notification_add_image);
                intent.setAction(NotificationReceiver.ACTION_EDIT_IMAGE);
                extras = getExtrasBundle(itemNotification.notificationId, itemNotification.itemId, itemNotification.imageUrl);
                break;
            case ACTION_EDIT_LABEL:
                label = context.getString(R.string.ui_notification_add_label_en);
                intent.setAction(NotificationReceiver.ACTION_EDIT_LABEL);
                remoteInput = new RemoteInput.Builder(NotificationReceiver.REMOTE_INPUT_KEY)
                        .setLabel(label)
                        .build();
                break;
            case ACTION_EDIT_DESCRIPTION:
                label = context.getString(R.string.ui_notification_add_description_en);
                intent.setAction(NotificationReceiver.ACTION_EDIT_DESCRIPTION);
                remoteInput = new RemoteInput.Builder(NotificationReceiver.REMOTE_INPUT_KEY)
                        .setLabel(label)
                        .build();
                break;
            default:
                return null;
        }

        intent.putExtras(extras);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, itemNotification.notificationId, intent, 0);
        NotificationCompat.Action.Builder notification = new NotificationCompat.Action.Builder(icon, label, pendingIntent);

        if (remoteInput != null)
            notification.addRemoteInput(remoteInput).setAllowGeneratedReplies(true);

        return notification.build();
    }

    public void showItemNotification(ItemNotification itemNotification) {
        RemoteViews itemNotificationCollapsed = new RemoteViews(context.getPackageName(), R.layout.item_notification_collapsed);
        itemNotificationCollapsed.setTextViewText(R.id.item_notificaton_collalpsed_title, Html.fromHtml(itemNotification.titleCollapsed));
        itemNotificationCollapsed.setTextViewText(R.id.item_notificaton_collalpsed_description, Html.fromHtml(itemNotification.descriptionCollapsed));
        if (itemNotification.imageCollapsed != null) {
            itemNotificationCollapsed.setImageViewBitmap(R.id.item_notificaton_collalpsed_image, itemNotification.imageCollapsed);
            itemNotificationCollapsed.setViewVisibility(R.id.item_notificaton_collalpsed_image, View.VISIBLE);
        }

        RemoteViews itemNotificationExpanded = new RemoteViews(context.getPackageName(), R.layout.item_notification_expandend);
        itemNotificationExpanded.setTextViewText(R.id.item_notificaton_expanded_title, Html.fromHtml(itemNotification.titleExpanded));
        itemNotificationExpanded.setTextViewText(R.id.item_notificaton_expanded_description, Html.fromHtml(itemNotification.descriptionExpanded));
        if (itemNotification.imageExpanded != null) {
            itemNotificationExpanded.setImageViewBitmap(R.id.item_notificaton_expanded_image, itemNotification.imageExpanded);
            itemNotificationExpanded.setViewVisibility(R.id.item_notificaton_expanded_image, View.VISIBLE);
            itemNotificationExpanded.setInt(R.id.item_notificaton_expanded_description, "setMaxLines", 5);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_room_24px)
                .setContentTitle(itemNotification.titleCollapsed)
                .setContentText(itemNotification.descriptionCollapsed)
                .setAutoCancel(true)
                .setCustomContentView(itemNotificationCollapsed)
                .setCustomBigContentView(itemNotificationExpanded)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        for (NotificationCompat.Action action : itemNotification.actions) {
            if (action != null)
                builder.addAction(action);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(itemNotification.notificationId, builder.build());
    }

    void showAfterEditNotification(final int id, String message) {
        boolean autoClose = false;
        if (message == null) {
            message = context.getString(R.string.ui_notification_edit_saved);
            autoClose = true;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_edit_black_24dp)
                .setContentTitle(message);

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, builder.build());

        if (!autoClose)
            return;

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            public void run() {
                notificationManager.cancel(id);
            }
        }, 2000);
    }

    public void removeAll() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

    public android.app.Notification createBackgroundServiceNotification() {
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_explore_24px)
                .setContentTitle(context.getString(R.string.ui_main_enable_notifications_enabled_label))
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
