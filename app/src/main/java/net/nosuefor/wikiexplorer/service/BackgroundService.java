package net.nosuefor.wikiexplorer.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import net.nosuefor.wikiexplorer.R;
import net.nosuefor.wikiexplorer.location.LocationListener;
import net.nosuefor.wikiexplorer.model.Item;
import net.nosuefor.wikiexplorer.notification.Notification;
import net.nosuefor.wikiexplorer.query.SparqlQuery;

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

    private void createNotificationsFromItems(final ArrayList<Item> items) {
        (new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < items.size(); i++) {
                    Item item = items.get(i);

                    notification.showNotification(
                            item.id,
                            item.label,
                            item.description,
                            item.imageUrl,
                            item.location,
                            item.distance);

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();

        notification = new Notification(this);
        sparql = intent.getStringExtra(EXTRA_SPARQL);

        locationListener = new LocationListener(BackgroundService.this, locationCallback);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
        locationListener.stop();
    }


    public static BackgroundService getInstance() {
        return instance;
    }

}
