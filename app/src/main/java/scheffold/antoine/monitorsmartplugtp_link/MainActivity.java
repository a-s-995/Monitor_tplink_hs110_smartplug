package scheffold.antoine.monitorsmartplugtp_link;

import android.app.ActivityManager;
import android.os.Bundle;
import android.os.Process;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int INTERVA_MINUTE = 60 * 1000;//todo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Button startServiceButton = findViewById(R.id.start_foreground_service_button);
        startServiceButton.setOnClickListener(v -> {
            EditText ipEditText = findViewById(R.id.ip_adress);
            String ip = ipEditText.getText().toString();
            EditText thresholdEditText = findViewById(R.id.threshold);
            int thresholdInWatt = Integer.parseInt(thresholdEditText.getText().toString());
            MyIntentService.startActionFoo(getApplicationContext(),  ip, thresholdInWatt );
            /*
            Intent intent = new Intent(getApplicationContext(), QuerySmartPlugService.class);

            intent.setAction(QuerySmartPlugService.ACTION_START_FOREGROUND_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //   startForegroundService(intent);
                Context context = getApplicationContext();

                System.out.println("HHHHHHHHHHIERR BIN ICH !!");
                int requestId = 0;
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                PendingIntent pendingIntent =
                        PendingIntent.getService(context, requestId, intent, PendingIntent.FLAG_NO_CREATE);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                if (pendingIntent != null && alarmManager != null) {
                    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
                             INTERVA_MINUTE, alarmIntent);
                    System.out.println("HHHHHHHHHHIERR BIN ICH !!");

                }
            }
            else {
                startService(intent);
            }

           */
        });

        Button stopServiceButton = findViewById(R.id.stop_foreground_service_button);
        stopServiceButton.setOnClickListener(v -> {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

            for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
                String processName = getPackageName() + ":service";
                if (next.processName.equals(processName)) {
                    Process.killProcess(next.pid);
                    break;
                }
            }
          //  MyIntentService.startActionBazz(getApplicationContext());
        });
    }
}