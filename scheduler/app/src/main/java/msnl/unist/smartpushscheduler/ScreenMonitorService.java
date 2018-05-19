package msnl.unist.smartpushscheduler;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScreenMonitorService extends android.accessibilityservice.AccessibilityService {
    
    public static final String fgInfoAction = "unist.msnl.broadcast.fginfo";
    public static String currentFgApp = "unist.msnl.smartpushscheduler";

    public void onServiceConnected() {
        super.onServiceConnected();
	Log.i("ScreenMOnitorService", "onServiceConnected");
        // Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
								event.getPackageName().toString(),
								event.getClassName().toString()
								);

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity) {
		    Log.i("ScreenMOnitorService", "CurrentActivity : " + componentName.getPackageName());
                    currentFgApp = componentName.getPackageName();
		    broadcastFgAppInfo(currentFgApp);
		}
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {}

    public void broadcastFgAppInfo(String packageName) {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(fgInfoAction);
	broadcastIntent.putExtra("packageName", packageName);
	sendBroadcast(broadcastIntent);
    }
}
