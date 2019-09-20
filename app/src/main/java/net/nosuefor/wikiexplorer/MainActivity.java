package net.nosuefor.wikiexplorer;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import net.nosuefor.wikiexplorer.helper.Format;
import net.nosuefor.wikiexplorer.model.Query;
import net.nosuefor.wikiexplorer.query.QueryList;
import net.nosuefor.wikiexplorer.service.BackgroundService;

import java.util.ArrayList;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    ArrayList<Query> queries = null;
    private int selectedQueryIndex = 0;
    private double distance = 100;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handlePermissions();
        loadQueries();

        initQuerySelector();
        initDistance();
        initEnableNotificationsSwitch();
        initPreview();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.settingsLayout).setVisibility(View.GONE);
            findViewById(R.id.divider).setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            findViewById(R.id.settingsLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.divider).setVisibility(View.VISIBLE);
        }
    }

    void handlePermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_LONG).show();
        } else {
            EasyPermissions.requestPermissions(this, "", 42, perms);
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

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void update() {
        if (BackgroundService.getInstance() != null)
            BackgroundService.getInstance().setSparql(getQuery());

        updateDistanceLabel();
        initPreview();
    }

    private void updateDistanceLabel() {
        TextView label = findViewById(R.id.distanceTextView);
        label.setText(getText(R.string.ui_label_distance) + " " + Format.distance((long) distance));
    }

    private String getQuery() {

        String query = null;

        if (queries.size() == 0) {
            query = getString(R.string.query);
        } else {
            query = queries.get(selectedQueryIndex).query;
        }

        query = query.replaceAll("\"\\[AUTO\\_LANGUAGE\\]\"", Locale.getDefault().getLanguage())
                .replaceAll("\"\\[RADIUS\\]\"", (distance / 1000) + "");

        return query;
    }

    private void initQuerySelector() {
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
    }

    private void initPreview() {
        WebView view = findViewById(R.id.webview);
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setGeolocationEnabled(true);
        view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        view.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        view.loadUrl((getString(R.string.SPARQLEmbed) + getQuery()).replace("{rand}", ("" + Math.random())));
    }

    private void initDistance() {
        SeekBar bar = findViewById(R.id.distanceSeekBar);

        bar.setProgress((int) Math.round(Math.log(distance)));
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
                // TODO Auto-generated method stub
                distance = Math.pow(progress + 1, 2);
                updateDistanceLabel();
            }
        });
    }

    private void initEnableNotificationsSwitch() {

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
                }
            }
        });

    }
}
