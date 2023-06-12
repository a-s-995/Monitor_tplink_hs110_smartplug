package scheffold.antoine.monitorsmartplugtp_link.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import scheffold.antoine.monitorsmartplugtp_link.FindTpLink;
import scheffold.antoine.monitorsmartplugtp_link.exception.PlaceHolderException;
import scheffold.antoine.monitorsmartplugtp_link.util.TpLinkSmartPlugCommandsHelper;

public class RequestViewModel extends ViewModel {

    private static final int TIMEOUT_MS = 400;
    private final MutableLiveData<String> networkResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exception> errorLiveData = new MutableLiveData<>();

    ReentrantLock lock = new ReentrantLock();
    String foundIp;


    public LiveData<String> getNetworkResultLiveData() {
        return networkResultLiveData;
    }

    public LiveData<Exception> getErrorLiveData() {
        return errorLiveData;
    }

    public void findTpLinkSmartPlugDevice(Context context) {
        new Thread(() -> {


        FindTpLink findTpLink = new FindTpLink(new FindTpLink.Callback() {
            @Override
            public void setFoundDeviceIp(String result) {
                networkResultLiveData.postValue(result);
            }

            @Override
            public void setError(Exception error) {
                errorLiveData.postValue(error);
            }
        });
        findTpLink.findTpLinkSmartPlugDevice(context);
        }).start();
    }

    public void enablePlugDevice(String ip) {
        new Thread(() -> {
        try {
            TpLinkSmartPlugCommandsHelper.startSmartPlug(ip);
        } catch (IOException e) {
            errorLiveData.postValue(e);
        }
        }).start();
    }

    public void disablePlugDevice(String ip) {
        new Thread(() -> {
        try {
            TpLinkSmartPlugCommandsHelper.stopSmartPlug(ip);
        } catch (IOException e) {
            errorLiveData.postValue(e);
        }
        }).start();
    }

    public void configureWifi(String ip, String ssid, String password) {
        new Thread(() -> {
            try {
                String answer = TpLinkSmartPlugCommandsHelper.configureWifi(ip, ssid, password);
                errorLiveData.postValue(new PlaceHolderException(answer));
            } catch (IOException e) {
                errorLiveData.postValue(e);
            }
        }).start();
    }

}
