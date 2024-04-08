package scheffold.antoine.monitorsmartplugtp_link.util

import scheffold.antoine.monitorsmartplugtp_link.Commands
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Arrays

object TpLinkSmartPlugCommandsHelper {
    const val TP_LINK_COMMUNICATION_PORT = 9999
    @JvmStatic
    fun parseConsumingWatt(toParse: String): Int {
        val seperatedByCommas =
            toParse.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (str in seperatedByCommas) {
            if (str.contains("power_mw")) {
                val powerAndWatt =
                    str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                println(Arrays.toString(powerAndWatt))
                return powerAndWatt[1].toInt()
            }
        }
        throw IllegalStateException("No Watt consumption in String")
    }

    @Throws(IOException::class)
    fun getSystemInfo(ip: String): String {
        val cmd = Commands.COMMANDS["info"]
        return queryCommand(ip, cmd)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun querySmartPlugMeter(ip: String): String {
        val cmd = Commands.COMMANDS["energy"]
        return queryCommand(ip, cmd)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun stopSmartPlug(ip: String): String {
        val cmd = Commands.COMMANDS["off"]
        return queryCommand(ip, cmd)
    }

    @Throws(IOException::class)
    fun startSmartPlug(ip: String): String {
        val cmd = Commands.COMMANDS["on"]
        return queryCommand(ip, cmd)
    }

    @Throws(IOException::class)
    fun configureWifi(ip: String, ssid: String?, password: String?): String {
        val cmd = Commands.COMMANDS["configwifi"]
        val formattedCmd = String.format(cmd!!, ssid, password)
        return queryCommand(ip, formattedCmd)
    }

    // TODO: 23.05.23 configure WIFI of smartplug
    @Throws(IOException::class)
    private fun queryCommand(ip: String, cmd: String?): String {
        val socket = Socket(ip, TP_LINK_COMMUNICATION_PORT)
        val encryptedCmd = encrypt(cmd!!.toByteArray())
        val socketOutputStream = socket.getOutputStream()
        socketOutputStream.write(encryptedCmd)
        socketOutputStream.flush()
        val inputStream = socket.getInputStream()
        val answer = ByteArray(2048)
        val a = inputStream.read(answer)
        socket.close()
        val decryptedAnswer = decrypt(answer)
        val smallerArray = cutOffArrayFromIndex(decryptedAnswer, a)
        return String(smallerArray, StandardCharsets.UTF_8)
    }

    // Encryption and Decryption of TP-Link Smart Home Protocol
    // XOR Autokey Cipher with starting key = 171
    private fun decrypt(bytArr: ByteArray): ByteArray {
        val tmparr = ByteArray(bytArr.size)
        if (bytArr.size > 0) {
            var key = 171
            for (i in 4 until bytArr.size) {
                val b = (key xor bytArr[i].toInt()).toByte()
                key = bytArr[i].toInt()
                tmparr[i] = b
            }
        }
        return tmparr
    }

    private fun encrypt(bArr: ByteArray): ByteArray {
        val tmpArr = ByteArray(bArr.size + 4)
        val otherArr = ByteBuffer.allocate(4).putInt(bArr.size).array()
        System.arraycopy(otherArr, 0, tmpArr, 0, otherArr.size)
        if (bArr.size > 0) {
            var key = 171
            for (i in bArr.indices) {
                val b = (key xor bArr[i].toInt()).toByte()
                key = b.toInt()
                tmpArr[i + 4] = b
            }
        }
        return tmpArr
    }

    private fun cutOffArrayFromIndex(decryptedAnswer: ByteArray, length: Int): ByteArray {
        val smallerArray = ByteArray(length)
        System.arraycopy(decryptedAnswer, 0, smallerArray, 0, length)
        return smallerArray
    }
}
