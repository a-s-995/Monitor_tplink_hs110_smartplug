package scheffold.antoine.monitorsmartplugtp_link.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import scheffold.antoine.monitorsmartplugtp_link.FindTpLink
import scheffold.antoine.monitorsmartplugtp_link.exception.PlaceHolderException
import scheffold.antoine.monitorsmartplugtp_link.util.TpLinkSmartPlugCommandsHelper
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock

class RequestViewModel : ViewModel() {
    val networkResultLiveData = MutableLiveData<String>()
    val errorLiveData = MutableLiveData<Exception>()
    var lock = ReentrantLock()
    var foundIp: String? = null

    fun findTpLinkSmartPlugDevice(context: Context?) {
        Thread {
            val findTpLink = FindTpLink(object : FindTpLink.Callback {
                override fun setFoundDeviceIp(result: String?) {
                    networkResultLiveData.postValue(result)
                }

                override fun setError(error: Exception?) {
                    errorLiveData.postValue(error)
                }
            })
            findTpLink.findTpLinkSmartPlugDevice(context!!)
        }.start()
    }

    fun enablePlugDevice(ip: String?) {
        Thread {
            try {
                TpLinkSmartPlugCommandsHelper.startSmartPlug(ip!!)
            } catch (e: IOException) {
                errorLiveData.postValue(e)
            }
        }.start()
    }

    fun disablePlugDevice(ip: String?) {
        Thread {
            try {
                TpLinkSmartPlugCommandsHelper.stopSmartPlug(ip!!)
            } catch (e: IOException) {
                errorLiveData.postValue(e)
            }
        }.start()
    }

    fun configureWifi(ip: String, ssid: String?, password: String?) {
        Thread {
            try {
                val answer = TpLinkSmartPlugCommandsHelper.configureWifi(ip, ssid, password)
                errorLiveData.postValue(PlaceHolderException(answer))
            } catch (e: IOException) {
                errorLiveData.postValue(e)
            }
        }.start()
    }

    companion object {
        private const val TIMEOUT_MS = 400
    }
}
