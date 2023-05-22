package scheffold.antoine.monitorsmartplugtp_link;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class MyIntentService extends IntentService {

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FETCH_NET_ITEM = "scheffold.antoine.monitorsmartplugtp_link.action.FOO";
    private static final String ACTION_BAZ = "scheffold.antoine.monitorsmartplugtp_link.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "scheffold.antoine.monitorsmartplugtp_link.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "scheffold.antoine.monitorsmartplugtp_link.extra.PARAM2";
    private static final String TAG = "MyIntentService";
    boolean querySmartPlug;

    public MyIntentService() {
        super("MyIntentService");
    }

    public static void startActionFoo(Context context, String ip, int thresholdInWatt) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_FETCH_NET_ITEM);
        intent.putExtra(EXTRA_PARAM1, ip);
        intent.putExtra(EXTRA_PARAM2, thresholdInWatt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }else {
            context.startService(intent);
        }
    }

    public static void startActionBazz(Context context) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_FETCH_NET_ITEM);
        context.startService(intent);

    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_NET_ITEM.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final int param2 = intent.getIntExtra(EXTRA_PARAM2, 0);
                Log.d(TAG, "callingHandleActionFoo");
                handleActionFoo(param1, param2);
            }
            if (ACTION_BAZ.equals(action)) {
                stopForegroundService();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String ip, int thresholdInWatt) {
        createNotificationChannelIfNeeded();
        startForeground(83, buildNotification());

        Log.d(TAG, "builded notificatioN");
        int port = 9999;
        int fiveMins = 5 * 60 * 1000;
        querySmartPlug = true;
        while(querySmartPlug) {
            try {
                Log.d(TAG, "Querrying Smart Plug" );
                String answerFromSmartPlug = Tp_Link_SmartPLug_Hacking.querySmartPlug(ip, port);
                Log.d(TAG, "parsing WATT");
                int currentlyUsedWattInMillis = Tp_Link_SmartPLug_Hacking.parseConsumingWatt(answerFromSmartPlug);
                if (currentlyUsedWattInMillis < thresholdInWatt * 1000) {
                    Tp_Link_SmartPLug_Hacking.stopSmartPlug(ip, port);
                    querySmartPlug = false;
                    break;
                }
                Thread.sleep(fiveMins);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                querySmartPlug = false;
            }
        }
        stopForegroundService();
    }
    private void stopForegroundService()
    {
        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }



    public void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                String notificationChannelName = "getString(R.string.notification_browsing_session_channel_name)";
                String notificationChannelDescription = "getString(R.string.notification_browsing_session_channel_description, new Object[]{getString(R.string.app_name)})";
                NotificationChannel channel = new NotificationChannel("browsing-session", notificationChannelName, NotificationManager.IMPORTANCE_MIN);
                channel.setImportance(NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(notificationChannelDescription);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, "browsing-session")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("getString(R.string.notification_erase_text)")
                .setContentIntent(createNotificationIntent())
              //  .setVisibility(-1)
                .setShowWhen(false)
                .setLocalOnly(true)
                .setColor(ContextCompat.getColor(this, R.color.black))
                .build();
    }

    private PendingIntent createNotificationIntent() {
        Intent intent = new Intent(this, MyIntentService.class);
        intent.setAction("erase");
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    }
}