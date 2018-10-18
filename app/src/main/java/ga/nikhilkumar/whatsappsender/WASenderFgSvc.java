package ga.nikhilkumar.whatsappsender;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WASenderFgSvc extends Service {

    Integer progress = 0;
    List<String> recipientList = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Boolean start = intent.getBooleanExtra("start", true);
        if (start) {
            progress = 0;
            recipientList.clear();
            NotificationManager nm = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder notificationBuilder = new Notification.Builder(this);
            notificationBuilder.setContentText("Sending Messages");
            notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
            Notification not = notificationBuilder.build();
            startForeground(12, not);
            recipientList.add("918002572171");
            send();
        } else {
            send();
        }
        return START_STICKY;
    }

    private void send() {
        if (progress == recipientList.size()) {
            return;
        }
        Toast.makeText(this, "" + progress, Toast.LENGTH_SHORT).show();
        String recipient = recipientList.get(progress);
        progress++;
        Intent sendIntent = new Intent();
        sendIntent.setPackage("com.whatsapp.w4b");
        sendIntent.setAction("android.intent.action.SEND");
        sendIntent.setType("text/plain");
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Sample Message");
        sendIntent.putExtra("jid", recipient + "@s.whatsapp.net");
        startActivity(sendIntent);
    }


}
