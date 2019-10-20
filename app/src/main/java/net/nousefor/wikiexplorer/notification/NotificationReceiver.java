package net.nousefor.wikiexplorer.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        int notificationId = extras.getInt(EXTRA_NOTIFICATION_ID, 0);

        try {
            NotificationReceiverAction action = getAction(intent.getAction());
            action.run(intent, context);
            if (action.isEditAction())
                new Notifications(context).showAfterEditNotification(notificationId, null);

        } catch (Exception e) {
            new Notifications(context).showAfterEditNotification(notificationId, "Error: " + e.getMessage());
        }

    }

    private NotificationReceiverAction getAction(String className) {
        try {
            return (NotificationReceiverAction) Class.forName(className).newInstance();

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }


}
