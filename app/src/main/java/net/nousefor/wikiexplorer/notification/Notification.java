package net.nousefor.wikiexplorer.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import net.nousefor.wikiexplorer.MainActivity;
import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.helper.Format;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Notification {

    private static final String CHANNEL_ID = "Wiki Explorer";
    private static final String IMAGE_PLACEHOLDER_URL = "https://upload.wikimedia.org/wikipedia/commons/4/49/Medium_placeholder.png";
    private Context context;

    public Notification(Context context) {
        this.context = context;
        createNotificationChannel();

    }

    private Bundle getExtrasBundle(int id, String extra) {
        Bundle extras = new Bundle();
        extras.putInt(NotificationReceiver.EXTRA_NOTIFICATION_ID, id);
        extras.putString(NotificationReceiver.EXTRA_DATA, extra);

        return extras;
    }

    private PendingIntent createIntent(String action, int id, String extra) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(action);
        intent.putExtras(getExtrasBundle(id, extra));
        return PendingIntent.getBroadcast(context, id, intent, 0);
    }


    private NotificationCompat.Action createEditAction(int id, String extra) {
        String label = context.getString(R.string.ui_notification_add_label_en);
        RemoteInput remoteInput = new RemoteInput.Builder(NotificationReceiver.REMOTE_INPUT_KEY)
                .setLabel(label)
                .build();

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(NotificationReceiver.ACTION_EDIT_LABEL);
        intent.putExtras(getExtrasBundle(id, extra));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        return new NotificationCompat.Action.Builder(R.drawable.ic_edit_black_24dp, label,
                pendingIntent).addRemoteInput(remoteInput).setAllowGeneratedReplies(true).build();
    }

    public void showNotification(String qId, String title, String text, String imageUrl, String location, double distance, boolean enableEdit) {
        int id = Integer.parseInt(qId.replace("Q", ""));

        PendingIntent wikipediaPendingIntent = createIntent(NotificationReceiver.ACTION_OPEN_WIKIPEDIA, id, qId);
        PendingIntent wikidataPendingIntent = createIntent(NotificationReceiver.ACTION_OPEN_WIKIDATA, id, qId);
        PendingIntent mapPendingIntent = createIntent(NotificationReceiver.ACTION_OPEN_MAP, id, location);

        try {
            if (imageUrl == null) {
                imageUrl = IMAGE_PLACEHOLDER_URL;
            }

            URL url = new URL(imageUrl);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_room_24px)
                    .setContentTitle(title + " (" + Format.distance(distance) + ")")
                    .setContentText(text)
                    .setContentIntent(mapPendingIntent)
                    .setLargeIcon(bmp)
                    .addAction(R.drawable.ic_launcher_background, "Wikipedia",
                            wikipediaPendingIntent)
                    .addAction(R.drawable.ic_launcher_background, "Wikidata",
                            wikidataPendingIntent)
                    .addAction(R.drawable.ic_launcher_background, "Map",
                            mapPendingIntent)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bmp)
                            .bigLargeIcon(null));

            if (enableEdit)
                builder.addAction(createEditAction(id, qId));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(id, builder.build());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public android.app.Notification createServiceNotification() {
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
