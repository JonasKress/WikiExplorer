package net.nousefor.wikiexplorer.model.action;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.google.gson.Gson;

import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.model.item.ItemNotification;
import net.nousefor.wikiexplorer.notification.NotificationReceiver;
import net.nousefor.wikiexplorer.notification.NotificationReceiverAction;

import java.util.Locale;
import java.util.Map;

public abstract class Action implements NotificationReceiverAction {
    static final String EXTRA_DATA = "EXTRA_DATA_STRING";
    static final String REMOTE_INPUT_KEY = "REMOTE_INPUT_KEY";
    public Map<String, String> labels = null;
    public Map<String, String> labelsInput = null;
    private Intent intent;

    abstract boolean execute(String parmsJson, Context context) throws Exception;

    RemoteInput getRemoteInput(Context context) {
        return null;
    }

    Bundle getExtrasBundle(String parmsJson) {
        Bundle extras = new Bundle();
        extras.putString(EXTRA_DATA, parmsJson);
        return extras;
    }

    String getParmsJson(ItemNotification itemNotification) {
        Parms parms = new Parms();
        parms.notificationId = itemNotification.notificationId;
        parms.itemId = itemNotification.itemId;
        return new Gson().toJson(parms);
    }

    String getLabel() {
        String language = Locale.getDefault().getLanguage();
        String label;

        if (labels == null) {
            return "[MISSING]";
        }

        if (labels.containsKey(language)) {
            label = labels.get(language);
        } else
            label = labels.get("en");

        return label;
    }

    String getLabelInput() {
        String language = Locale.getDefault().getLanguage();
        String label;

        if (labels == null) {
            return "[MISSING]";
        }

        if (labelsInput.containsKey(language)) {
            label = labelsInput.get(language);
        } else
            label = labelsInput.get("en");

        return label;
    }

    public NotificationCompat.Action createAction(ItemNotification itemNotification, Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(getClass().getName());
        int icon = R.drawable.ic_launcher_background;

        Bundle extras = getExtrasBundle(getParmsJson(itemNotification));
        extras.putInt(NotificationReceiver.EXTRA_NOTIFICATION_ID, itemNotification.notificationId);
        intent.putExtras(extras);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, itemNotification.notificationId, intent, 0);
        NotificationCompat.Action.Builder notification = new NotificationCompat.Action.Builder(icon, getLabel(), pendingIntent);

        RemoteInput remoteInput = getRemoteInput(context);
        if (remoteInput != null)
            notification.addRemoteInput(remoteInput).setAllowGeneratedReplies(true);

        return notification.build();
    }

    void openIntent(String uri, Context context) {
        Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(openIntent);
    }

    public boolean isEditAction() {
        return false;
    }

    String getRemoteInputValue() {
        Bundle remoteInput = android.app.RemoteInput.getResultsFromIntent(intent);
        return remoteInput.getCharSequence(REMOTE_INPUT_KEY).toString();

    }

    public boolean run(Intent intent, Context context) throws Exception {
        this.intent = intent;
        Bundle extras = intent.getExtras();

        return execute(extras.getString(EXTRA_DATA), context);
    }

    class Parms {
        int notificationId;
        String itemId;
    }

}
