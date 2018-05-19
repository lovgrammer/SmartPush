package msnl.unist.smartpushscheduler;

import android.app.Notification;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

public class SPNotiListenerService extends NotificationListenerService {
    
    class PackageWrapper {
	public String packageName;
	public int score;
	public PackageWrapper(String packageName, int score) {
	    this.packageName = packageName;
	    this.score = score;
	}
    }
    
    Queue<StatusBarNotification> notiQueue = new LinkedList<StatusBarNotification>();
    ArrayList<PackageWrapper> schedulablePackages = new ArrayList<PackageWrapper>();
    
    private boolean isRegistered = false;
    
    @Override
    public void onCreate() {
	super.onCreate();
	Log.i("SPNotiListenerService", "onCreate()");
	
	registerScreenEvent();
	if (!isRegistered) {
	    isRegistered = true;
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
	if (isRegistered) {
	    unregisterReceiver(mReceiver);
	    isRegistered = false;
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
	displaySchedulablePackage();
	
	for (PackageWrapper p : schedulablePackages) {
	    if (sbn.getPackageName().equals(p.packageName)) {
		if (p.score > 7) {
		    notiQueue.offer(sbn);
		    SPNotiListenerService.this.cancelNotification(sbn.getKey());		    
		}
		return;
	    }
	}
    }
    
    public static int count = 0;
    String tmpPackageName;
    ArrayList<String> clearAllPackageNames = new ArrayList<String>();
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
	clearAllPackageNames.add(sbn.getPackageName());
	Log.i("NotificationListener", "onNotificationRemoved() - " + sbn.toString());
	if (System.currentTimeMillis() - sbn.getPostTime() > 60000 * 60 * 6) {
	    createOrUpdateSchedulablePackage(sbn.getPackageName(), -1);		    
	} else {
	    createOrUpdateSchedulablePackage(sbn.getPackageName(), -5);
	}
	StatusBarNotification[] activeNotis = SPNotiListenerService.this.getActiveNotifications();
	for (StatusBarNotification noti :activeNotis) {
	    createOrUpdateSchedulablePackage(noti.getPackageName(), 3);				
	}
	Thread t = new Thread() {
		public void run() {
		    try {
			Thread.sleep(500);
		    } catch (InterruptedException ignore) { }
		    
		    if (clearAllPackageNames.size() > 1) {
			for (String packageName : clearAllPackageNames) {
			    createOrUpdateSchedulablePackage(packageName, 8);
			}
			displaySchedulablePackage();
		    }
		    clearAllPackageNames.clear();
		}
	    };
	t.start();
	displaySchedulablePackage();
    }

    public void createOrUpdateSchedulablePackage(String packageName, int score) {
	for (PackageWrapper p : schedulablePackages) {
	    if (p.packageName.equals(packageName)) {
		p.score += score;
		if (p.score > 10) p.score = 10;
		else if (p.score < -10) p.score = -10;
		return;
	    }
	}
	schedulablePackages.add(new PackageWrapper(packageName, score));
    }


    public void displaySchedulablePackage() {
	Log.i("SPNotiListenerService", "** SchedulablePackage ***");	    
	for (PackageWrapper p : schedulablePackages) {
	    Log.i("SPNotiListenerService", "[" + p.packageName + ", " + p.score + "]");	    
	}	
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
		    Log.i("SPNotiListenerService", "screen off");
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
		    Log.i("SPNotiListenerService", "screen on");
		    StatusBarNotification[] activeNotis = SPNotiListenerService.this.getActiveNotifications();
		    for (StatusBarNotification noti :activeNotis) {
			createOrUpdateSchedulablePackage(noti.getPackageName(), 1);				
		    }
		    displaySchedulablePackage();
		}
	    }
	};

    private void registerScreenEvent() {
	IntentFilter filterScreenON = new IntentFilter(Intent.ACTION_SCREEN_ON);
	registerReceiver(mReceiver, filterScreenON);

	IntentFilter filterScreenOFF = new IntentFilter(Intent.ACTION_SCREEN_OFF);
	registerReceiver(mReceiver, filterScreenOFF);
    }
}

