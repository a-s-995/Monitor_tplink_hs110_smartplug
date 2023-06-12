package scheffold.antoine.monitorsmartplugtp_link;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import scheffold.antoine.monitorsmartplugtp_link.databinding.DialogWifiConfigurationBinding;
import scheffold.antoine.monitorsmartplugtp_link.viewmodel.RequestViewModel;

public class WifiConfigurationDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.alert_dialog_title));
        DialogWifiConfigurationBinding binding = DialogWifiConfigurationBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        RequestViewModel viewModel= new ViewModelProvider(this).get(RequestViewModel.class);

        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            String ssid = binding.editSsid.getText().toString();
            String password = binding.editPassword.getText().toString();
            SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
            String ip = sharedPref.getString(getString(R.string.tp_link_ip_address), "");
            viewModel.configureWifi(ip, ssid, password);
            dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dismiss());
        return builder.create();
    }
}
