package net.nousefor.wikiexplorer.notification;

import android.content.Context;
import android.content.Intent;

public interface NotificationReceiverAction {
    boolean run(Intent intent, Context context) throws Exception;

    boolean isEditAction();
}
