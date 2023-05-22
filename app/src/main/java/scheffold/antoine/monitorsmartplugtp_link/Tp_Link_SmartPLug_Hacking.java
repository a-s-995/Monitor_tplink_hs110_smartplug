package scheffold.antoine.monitorsmartplugtp_link;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Tp_Link_SmartPLug_Hacking {


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

    public static String querySmartPlug(String ip, int port) throws IOException {
        String cmd = "{\"emeter\":{\"get_realtime\":{}}}";
        Socket socket = new Socket(ip, port);
        byte[] encryptedCmd = encrypt(cmd.getBytes());
      //  System.out.println(bytesToHex(encryptedCmd));
        OutputStream socketOutputStream = socket.getOutputStream();
        socketOutputStream.write(encryptedCmd);
        socketOutputStream.flush();

        InputStream inputStream = socket.getInputStream();
        byte[] answer = new byte[2048];
        int a = inputStream.read(answer);
        socket.close();
        System.out.println(a);
        byte[] decryptedAnswer = decrypt(answer);
        byte[] smalerArray = cutOffArrayFromIndex(decryptedAnswer, a);
        return new String(smalerArray, StandardCharsets.UTF_8);
    }

    public static void stopSmartPlug(String ip, int port) throws IOException {
        System.out.println("entering stopsmartplug function");
        String cmd = "{\"system\":{\"set_relay_state\":{\"state\":0}}}";
        Socket socket = new Socket(ip, port);
        byte[] encryptedCmd = encrypt(cmd.getBytes());
        OutputStream socketOutputStream = socket.getOutputStream();
        socketOutputStream.write(encryptedCmd);
        socketOutputStream.flush();

        InputStream inputStream = socket.getInputStream();
        byte[] answer = new byte[2048];
        int a = inputStream.read(answer);
        socket.close();
        System.out.println(a);
        byte[] decryptedAnswer = decrypt(answer);
        byte[] smalerArray = cutOffArrayFromIndex(decryptedAnswer, a);
        String answerAsString = new String(smalerArray, StandardCharsets.UTF_8);
        System.out.println(answerAsString);
    }


    // Encryption and Decryption of TP-Link Smart Home Protocol
// XOR Autokey Cipher with starting key = 171
    private static byte[] decrypt (byte[] bytArr) {
        byte[] tmparr = new byte[bytArr.length];
        if (bytArr != null && bytArr.length > 0) {
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
        //i have no idea why;
        byte [] tmpArr = new byte[bArr.length + 4];
        byte[] otherArr = ByteBuffer.allocate(4).putInt(bArr.length).array();
        System.arraycopy(otherArr, 0, tmpArr, 0, otherArr.length);
        if (bArr != null && bArr.length > 0) {
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
        if (length >= 0) System.arraycopy(decryptedAnswer, 0, smallerArray, 0, length);
        return smallerArray;
    }


}
