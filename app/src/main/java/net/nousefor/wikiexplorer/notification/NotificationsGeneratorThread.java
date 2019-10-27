package net.nousefor.wikiexplorer.notification;

import android.content.Context;

import net.nousefor.wikiexplorer.model.item.ItemNotification;

import java.util.ArrayList;

public class NotificationsGeneratorThread extends Thread {

    private Context context;
    private Notifications notifications;
    private ArrayList<ItemNotification> items = new ArrayList<>();

    private boolean isDestroyed = false;

    public NotificationsGeneratorThread(Context context) {
        this.context = context;
        notifications = new Notifications(context);
    }


    public void addNotification(ItemNotification notification) {
        this.items.add(notification);
    }


    private void createNotification(ItemNotification item) {
        notifications.showItemNotification(item);
    }

    private boolean isDestroyed() {
        if (Thread.currentThread().isInterrupted()) {
            return true;
        }

        return isDestroyed;
    }

    public NotificationsGeneratorThread setDestroyed(boolean destroyed) {
        isDestroyed = destroyed;
        return this;
    }


    private void doSleep(long s) {
        try {
            Thread.sleep(s);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        super.run();

        while (true) {
            if (items.size() == 0) {
                doSleep(2000);
                continue;
            }

            if (isDestroyed())
                break;

            createNotification(items.get(0));
            items.remove(0);

            doSleep(1000);
        }
    }

}
