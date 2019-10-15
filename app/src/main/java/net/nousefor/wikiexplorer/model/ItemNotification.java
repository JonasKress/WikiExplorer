package net.nousefor.wikiexplorer.model;

import android.graphics.Bitmap;

import androidx.core.app.NotificationCompat;

public class ItemNotification {

    public int notificationId;

    public String titleCollapsed;
    public String descriptionCollapsed;
    public Bitmap imageCollapsed;

    public String titleExpanded;
    public String descriptionExpanded;
    public Bitmap imageExpanded;

    public String location;
    public String wikipediaUrl;
    public String itemId;
    public String imageUrl;

    public NotificationCompat.Action[] actions;
}
