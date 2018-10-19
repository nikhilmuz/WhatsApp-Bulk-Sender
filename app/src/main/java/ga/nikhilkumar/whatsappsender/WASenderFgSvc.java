package ga.nikhilkumar.whatsappsender;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WASenderFgSvc extends Service {

    private static final int NOTIFICATION_ID = 12;
    Notification.Builder notificationBuilder;
    SharedPreferences sp;
    Integer progress = 0;
    List<String[]> recipientList = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        Boolean start = intent.getBooleanExtra("start", true);
        if (start) {
            progress = 0;
            recipientList.clear();
            Uri uri = intent.getParcelableExtra("uri");
            try {
                InputStream file = getContentResolver().openInputStream(uri);
                CSVReader csvReader = new CSVReader(new InputStreamReader(file));
                recipientList = csvReader.readAll();
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "File not Found", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Not a CSV file", Toast.LENGTH_SHORT).show();
            }
            sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            notificationBuilder.setContentText("Sending Messages");
            Notification not = notificationBuilder.build();
            startForeground(NOTIFICATION_ID, not);
            send();
        } else {
            send();
        }
        return START_STICKY;
    }

    @SuppressLint("ApplySharedPref")
    private void send() {
        if (progress == recipientList.size()) {
            Toast.makeText(this, "Task Complete", Toast.LENGTH_SHORT).show();
            sp.edit().putBoolean("running", false).commit();
            notificationBuilder.setContentText("Sent");
            Notification not = notificationBuilder.build();
            startForeground(NOTIFICATION_ID, not);
            return;
        }
        String recipient = recipientList.get(progress)[0];
        String message = recipientList.get(progress)[1];
        Intent sendIntent = new Intent();
        sendIntent.setPackage("com.whatsapp.w4b");
        sendIntent.setAction("android.intent.action.SEND");
        sendIntent.setType("text/plain");
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.putExtra("jid", recipient + "@s.whatsapp.net");
        progress++;
        startActivity(sendIntent);
    }


}
