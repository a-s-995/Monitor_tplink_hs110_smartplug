package scheffold.antoine.monitorsmartplugtp_link.exception;

import androidx.annotation.Nullable;

public class NoIpInNetworkFoundException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "No TP-Link smart plug found in current network";
    }
}
