package scheffold.antoine.monitorsmartplugtp_link

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import scheffold.antoine.monitorsmartplugtp_link.exception.NoIpInNetworkFoundException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

class FindTpLink(private val callback: Callback) {
    fun findTpLinkSmartPlugDevice(context: Context) {
        val ipBaseAddress = getIpBaseAddress(context)
        val executorService = Executors.newFixedThreadPool(32)
        val lock = ReentrantLock()
        val foundIps: MutableList<String> = ArrayList()
        val latch = CountDownLatch(255)
        for (i in 1..255) {
            val host = ipBaseAddress + i
            val runnable = SearchTpLinkDeviceRunnable(host, lock, foundIps)
            executorService.submit(runnable)
        }
        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
//            throw new RuntimeException(e);
        }
        executorService.shutdown()
        //        boolean terminatedThreads;
//        try {
//            terminatedThreads = executorService.awaitTermination(10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        if (!terminatedThreads) {
//            callback.setError(new IllegalStateException("Not all Threads are terminated"));
        // show error not terminated, something crashed
        if (false) {
        } else if (foundIps.isEmpty()) {
            callback.setError(NoIpInNetworkFoundException())
            // show error found no IP found in current network
        } else {
            val foundIp = foundIps[0]
            callback.setFoundDeviceIp(foundIp)
        }
    }

    private fun getIpBaseAddress(context: Context): String {
        val wm = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        val deviceLocalIp = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        val split =
            deviceLocalIp.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val ipBaseAddress = StringBuilder()
        for (i in 0..2) {
            ipBaseAddress.append(split[i]).append(".")
        }
        return ipBaseAddress.toString()
    }

    interface Callback {
        fun setFoundDeviceIp(result: String?)
        fun setError(error: Exception?)
    }
}
