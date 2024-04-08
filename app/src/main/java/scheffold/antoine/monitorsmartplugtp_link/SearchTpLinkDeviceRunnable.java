package scheffold.antoine.monitorsmartplugtp_link;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import scheffold.antoine.monitorsmartplugtp_link.util.TpLinkSmartPlugCommandsHelper;

public class SearchTpLinkDeviceRunnable implements Runnable{
        private static final int TIMEOUT_MS = 400;
        private final String host;
        private final ReentrantLock lock;
        private final List<String> foundIps;
        private final String TAG = SearchTpLinkDeviceRunnable.class.getSimpleName();

        public SearchTpLinkDeviceRunnable(String host, ReentrantLock lock, List<String> foundIps) {
            this.host = host;
            this.lock = lock;
            this.foundIps = foundIps;
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "run: with ip address " + host);
                InetAddress inetAddress = InetAddress.getByName(host);
                boolean isReachable = inetAddress.isReachable(TIMEOUT_MS);
                if (isReachable) {
                    String systemInfo = TpLinkSmartPlugCommandsHelper.getSystemInfo(host);
                    if (systemInfo.contains("HS110")) {
                        this.lock.lock();
                        this.foundIps.add(host);
                        this.lock.unlock();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
