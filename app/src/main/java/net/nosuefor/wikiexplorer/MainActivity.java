package net.nosuefor.wikiexplorer;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import net.nosuefor.wikiexplorer.location.LocationListener;
import net.nosuefor.wikiexplorer.notification.Notification;
import net.nosuefor.wikiexplorer.query.Item;
import net.nosuefor.wikiexplorer.query.Query;
import net.nosuefor.wikiexplorer.query.QueryItem;
import net.nosuefor.wikiexplorer.query.QueryList;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private LocationCallback locationCallback;
    ArrayList<QueryItem> queries = null;
    private int selectedQuery = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        queries = QueryList.get();
        populateQuerySelector();

        final Notification notification = new Notification(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d("main", "Location result");
                if (locationResult == null) {
                    Log.d("main", "Location = null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d("main", "Lat: " + location.getLatitude() + "Long: " + location.getLongitude());

                    Query query = new Query(getString(R.string.SPARQLEndpoint));
                    ArrayList<Item> items = query.execute(getQuery(), location);

                    for (int i = 0; i < items.size(); i++) {
                        notification.showNotification(
                                items.get(i).id,
                                items.get(i).label,
                                items.get(i).description,
                                items.get(i).imageUrl,
                                items.get(i).location);
                    }
                }
            }
        };


        new LocationListener(this, locationCallback);

    }

    private void populateQuerySelector() {
        Spinner dropdown = findViewById(R.id.querySelector);
        String[] items = new String[queries.size()];

        for (int i = 0; i < queries.size(); i++) {
            items[i] = queries.get(i).name;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);

        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedQuery = i;

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private String getQuery() {

        if (queries.size() == 0) {
            return getString(R.string.query);
        }

        return queries.get(selectedQuery).query;
    }
}
