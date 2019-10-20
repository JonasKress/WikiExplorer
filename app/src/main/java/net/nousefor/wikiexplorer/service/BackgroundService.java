package net.nousefor.wikiexplorer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import net.nousefor.wikiexplorer.MainActivity;
import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.api.QueryItems;
import net.nousefor.wikiexplorer.location.LocationListener;
import net.nousefor.wikiexplorer.model.item.Item;
import net.nousefor.wikiexplorer.model.item.ItemNotificationBuilder;
import net.nousefor.wikiexplorer.model.query.Query;
import net.nousefor.wikiexplorer.notification.Notifications;
import net.nousefor.wikiexplorer.notification.NotificationsGeneratorThread;

import java.util.List;

public class BackgroundService extends Service {
    private static final String PREFERENCES_NAME = "BACKGROUND_SERVICE_PREFERENCES";

    static BackgroundService instance = null;

    LocationListener locationListener;
    NotificationsGeneratorThread notificationsGeneratorThread;
    Notifications notifications;
    Query query;

    boolean isDestroyed = false;

    //preferences
    private boolean enableWikipediaSummary = false;
    private boolean enableEditing = false;
    private boolean enableUniqueMode = false;

    int maximumNotifications = 0;
    int locationUpdateInterval = 5;
    private ItemNotificationBuilder itemNotificationBuilder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (MainActivity.getInstance() != null)
                query = MainActivity.getInstance().getQuery();

            if (locationResult == null || query == null) {
                return;
            }

            for (Location location : locationResult.getLocations()) {
                if (isDestroyed)
                    break;

                Log.d("main", "Lat: " + location.getLatitude() + "Long: " + location.getLongitude());
                QueryItems api = new QueryItems(getString(R.string.SPARQLEndpoint));
                List<Item> resultSet = api.execute(query.sparql, location);

                if (isDestroyed)
                    break;

                if (maximumNotifications > 0 && resultSet.size() > maximumNotifications)
                    resultSet = resultSet.subList(0, maximumNotifications + 1);


                for (Item item : resultSet) {
                    if (isSkippedNotification(item.id))
                        continue;

                    itemNotificationBuilder.setEnableEditing(enableEditing);
                    itemNotificationBuilder.setEnableWikipediaSummary(enableWikipediaSummary);

                    notificationsGeneratorThread.addNotification(itemNotificationBuilder.build(item, BackgroundService.this.query));

                }
            }
        }
    };

    public static void resetNotificationMemory(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    private boolean isInNotificationMemory(String qId) {
        SharedPreferences settings = this.getSharedPreferences(PREFERENCES_NAME, 0);
        return settings.getBoolean(qId, false);
    }

    private void addToNotificationMemory(String qId) {
        SharedPreferences settings = this.getSharedPreferences(PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(qId, true);
        editor.apply();
    }

    private boolean isSkippedNotification(String qId) {
        if (!enableUniqueMode)
            return false;

        if (isInNotificationMemory(qId))
            return true;

        addToNotificationMemory(qId);
        return false;
    }

    @Override
    public void onCreate() {
        itemNotificationBuilder = new ItemNotificationBuilder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;

        updatePreferences();
        notifications = new Notifications(this);
        locationListener = new LocationListener(BackgroundService.this, locationCallback, locationUpdateInterval * 60 * 1000);
        notificationsGeneratorThread = new NotificationsGeneratorThread(getBaseContext());
        notificationsGeneratorThread.start();

        startForeground(1, notifications.createBackgroundServiceNotification());
        return START_STICKY;
    }

    public void updatePreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        locationUpdateInterval = sharedPreferences.getInt(getString(R.string.notifications_location_update_interval), locationUpdateInterval);
        maximumNotifications = sharedPreferences.getInt(getString(R.string.notifications_amount_max), maximumNotifications);

        enableUniqueMode = sharedPreferences.getBoolean(getString(R.string.notifications_unique), enableUniqueMode);
        enableEditing = sharedPreferences.getBoolean(getString(R.string.notifications_enable_edit), enableEditing);
        enableWikipediaSummary = sharedPreferences.getBoolean(getString(R.string.notifications_wikipedia_summary), enableWikipediaSummary);
    }

    @Override
    public void onDestroy() {
        if (locationListener != null)
            locationListener.stop();

        if (notificationsGeneratorThread != null)
            notificationsGeneratorThread.setDestroyed(true);

        isDestroyed = true;
        instance = null;
    }


    public static BackgroundService getInstance() {
        return instance;
    }


}
