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

import net.nosuefor.wikiexplorer.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Notification {

    private static final String CHANNEL_ID = "C1";
    private Context context = null;


    public Notification(Context context) {
        this.context = context;
        createNotificationChannel();

    }

    public void showNotification(String entityURI, String title, String text, String imageUrl, String location) {

        int id = Integer.parseInt(entityURI.substring(entityURI.indexOf("/Q") + 2));

        Intent ignoreIntent = new Intent(context, NotificationReceiver.class);
        ignoreIntent.setAction(NotificationReceiver.ACTION_IGNORE);
        ignoreIntent.putExtra("id", "Q" + id);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(context, 0, ignoreIntent, 0);

        Intent wikipediaIntent = new Intent(context, NotificationReceiver.class);
        wikipediaIntent.setAction(NotificationReceiver.ACTION_OPEN_WIKIPEDIA);
        wikipediaIntent.putExtra("id", "Q" + id);
        PendingIntent wikipediaPendingIntent =
                PendingIntent.getBroadcast(context, 0, wikipediaIntent, 0);

        Intent wikidataIntent = new Intent(context, NotificationReceiver.class);
        wikidataIntent.setAction(NotificationReceiver.ACTION_OPEN_WIKIDATA);
        wikidataIntent.putExtra("id", "Q" + id);
        PendingIntent wikidataPendingIntent =
                PendingIntent.getBroadcast(context, 0, wikidataIntent, 0);

        try {
            URL url = null;
            url = new URL(imageUrl);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());


            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setLargeIcon(bmp)
                    .addAction(R.drawable.ic_launcher_background, "Wikipedia",
                            wikipediaPendingIntent)
                    .addAction(R.drawable.ic_launcher_background, "Wikidata",
                            wikidataPendingIntent)
                    .addAction(R.drawable.ic_launcher_background, "Ignore",
                            snoozePendingIntent)
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = "desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
