package scheffold.antoine.monitorsmartplugtp_link;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import androidx.annotation.NonNull;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import scheffold.antoine.monitorsmartplugtp_link.exception.NoIpInNetworkFoundException;

public class FindTpLink {

    private final Callback callback;

    public FindTpLink (Callback callback) {
        this.callback = callback;
    }

    public void findTpLinkSmartPlugDevice(Context context) {
        String ipBaseAddress = getIpBaseAddress(context);
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        ReentrantLock lock = new ReentrantLock();
        List<String> foundIps = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(255);
        for (int i = 1; i <= 255; i++) {
            String host = ipBaseAddress + i;
            SearchTpLinkDeviceRunnable runnable = new SearchTpLinkDeviceRunnable(host, lock, foundIps);
            executorService.submit(runnable);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
        }
        executorService.shutdown();
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
            callback.setError(new NoIpInNetworkFoundException());
            // show error found no IP found in current network
        } else {
            String foundIp = foundIps.get(0);
            callback.setFoundDeviceIp(foundIp);
        }
    }

    @NonNull
    private String getIpBaseAddress(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        String deviceLocalIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        String[] split = deviceLocalIp.split("\\.");
        StringBuilder ipBaseAddress = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            ipBaseAddress.append(split[i]).append(".");
        }
        return ipBaseAddress.toString();
    }

    public interface Callback {
        void setFoundDeviceIp(String result);

        void setError(Exception error);
    }
}
