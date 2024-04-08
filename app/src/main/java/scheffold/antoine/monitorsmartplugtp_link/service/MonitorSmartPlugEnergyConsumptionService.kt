package scheffold.antoine.monitorsmartplugtp_link.service

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import scheffold.antoine.monitorsmartplugtp_link.FindTpLink
import scheffold.antoine.monitorsmartplugtp_link.R
import scheffold.antoine.monitorsmartplugtp_link.util.TpLinkSmartPlugCommandsHelper.parseConsumingWatt
import scheffold.antoine.monitorsmartplugtp_link.util.TpLinkSmartPlugCommandsHelper.querySmartPlugMeter
import scheffold.antoine.monitorsmartplugtp_link.util.TpLinkSmartPlugCommandsHelper.stopSmartPlug
import java.io.IOException

class MonitorSmartPlugEnergyConsumptionService : IntentService(TAG) {
    var querySmartPlug = false
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var wakeLock: WakeLock? = null
    private var mWifiLock: WifiLock? = null

    override fun onCreate() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MC2::ForegroundServiceWakeLock"
        )
        wakeLock?.acquire(30 * 60 * 1000L /*30 minutes*/)
        val mgr = applicationContext
            .getSystemService(WIFI_SERVICE) as WifiManager
        mWifiLock = mgr.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, null)
        mWifiLock?.acquire()
        super.onCreate()
    }

    override fun onDestroy() {
        mWifiLock?.release()
        wakeLock?.release()
        super.onDestroy()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_START_FOREGROUND == action) {
                createNotificationChannelIfNeeded()
                startForeground(CHANNEL_ID, buildNotification())
                val thresholdWatt = intent.getIntExtra(EXTRA_THRESHOLD_WATT, 0)
                val ipAddress = intent.getStringExtra(EXTRA_IP_ADDRESS)
                ipAddress?.let {
                    handleActionFoo(it, thresholdWatt)
                    return
                }
                val findTpLink = FindTpLink(object : FindTpLink.Callback {
                    override fun setFoundDeviceIp(result: String?) {
                        Log.d(TAG, "callingHandleActionFoo")
                        handleActionFoo(result, thresholdWatt)
                    }

                    override fun setError(error: Exception?) {
                        // write error into
//                        errorLiveData.postValue(error);
                    }
                })
                findTpLink.findTpLinkSmartPlugDevice(applicationContext)
            }
            if (ACTION_STOP_FOREGROUND == action) {
                stopForegroundService()
            }
        }
    }

    private fun handleActionFoo(ip: String?, thresholdInWatt: Int) {
        val towMins = 2 * 60 * 1000
        querySmartPlug = true
        // TODO: 12.06.23 do not use while true loop 
        while (querySmartPlug) {
            try {
                Log.d(TAG, "Querrying Smart Plug")
                val answerFromSmartPlug = querySmartPlugMeter(
                    ip!!
                )
                Log.d(TAG, "parsing WATT")
                val currentlyUsedWattInMillis = parseConsumingWatt(answerFromSmartPlug)
                updateNotificationText((currentlyUsedWattInMillis / 1000).toString())
                if (currentlyUsedWattInMillis < thresholdInWatt * 1000) {
                    stopSmartPlug(ip)
                    querySmartPlug = false
                    break
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: IOException) {
                Log.e(TAG, "handleActionFoo: ",e )
            }
            Thread.sleep(towMins.toLong())
        }
        stopForegroundService()
    }

    private fun stopForegroundService() {
        // Stop foreground service and remove the notification.
        stopForeground(true)
        // Stop the foreground service.
        stopSelf()
    }

    fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            if (notificationManager != null) {
                val notificationChannelName = getString(R.string.channel_name)
                val notificationChannelDescription = getString(R.string.channel_description)
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_1,
                    notificationChannelName, NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.description = notificationChannelDescription
                channel.enableLights(false)
                channel.enableVibration(false)
                channel.setShowBadge(true)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun buildNotification(): Notification {
        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_1)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(getString(R.string.currently_power_consumption))
            .setContentText(getString(R.string.initial_notification_text))
            .setContentIntent(createNotificationIntent())
            .setShowWhen(false)
            .setLocalOnly(true)
            .setColor(ContextCompat.getColor(this, R.color.black))
        return notificationBuilder!!.build()
    }

    private fun updateNotificationText(newText: String) {
        if (notificationBuilder != null) {
            notificationBuilder!!.setContentText(newText)
            NotificationManagerCompat.from(this).notify(83, notificationBuilder!!.build())
        }
    }

    private fun createNotificationIntent(): PendingIntent {
        val intent = Intent(this, MonitorSmartPlugEnergyConsumptionService::class.java)
        intent.setAction("erase")
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private val TAG = MonitorSmartPlugEnergyConsumptionService::class.java.simpleName
        private const val ACTION_START_FOREGROUND = "ACTION_START_FOREGROUND"
        private const val ACTION_STOP_FOREGROUND = "ACTION_STOP_FOREGROUND"
        private const val EXTRA_THRESHOLD_WATT = "EXTRA_THRESHOLD_WATT"
        private const val EXTRA_IP_ADDRESS = "EXTRA_IP_ADDRESS"
        private const val NOTIFICATION_CHANNEL_1 = "NOTIFICATION_CHANNEL_1"
        private const val CHANNEL_ID = 83
        fun startActionFoo(context: Context, thresholdInWatt: Int, ipAddress: String?) {
            val intent = Intent(context, MonitorSmartPlugEnergyConsumptionService::class.java)
            intent.setAction(ACTION_START_FOREGROUND)
            intent.putExtra(EXTRA_THRESHOLD_WATT, thresholdInWatt)
            ipAddress?.let { intent.putExtra(EXTRA_IP_ADDRESS, it) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}