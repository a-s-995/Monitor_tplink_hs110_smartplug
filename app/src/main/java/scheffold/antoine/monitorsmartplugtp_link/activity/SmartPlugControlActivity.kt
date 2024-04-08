package scheffold.antoine.monitorsmartplugtp_link.activity

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import scheffold.antoine.monitorsmartplugtp_link.R
import scheffold.antoine.monitorsmartplugtp_link.WifiConfigurationDialogFragment
import scheffold.antoine.monitorsmartplugtp_link.databinding.SmartPlugControlActivityBinding
import scheffold.antoine.monitorsmartplugtp_link.service.MonitorSmartPlugEnergyConsumptionService
import scheffold.antoine.monitorsmartplugtp_link.viewmodel.RequestViewModel

class SmartPlugControlActivity : AppCompatActivity() {
    private lateinit var binding: SmartPlugControlActivityBinding
    private var notificationPermLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SmartPlugControlActivityBinding.inflate(
            layoutInflater
        )
        val view: View = binding.root
        setContentView(view)
        val viewModel = ViewModelProvider(this).get(
            RequestViewModel::class.java
        )
        binding.startForegroundServiceButton.setOnClickListener { v: View? ->
            val thresholdEditText = findViewById<EditText>(R.id.threshold)
            // TODO: 23.05.23 cannot be empty. Evaluate
            val thresholdInWatt = thresholdEditText.text.toString().toInt()
            MonitorSmartPlugEnergyConsumptionService.startActionFoo(
                applicationContext,
                thresholdInWatt,
                viewModel.networkResultLiveData.value
            )
        }
        binding.stopForegroundServiceButton.setOnClickListener { v: View? ->
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val runningAppProcesses = am.runningAppProcesses
            for (next in runningAppProcesses) {
                val processName = packageName
                if (next.processName == processName) {
                    Process.killProcess(next.pid)
                    break
                }
            }
        }
        binding.enablePlugDevice.setOnClickListener { v: View? ->
            val sharedPref = getPreferences(MODE_PRIVATE)
            val ip = sharedPref.getString(getString(R.string.tp_link_ip_address), "")
            viewModel.enablePlugDevice(ip)
        }
        binding.disablePlugDevice.setOnClickListener { v: View? ->
            val sharedPref = getPreferences(MODE_PRIVATE)
            val ip = sharedPref.getString(getString(R.string.tp_link_ip_address), "")
            viewModel.disablePlugDevice(ip)
        }
        viewModel.networkResultLiveData.observe(this) { result: String? ->
            result?.let { handleFoundIpAddress(it) }
        }
        viewModel.errorLiveData.observe(this) { error: Exception? ->
            Toast.makeText(
                this,
                error?.message,
                Toast.LENGTH_LONG
            ).show()
        }
        viewModel.findTpLinkSmartPlugDevice(this)
        notificationPermLauncher = registerForActivityResult(
            RequestPermission()
        ) { granted: Boolean? -> }
        requestNotificationsPermission()
        // Check if the device is running Android Marshmallow (API 23) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestBatteryOptimizations()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.configure_wifi) {
            val dialogFragment = WifiConfigurationDialogFragment()
            dialogFragment.show(supportFragmentManager, FRAGMENT_WIFI_CONFIGURATION_DIALOG)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(
            applicationContext, permission
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        notificationPermLauncher!!.launch(permission)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestBatteryOptimizations() {
        val intent = Intent()
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.setData(Uri.parse("package:$packageName"))
        startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
            Log.d("YourActivity", "onActivityResult: " + data?.data)
        }
    }

    private fun handleFoundIpAddress(result: String) {
        val sharedPref = getPreferences(MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(getString(R.string.tp_link_ip_address), result)
        editor.apply()
        binding.indeterminateBar.visibility = View.GONE
    }

    companion object {
        private const val FRAGMENT_WIFI_CONFIGURATION_DIALOG = "FRAGMENT_WIFI_CONFIGURATION_DIALOG"
        private const val REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1001
    }
}