package net.nousefor.wikiexplorer.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import net.nousefor.wikiexplorer.MainActivity;
import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.model.item.ItemNotification;


public class Notifications {

    private static final String CHANNEL_ID = "Wiki Explorer";

    private Context context;

    public Notifications(Context context) {
        this.context = context;
        createNotificationChannel();

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
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message);

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
