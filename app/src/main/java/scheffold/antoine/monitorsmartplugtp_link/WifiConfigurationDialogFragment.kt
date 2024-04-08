package scheffold.antoine.monitorsmartplugtp_link

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import scheffold.antoine.monitorsmartplugtp_link.databinding.DialogWifiConfigurationBinding
import scheffold.antoine.monitorsmartplugtp_link.viewmodel.RequestViewModel

class WifiConfigurationDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.alert_dialog_title))
        val binding = DialogWifiConfigurationBinding.inflate(
            layoutInflater
        )
        builder.setView(binding.root)
        val viewModel = ViewModelProvider(this).get(
            RequestViewModel::class.java
        )
        builder.setPositiveButton(getString(R.string.ok)) { dialog: DialogInterface?, which: Int ->
            val ssid = binding.editSsid.text.toString()
            val password = binding.editPassword.text.toString()
            val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
            val ip = sharedPref.getString(getString(R.string.tp_link_ip_address), "") ?: ""
            viewModel.configureWifi(ip, ssid, password)
            dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int -> dismiss() }
        return builder.create()
    }
}
