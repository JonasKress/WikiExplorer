package net.nosuefor.wikiexplorer.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import net.nosuefor.wikiexplorer.MainActivity;
import net.nosuefor.wikiexplorer.R;
import net.nosuefor.wikiexplorer.helper.Format;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Notification {

    private static final String CHANNEL_ID = "Wiki Explorer";
    private static final String IMAGE_PLACEHOLDER_URL = "https://upload.wikimedia.org/wikipedia/commons/4/49/Medium_placeholder.png";
    private Context context = null;


    public Notification(Context context) {
        this.context = context;
        createNotificationChannel();

    }

    PendingIntent createIntent(String action, int id, String extra) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(action);
        intent.putExtra(NotificationReceiver.EXTRA_DATA, extra);
        return PendingIntent.getBroadcast(context, id, intent, 0);
    }

    public void showNotification(String entityURI, String title, String text, String imageUrl, String location, double distance) {
        int id = Integer.parseInt(entityURI.substring(entityURI.indexOf("/Q") + 2));

        PendingIntent wikipediaPendingIntent = createIntent(NotificationReceiver.ACTION_OPEN_WIKIPEDIA, id, "Q" + id);
        PendingIntent wikidataPendingIntent = createIntent(NotificationReceiver.ACTION_OPEN_WIKIDATA, id, "Q" + id);
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

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(id, builder.build());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public android.app.Notification createServiceNotification() {
        // Tapping the notification will open the specified Activity.
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_explore_24px)
                // .setContentTitle("Service is running")
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    private NotificationCompat.Builder createBuilder() {
        NotificationCompat.Builder builder = null;

        return builder;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = "desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
