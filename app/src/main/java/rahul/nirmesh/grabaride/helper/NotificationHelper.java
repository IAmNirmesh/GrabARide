package rahul.nirmesh.grabaride.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import rahul.nirmesh.grabaride.R;

/**
 * Created by NIRMESH on 12-Apr-18.
 */

public class NotificationHelper extends ContextWrapper {

    private static final String GRAB_CHANNEL_ID = "rahul.nirmesh.grabaride";
    private static final String GRAB_CHANNEL_NAME = "Grab Ride";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel grabChannels = new NotificationChannel(GRAB_CHANNEL_ID,
                                                                    GRAB_CHANNEL_NAME,
                                                                    NotificationManager.IMPORTANCE_DEFAULT);

        grabChannels.enableLights(true);
        grabChannels.enableVibration(true);
        grabChannels.setLightColor(Color.GRAY);
        grabChannels.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(grabChannels);
    }

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getGrabNotification(String title, String content,
                                                    PendingIntent contentIntent, Uri soundUri) {
        return new Notification.Builder(getApplicationContext(), GRAB_CHANNEL_ID)
                                            .setContentText(content)
                                            .setContentTitle(title)
                                            .setAutoCancel(true)
                                            .setSound(soundUri)
                                            .setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.ic_car);
    }
}
