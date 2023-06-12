package scheffold.antoine.monitorsmartplugtp_link;

import java.util.HashMap;
import java.util.Map;

public class Commands {

    public static final Map<String, String> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put("info", "{\"system\":{\"get_sysinfo\":{}}}");
        COMMANDS.put("on", "{\"system\":{\"set_relay_state\":{\"state\":1}}}");
        COMMANDS.put("off", "{\"system\":{\"set_relay_state\":{\"state\":0}}}");
        COMMANDS.put("ledoff", "{\"system\":{\"set_led_off\":{\"off\":1}}}");
        COMMANDS.put("ledon", "{\"system\":{\"set_led_off\":{\"off\":0}}}");
        COMMANDS.put("cloudinfo", "{\"cnCloud\":{\"get_info\":{}}}");
        COMMANDS.put("wlanscan", "{\"netif\":{\"get_scaninfo\":{\"refresh\":0}}}");
        COMMANDS.put("time", "{\"time\":{\"get_time\":{}}}");
        COMMANDS.put("schedule", "{\"schedule\":{\"get_rules\":{}}}");
        COMMANDS.put("countdown", "{\"count_down\":{\"get_rules\":{}}}");
        COMMANDS.put("antitheft", "{\"anti_theft\":{\"get_rules\":{}}}");
        COMMANDS.put("reboot", "{\"system\":{\"reboot\":{\"delay\":1}}}");
        COMMANDS.put("reset", "{\"system\":{\"reset\":{\"delay\":1}}}");
        COMMANDS.put("energy", "{\"emeter\":{\"get_realtime\":{}}}");
        COMMANDS.put("configwifi", "{\"netif\":{\"set_stainfo\":{\"ssid\":\"%s\",\"password\":\"%s\",\"key_type\":3}}}");
        COMMANDS.put("scanwifi", "{\"netif\":{\"get_scaninfo\":{\"refresh\":1}}}");
    }
}
