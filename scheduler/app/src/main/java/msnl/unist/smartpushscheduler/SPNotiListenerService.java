package msnl.unist.smartpushscheduler;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

public class SPNotiListenerService extends NotificationListenerService {
    
    private Messenger mService = null;
    private boolean mBound;

    static final int NOTI_REMOVED = 2;
    public static final String ACTION_CUSTOM = "msnl.unist.smartpushscheduler.ACTION_CUSTOM";

    private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
		mService = new Messenger(service);
		mBound = true;
	    }

	    public void onServiceDisconnected(ComponentName className) {
		mService = null;
		mBound = false;
	    }
	};
    
    @Override
    public void onCreate() {
	super.onCreate();
	Log.i("SPNotiListenerService", "onCreate()");
	if (!mBound) {
	    doBindService();
	}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.i("SPNotiListenerService", "onStartCommand()");
	return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	Log.i("SPNotiListenerService", "onDestroy()");
	if (mBound) {
	    doUnbindService();
	}
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
	Log.i("SPScheduleService", "PackageName : " + sbn.getPackageName());
	Log.i("SPScheduleService", "PostTime : " + sbn.getPostTime());

	Notification notification = sbn.getNotification();
	Bundle extras = notification.extras;
	String title = extras.getString(Notification.EXTRA_TITLE);
	int smallIconRes = extras.getInt(Notification.EXTRA_SMALL_ICON);
	Bitmap largeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON));
	CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
	CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

	Log.i("SPScheduleService", "Title : " + title);
	Log.i("SPScheduleService", "Text : " + text);
	Log.i("SPScheduleService", "Sub Text : " + subText);
	
	// Calendar cal = Calendar.getInstance();
	// SPDay d =  dayList.get(cal.get(Calendar.DAY_OF_WEEK) - 1);
	// SPHour h = d.getHourList().get(cal.get(Calendar.HOUR_OF_DAY));
	// h.postedCount += 1;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
	Log.i("NotificationListener", "onNotificationRemoved() - " + sbn.toString());

	// Calendar cal = Calendar.getInstance();
	// SPDay d =  dayList.get(cal.get(Calendar.DAY_OF_WEEK) - 1);
	// SPHour h = d.getHourList().get(cal.get(Calendar.HOUR_OF_DAY));

	// h.decisionCount += 1;

	updateNotiRemoved("", "");
    }

    public void doBindService() {
	try {
	    Intent intentForSPService = new Intent();
	    Log.i("SPNotiListenerService", "init intent.componentName");
	    intentForSPService.setComponent(new ComponentName("msnl.unist.smartpushscheduler", "msnl.unist.smartpushscheduler.SPScheduleService"));
	    intentForSPService.setAction(ACTION_CUSTOM);
	    Log.i("SPNotiListenerService", "Before bindService");
	    if (bindService(intentForSPService, mConnection, 0)) {
		Log.i("SPNotiListenerService", "Binding to Scheduler returned true");
	    } else {
		Log.i("SPNotiListenerService", "Binding to Scheduler returned false");
	    }
	} catch (SecurityException e)  {
	    Log.e("SPNotiListenerService", "cannot bind to Scheduler check permission in Manifest : " + e.getMessage());
	}
    }

    public void doUnbindService() {
	if (mBound) {
	    unbindService(mConnection);
	    mBound = false;
	}
    }

    public void updateNotiRemoved(String title, String body) {
	Log.i("SPNotiListenerService", "updateNotiRemoved!");
	if (!mBound)  return;
	try {
	    JSONObject json = new JSONObject();
	    json.put("title", title);
	    json.put("body", body);
	
	    Message msg = Message.obtain(null, NOTI_REMOVED, 0, 0);
	    Bundle data = new Bundle();
	    data.putString("data", json.toString());
	    msg.setData(data);
	    
	    mService.send(msg);
	} catch (RemoteException e) {
	    e.printStackTrace();
	} catch (JSONException e) {
	    e.printStackTrace();
	}
    }
}

