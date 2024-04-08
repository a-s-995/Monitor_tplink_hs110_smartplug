package scheffold.antoine.monitorsmartplugtp_link.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

import scheffold.antoine.monitorsmartplugtp_link.FindTpLink;
import scheffold.antoine.monitorsmartplugtp_link.R;
import scheffold.antoine.monitorsmartplugtp_link.util.TpLinkSmartPlugCommandsHelper;

public class MonitorSmartPlugEnergyConsumptionService extends IntentService {

    private static final String TAG = MonitorSmartPlugEnergyConsumptionService.class.getSimpleName();
    private static final String ACTION_START_FOREGROUND = "ACTION_START_FOREGROUND";
    private static final String ACTION_STOP_FOREGROUND = "ACTION_STOP_FOREGROUND";
    private static final String EXTRA_THRESHOLD_WATT = "EXTRA_THRESHOLD_WATT";
    private static final String NOTIFICATION_CHANNEL_1 = "NOTIFICATION_CHANNEL_1";
    private static final int CHANNEL_ID = 83;
    boolean querySmartPlug;
    private NotificationCompat.Builder notificationBuilder;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock mWifiLock;

    public MonitorSmartPlugEnergyConsumptionService() {
        super(TAG);
    }

    public static void startActionFoo(Context context, int thresholdInWatt) {
        Intent intent = new Intent(context, MonitorSmartPlugEnergyConsumptionService.class);
        intent.setAction(ACTION_START_FOREGROUND);
        intent.putExtra(EXTRA_THRESHOLD_WATT, thresholdInWatt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MC2::ForegroundServiceWakeLock");
        wakeLock.acquire(30*60*1000L /*30 minutes*/);
        final WifiManager mgr = (WifiManager) getApplicationContext()
                .getSystemService(WIFI_SERVICE);

        mWifiLock = mgr.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, null);
        mWifiLock.acquire();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mWifiLock.release();
        wakeLock.release();
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_FOREGROUND.equals(action)) {
                createNotificationChannelIfNeeded();
                startForeground(CHANNEL_ID, buildNotification());

                FindTpLink findTpLink = new FindTpLink(new FindTpLink.Callback() {
                    @Override
                    public void setFoundDeviceIp(String result) {
                        final int thresholdWatt = intent.getIntExtra(EXTRA_THRESHOLD_WATT, 0);
                        Log.d(TAG, "callingHandleActionFoo");
                        handleActionFoo(result, thresholdWatt);
                    }

                    @Override
                    public void setError(Exception error) {
                        // write error into
//                        errorLiveData.postValue(error);
                    }
                });
                findTpLink.findTpLinkSmartPlugDevice(getApplicationContext());
            }
            if (ACTION_STOP_FOREGROUND.equals(action)) {
                stopForegroundService();
            }
        }
    }

    private void handleActionFoo(String ip, int thresholdInWatt) {
        int fiveMins = 5 * 60 * 1000;
        querySmartPlug = true;
        // TODO: 12.06.23 do not use while true loop 
        while(querySmartPlug) {
            try {
                Log.d(TAG, "Querrying Smart Plug" );
                String answerFromSmartPlug = TpLinkSmartPlugCommandsHelper.querySmartPlugMeter(ip);
                Log.d(TAG, "parsing WATT");
                int currentlyUsedWattInMillis = TpLinkSmartPlugCommandsHelper.parseConsumingWatt(answerFromSmartPlug);
                updateNotificationText(String.valueOf(currentlyUsedWattInMillis / 1000));
                if (currentlyUsedWattInMillis < thresholdInWatt * 1000) {
                    TpLinkSmartPlugCommandsHelper.stopSmartPlug(ip);
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
                String notificationChannelName = getString(R.string.channel_name);
                String notificationChannelDescription = getString(R.string.channel_description);
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_1,
                        notificationChannelName, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(notificationChannelDescription);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_1)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.currently_power_consumption))
                .setContentText(getString(R.string.initial_notification_text))
                .setContentIntent(createNotificationIntent())
                .setShowWhen(false)
                .setLocalOnly(true)
                .setColor(ContextCompat.getColor(this, R.color.black));
        return notificationBuilder.build();
    }

    private void updateNotificationText(String newText) {
        if (notificationBuilder != null) {
            notificationBuilder.setContentText(newText);
            NotificationManagerCompat.from(this).notify(83, notificationBuilder.build());
        }
    }
    private PendingIntent createNotificationIntent() {
        Intent intent = new Intent(this, MonitorSmartPlugEnergyConsumptionService.class);
        intent.setAction("erase");
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
    }
}