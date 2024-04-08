package scheffold.antoine.monitorsmartplugtp_link

import android.util.Log
import scheffold.antoine.monitorsmartplugtp_link.util.TpLinkSmartPlugCommandsHelper
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.locks.ReentrantLock

class SearchTpLinkDeviceRunnable(
    private val host: String,
    private val lock: ReentrantLock,
    private val foundIps: MutableList<String>
) : Runnable {
    private val TAG = SearchTpLinkDeviceRunnable::class.java.simpleName
    override fun run() {
        try {
            Log.d(TAG, "run: with ip address $host")
            val inetAddress = InetAddress.getByName(host)
            val isReachable = inetAddress.isReachable(TIMEOUT_MS)
            if (isReachable) {
                val systemInfo = TpLinkSmartPlugCommandsHelper.getSystemInfo(host)
                if (systemInfo.contains("HS110")) {
                    lock.lock()
                    foundIps.add(host)
                    lock.unlock()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TIMEOUT_MS = 400
    }
}
