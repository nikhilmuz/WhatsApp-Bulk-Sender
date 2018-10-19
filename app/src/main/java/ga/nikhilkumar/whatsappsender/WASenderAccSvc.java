package ga.nikhilkumar.whatsappsender;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;

import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK;

public class WASenderAccSvc extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("running", false)) {
            return;
        }
        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
            String actname = event.getClassName().toString();
            if (actname.equals("com.whatsapp.Conversation")) {
                getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.whatsapp.w4b:id/send").get(0).performAction(ACTION_CLICK);
                performGlobalAction(GLOBAL_ACTION_BACK);
            } else if (actname.equals("com.whatsapp.HomeActivity")) {
                sendNext();
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    private void sendNext() {
        Intent intent = new Intent(this, WASenderFgSvc.class);
        intent.putExtra("start", false);
        startService(intent);
    }
}
