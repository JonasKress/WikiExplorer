package net.nousefor.wikiexplorer.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import net.nousefor.wikiexplorer.MainActivity;
import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.service.BackgroundService;


public class NotificationPreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new Fragment())
                .commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    public boolean onOptionsItemSelected(MenuItem item) {
        updateBackgroundService();

        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

    private void updateBackgroundService() {
        if (BackgroundService.getInstance() != null)
            BackgroundService.getInstance().updatePreferences();
    }

    public static class Fragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.notifications_preferences, rootKey);


            initMemoryClearButton();
            initEnableEditSwitch();
        }

        private void initEnableEditSwitch() {
            final SwitchPreferenceCompat sw = findPreference(getString(R.string.notifications_enable_edit));

            sw.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (!sw.isChecked()) {
                        createConfirmWikidataEditsDialog(sw);
                        return false;
                    }
                    return true;
                }
            });
        }

        private void createConfirmWikidataEditsDialog(final SwitchPreferenceCompat sw) {
            TextView msg = new TextView(getContext());
            msg.setText(Html.fromHtml(getString(R.string.ui_notification_edit_warning_dialog_description)));
            msg.setMovementMethod(LinkMovementMethod.getInstance());
            msg.setClickable(true);

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sw.setChecked(true);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sw.setChecked(false);
                        }
                    }).create();
            dialog.setView(msg, 50, 50, 50, 50);
            dialog.show();
        }

        private void initMemoryClearButton() {
            Preference button = findPreference(getString(R.string.notifications_memory_clear));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getContext(), getString(R.string.ui_notification_preferences_memory_clear), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
