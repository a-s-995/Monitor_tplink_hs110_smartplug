package scheffold.antoine.monitorsmartplugtp_link.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import scheffold.antoine.monitorsmartplugtp_link.R;
import scheffold.antoine.monitorsmartplugtp_link.WifiConfigurationDialogFragment;
import scheffold.antoine.monitorsmartplugtp_link.databinding.SmartPlugControlActivityBinding;
import scheffold.antoine.monitorsmartplugtp_link.service.MonitorSmartPlugEnergyConsumptionService;
import scheffold.antoine.monitorsmartplugtp_link.viewmodel.RequestViewModel;

public class SmartPlugControlActivity extends AppCompatActivity {

    private static final String FRAGMENT_WIFI_CONFIGURATION_DIALOG = "FRAGMENT_WIFI_CONFIGURATION_DIALOG";
    private SmartPlugControlActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SmartPlugControlActivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        RequestViewModel viewModel= new ViewModelProvider(this).get(RequestViewModel.class);

        binding.startForegroundServiceButton.setOnClickListener(v -> {
            EditText thresholdEditText = findViewById(R.id.threshold);
            // TODO: 23.05.23 cannot be empty. Evaluate
            int thresholdInWatt = Integer.parseInt(thresholdEditText.getText().toString());
            MonitorSmartPlugEnergyConsumptionService.startActionFoo(getApplicationContext(), thresholdInWatt);
        });

        binding.stopForegroundServiceButton.setOnClickListener(v -> {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

            for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
                String processName = getPackageName();
                if (next.processName.equals(processName)) {
                    Process.killProcess(next.pid);
                    break;
                }
            }
        });
        binding.enablePlugDevice.setOnClickListener(v -> {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String ip = sharedPref.getString(getString(R.string.tp_link_ip_address), "");
            viewModel.enablePlugDevice(ip);
//            Anfrage gemacht -> schauen ob IP passt -> ip Suchen -> gefunden oder nicht gefunden
        });
        binding.disablePlugDevice.setOnClickListener(v -> {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String ip = sharedPref.getString(getString(R.string.tp_link_ip_address), "");
            viewModel.disablePlugDevice(ip);
//            Anfrage gemacht -> schauen ob IP passt -> ip Suchen -> gefunden oder nicht gefunden
        });
        viewModel.getNetworkResultLiveData().observe(this, this::handleFoundIpAddress);
        viewModel.getErrorLiveData().observe(this, error ->
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show());
        viewModel.findTpLinkSmartPlugDevice(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.configure_wifi) {
            WifiConfigurationDialogFragment dialogFragment = new WifiConfigurationDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), FRAGMENT_WIFI_CONFIGURATION_DIALOG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleFoundIpAddress(String result) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.tp_link_ip_address), result);
        editor.apply();
        binding.indeterminateBar.setVisibility(View.GONE);
    }
}