package ga.nikhilkumar.whatsappsender;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class WASender_Service extends AccessibilityService {
    public WASender_Service() {
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
            if(event.getClassName().toString().equals("com.whatsapp.Conversation")){
                AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                List<AccessibilityNodeInfo> sendNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp.w4b:id/send");
                for (AccessibilityNodeInfo mNode : sendNode)
                mNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sp.edit().putBoolean("sent", true).commit();
                Log.e("info","sending");
            }
        }
    }

        @Override
    public void onInterrupt() {

    }
}
