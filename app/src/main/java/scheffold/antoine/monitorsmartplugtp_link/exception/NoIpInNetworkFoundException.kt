package scheffold.antoine.monitorsmartplugtp_link.exception

class NoIpInNetworkFoundException : Exception() {
    override val message: String
        get() = "No TP-Link smart plug found in current network"
}
