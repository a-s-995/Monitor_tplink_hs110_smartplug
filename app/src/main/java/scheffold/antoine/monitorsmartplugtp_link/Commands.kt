package scheffold.antoine.monitorsmartplugtp_link

object Commands {
    @JvmField
    val COMMANDS: MutableMap<String, String> = HashMap()

    init {
        COMMANDS["info"] =
            "{\"system\":{\"get_sysinfo\":{}}}"
        COMMANDS["on"] = "{\"system\":{\"set_relay_state\":{\"state\":1}}}"
        COMMANDS["off"] = "{\"system\":{\"set_relay_state\":{\"state\":0}}}"
        COMMANDS["ledoff"] =
            "{\"system\":{\"set_led_off\":{\"off\":1}}}"
        COMMANDS["ledon"] = "{\"system\":{\"set_led_off\":{\"off\":0}}}"
        COMMANDS["cloudinfo"] = "{\"cnCloud\":{\"get_info\":{}}}"
        COMMANDS["wlanscan"] =
            "{\"netif\":{\"get_scaninfo\":{\"refresh\":0}}}"
        COMMANDS["time"] = "{\"time\":{\"get_time\":{}}}"
        COMMANDS["schedule"] = "{\"schedule\":{\"get_rules\":{}}}"
        COMMANDS["countdown"] = "{\"count_down\":{\"get_rules\":{}}}"
        COMMANDS["antitheft"] =
            "{\"anti_theft\":{\"get_rules\":{}}}"
        COMMANDS["reboot"] = "{\"system\":{\"reboot\":{\"delay\":1}}}"
        COMMANDS["reset"] =
            "{\"system\":{\"reset\":{\"delay\":1}}}"
        COMMANDS["energy"] =
            "{\"emeter\":{\"get_realtime\":{}}}"
        COMMANDS["configwifi"] =
            "{\"netif\":{\"set_stainfo\":{\"ssid\":\"%s\",\"password\":\"%s\",\"key_type\":3}}}"
        COMMANDS["scanwifi"] = "{\"netif\":{\"get_scaninfo\":{\"refresh\":1}}}"
    }
}
