package net.nousefor.wikiexplorer.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.api.SparqlQuery;
import net.nousefor.wikiexplorer.api.Wikidata;
import net.nousefor.wikiexplorer.location.LocationListener;
import net.nousefor.wikiexplorer.model.Item;
import net.nousefor.wikiexplorer.notification.Notification;

import java.util.ArrayList;

public class BackgroundService extends Service {

    static BackgroundService instance = null;

    public static final String EXTRA_SPARQL = "SPARQL";
    LocationListener locationListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    String sparql = null;
    Notification notification = null;

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }

    //preferences
    boolean enableWikipediaSummary = false;
    boolean enableUniqueMode = false;
    boolean enableEditing = false;
    int maximumNotifications = 0;
    int locationUpdateInterval = 5;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null || sparql == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                Log.d("main", "Lat: " + location.getLatitude() + "Long: " + location.getLongitude());

                SparqlQuery query = new SparqlQuery(getString(R.string.SPARQLEndpoint));
                createNotificationsFromItems(query.execute(sparql, location));
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean enableLabelEditing(String id) {
        Wikidata w = new Wikidata();

        if (!enableEditing) {
            return false;
        }

        return w.getLabel(id, "en") == null;
    }

    private void createNotificationsFromItems(final ArrayList<Item> items) {
        Log.d(this.toString(), "Create notifications:" + items.size());

        (new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
            public void run() {
                for (int i = 0; i < items.size(); i++) {
                    Item item = items.get(i);

                    notification.showNotification(
                            item.id,
                            item.label,
                            item.description,
                            item.imageUrl,
                            item.location,
                            item.distance,
                            enableLabelEditing(item.id));

                    if (maximumNotifications > 0) {
                        if (i + 1 >= maximumNotifications)
                            break;
                    }

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();
    }


    @Override
    public void onCreate() {

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        sparql = intent.getStringExtra(EXTRA_SPARQL);
        updatePreferences();


        notification = new Notification(this);
        locationListener = new LocationListener(BackgroundService.this, locationCallback, locationUpdateInterval * 60 * 1000);

        startForeground(1, notification.createServiceNotification());
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
        locationListener.stop();
    }


    public static BackgroundService getInstance() {
        return instance;
    }

}
