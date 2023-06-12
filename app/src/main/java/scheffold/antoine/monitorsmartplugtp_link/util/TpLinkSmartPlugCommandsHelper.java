package scheffold.antoine.monitorsmartplugtp_link.util;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import scheffold.antoine.monitorsmartplugtp_link.Commands;

public class TpLinkSmartPlugCommandsHelper {

    public static final int TP_LINK_COMMUNICATION_PORT = 9999;

    public static int parseConsumingWatt(String toParse) {
        String[] seperatedByCommas = toParse.split(",");
        for (String str : seperatedByCommas) {
            if (str.contains("power_mw")) {
                String[] powerAndWatt = str.split(":");
                System.out.println(Arrays.toString(powerAndWatt));
                return Integer.parseInt(powerAndWatt[1]);
            }
        }
        throw new IllegalStateException("No Watt consumption in String");
    }

    public static String getSystemInfo(String ip) throws IOException {
        String cmd = Commands.COMMANDS.get("info");
        return queryCommand(ip, cmd);
    }

    public static String querySmartPlugMeter(String ip) throws IOException {
        String cmd = Commands.COMMANDS.get("energy");
        return queryCommand(ip, cmd);
    }

    public static String stopSmartPlug(String ip) throws IOException {
        String cmd = Commands.COMMANDS.get("off");
        return queryCommand(ip, cmd);
    }

    public static String startSmartPlug(String ip) throws IOException {
        String cmd = Commands.COMMANDS.get("on");
        return queryCommand(ip, cmd);
    }

    public static String configureWifi(String ip, String ssid, String password) throws IOException {
        String cmd = Commands.COMMANDS.get("configwifi");
        String formattedCmd = String.format(cmd, ssid, password);
        return queryCommand(ip, formattedCmd);
    }

    // TODO: 23.05.23 configure WIFI of smartplug

    @NonNull
    private static String queryCommand(String ip, String cmd) throws IOException {
        Socket socket = new Socket(ip, TP_LINK_COMMUNICATION_PORT);
        byte[] encryptedCmd = encrypt(cmd.getBytes());
        OutputStream socketOutputStream = socket.getOutputStream();
        socketOutputStream.write(encryptedCmd);
        socketOutputStream.flush();
        InputStream inputStream = socket.getInputStream();
        byte[] answer = new byte[2048];
        int a = inputStream.read(answer);
        socket.close();
        byte[] decryptedAnswer = decrypt(answer);
        byte[] smallerArray = cutOffArrayFromIndex(decryptedAnswer, a);
        return new String(smallerArray, StandardCharsets.UTF_8);
    }

    // Encryption and Decryption of TP-Link Smart Home Protocol
// XOR Autokey Cipher with starting key = 171
    private static byte[] decrypt (byte[] bytArr) {
        byte[] tmparr = new byte[bytArr.length];
        if (bytArr.length > 0) {
            int key = 171;
            for (int i = 4; i < bytArr.length; i++) {
                byte b = (byte) (key ^ bytArr[i]);
                key =  bytArr[i];
                tmparr[i] = b;
            }
        }
        return tmparr;
    }

    private static byte[] encrypt (byte[] bArr) {
        byte [] tmpArr = new byte[bArr.length + 4];
        byte[] otherArr = ByteBuffer.allocate(4).putInt(bArr.length).array();
        System.arraycopy(otherArr, 0, tmpArr, 0, otherArr.length);
        if (bArr.length > 0) {
            int key = 171;
            for (int i = 0; i < bArr.length; i++) {
                byte b = (byte) (key ^ bArr[i]);
                key =  b;
                tmpArr[i+4] = b;
            }
        }
        return tmpArr;
    }

    private static byte[] cutOffArrayFromIndex(byte[] decryptedAnswer, int length) {
        byte[] smallerArray = new byte[length];
        System.arraycopy(decryptedAnswer, 0, smallerArray, 0, length);
        return smallerArray;
    }


}
