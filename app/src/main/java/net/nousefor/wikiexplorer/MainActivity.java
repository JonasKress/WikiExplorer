package net.nousefor.wikiexplorer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import net.nousefor.wikiexplorer.api.QueryList;
import net.nousefor.wikiexplorer.helper.Format;
import net.nousefor.wikiexplorer.model.Query;
import net.nousefor.wikiexplorer.notification.Notifications;
import net.nousefor.wikiexplorer.preferences.NotificationPreferencesActivity;
import net.nousefor.wikiexplorer.service.BackgroundService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    final static String PREFERENCES_NAME = "WIKI_EXPLORER";
    final static String PREFERENCES_FIELD_SELECTED_QUERY_INDEX = "PREFERENCES_SELECTED_QUERY_INDEX";
    final static String PREFERENCES_FIELD_DISTANCE = "PREFERENCES_FIELD_DISTANCE";

    ArrayList<Query> queries = null;
    int selectedQueryIndex = 0;
    double distance = 100;
    String resultView = "Map";
    Map<Integer, String> resultViews = new HashMap<Integer, String>() {{
        put(R.id.action_map, "Map");
        put(R.id.action_images, "ImageGrid");
        put(R.id.action_graph, "Graph");
        put(R.id.action_tree, "Tree");
        put(R.id.action_chart, "Barchart");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handlePermissions();
        loadQueries();

        readSettings();
        initQuerySelector();
        initDistance();
        initNotificationPreferencesButton();
        initEnableNotificationsSwitch();
        initPreview();
        initNavigationView();
    }

    void readSettings() {
        try {
            SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
            selectedQueryIndex = settings.getInt(PREFERENCES_FIELD_SELECTED_QUERY_INDEX, 0);
            distance = settings.getFloat(PREFERENCES_FIELD_DISTANCE, 200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void saveSettings() {
        try {
            SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();

            editor.putInt(PREFERENCES_FIELD_SELECTED_QUERY_INDEX, selectedQueryIndex);
            editor.putFloat(PREFERENCES_FIELD_DISTANCE, (float) distance);

            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.settingsLayout).setVisibility(View.GONE);
            findViewById(R.id.divider).setVisibility(View.GONE);
            findViewById(R.id.divider2).setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            findViewById(R.id.settingsLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.divider).setVisibility(View.VISIBLE);
            findViewById(R.id.divider2).setVisibility(View.VISIBLE);
        }
    }

    void handlePermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.FOREGROUND_SERVICE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "", 42, perms);
        }

        try {
            int off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (off == 0) {
                Snackbar.make(findViewById(android.R.id.content), "GPS is disabled",
                        Snackbar.LENGTH_LONG)
                        .setAction("Enable", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(onGPS);
                            }
                        }).show();
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    void loadQueries() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        queries = (new QueryList()).get();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    void update() {
        if (BackgroundService.getInstance() != null)
            BackgroundService.getInstance().setSparql(getQuery());

        updateDistanceLabel();
        initPreview();
        saveSettings();
    }

    void updateDistanceLabel() {
        TextView label = findViewById(R.id.distanceTextView);
        label.setText(" " + Format.distance(distance));
    }

    String getQuery() {

        String query = null;

        if (queries.size() == 0) {
            query = getString(R.string.query);
        } else {
            query = queries.get(selectedQueryIndex).query;
        }

        query = query.replaceAll("\\[AUTO\\_LANGUAGE\\]", Locale.getDefault().getLanguage())
                .replaceAll("\"\\[RADIUS\\]\"", (distance / 1000.0) + "");

        Log.d(Thread.currentThread().getStackTrace().toString(), query);
        return query;
    }

    void initQuerySelector() {
        Spinner dropdown = findViewById(R.id.querySelector);
        String[] items = new String[queries.size()];
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedQueryIndex = i;
                update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };

        for (int i = 0; i < queries.size(); i++) {
            items[i] = queries.get(i).name;
        }

        ArrayAdapter adapter = new ArrayAdapter<>(this
                , android.R.layout.simple_spinner_dropdown_item, items);

        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(listener);
        dropdown.setSelection(selectedQueryIndex);
    }

    void initPreview() {
        WebView view = findViewById(R.id.webview);
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setGeolocationEnabled(true);
        view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        view.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        String query = getQuery();
        if (resultView == resultViews.get(R.id.action_chart))
            query = getString(R.string.query_statistics).replace("{QUERY}", query);

        String url = (getString(R.string.SPARQLEmbed) + query)
                .replace("{RAND}", ("" + Math.random()))
                .replace("{VIEW}", resultView);
        view.loadUrl(url);
    }

    void initDistance() {
        final SeekBar bar = findViewById(R.id.distanceSeekBar);
        bar.setProgress(((int) Math.pow(Math.E, Math.log(distance) / 2.5)) + 1);

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                update();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = Math.pow(progress + 1, 2.5);

                updateDistanceLabel();
                bar.setContentDescription(Format.distance(distance));
            }
        });
    }

    void initNotificationPreferencesButton() {
        ImageButton button = findViewById(R.id.notificationPreferencesButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsActivity = new Intent(MainActivity.this, NotificationPreferencesActivity.class);
                startActivity(settingsActivity);
            }
        });
    }

    void initEnableNotificationsSwitch() {
        final MainActivity activity = this;
        Switch sw = findViewById(R.id.enableNotifications);

        if (BackgroundService.getInstance() != null) {
            sw.setChecked(true);
        }

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent intent = new Intent(activity, BackgroundService.class);
                    intent.putExtra(BackgroundService.EXTRA_SPARQL, getQuery());
                    startService(intent);

                } else {
                    Intent intent = new Intent(activity, BackgroundService.class);
                    stopService(intent);
                    (new Notifications(getBaseContext())).removeAll();
                }
            }
        });
    }

    void initNavigationView() {
        BottomNavigationView view = findViewById(R.id.navigationView);
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                resultView = resultViews.get(menuItem.getItemId());
                update();
                return true;
            }
        });
    }
}
