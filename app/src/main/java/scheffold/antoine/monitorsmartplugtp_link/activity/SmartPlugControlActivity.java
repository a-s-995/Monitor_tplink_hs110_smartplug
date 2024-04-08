package scheffold.antoine.monitorsmartplugtp_link.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    private ActivityResultLauncher<String> notificationPermLauncher;
    private static final int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1001;

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
        notificationPermLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                granted -> {});
        requestNotificationsPermission();
        // Check if the device is running Android Marshmallow (API 23) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestBatteryOptimizations();
        }
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

    private void requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;

        String permission = Manifest.permission.POST_NOTIFICATIONS;
        boolean granted = ContextCompat.checkSelfPermission(
                getApplicationContext(), permission
        ) == PackageManager.PERMISSION_GRANTED;

        if (granted) return;

        notificationPermLauncher.launch(permission);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestBatteryOptimizations() {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
            Log.d("YourActivity", "onActivityResult: " + (data != null ? data.getData() : null));
        }
    }

    private void handleFoundIpAddress(String result) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.tp_link_ip_address), result);
        editor.apply();
        binding.indeterminateBar.setVisibility(View.GONE);
    }
}