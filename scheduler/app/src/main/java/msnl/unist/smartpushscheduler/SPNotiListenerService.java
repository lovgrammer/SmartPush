package msnl.unist.smartpushscheduler;

import android.app.Notification;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

public class SPNotiListenerService extends NotificationListenerService {
    
    Queue<StatusBarNotification> notiQueue = new LinkedList<StatusBarNotification>();

    class PackageWrapper {
	public String packageName;
	public int score;
	public PackageWrapper(String packageName, int score) {
	    this.packageName = packageName;
	    this.score = score;
	}
    }
    
    ArrayList<PackageWrapper> schedulablePackages = new ArrayList<PackageWrapper>();
    
    @Override
    public void onCreate() {
	super.onCreate();
	Log.i("SPNotiListenerService", "onCreate()");
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
    
    int count = 0;
    String tmpPackageName;
    
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
	if (count > 0) {
	    return;
	}
	tmpPackageName = sbn.getPackageName();
	Log.i("NotificationListener", "onNotificationRemoved() - " + sbn.toString());
	if (System.currentTimeMillis() - sbn.getPostTime() > 60000 * 60 * 6) {
	    createOrUpdateSchedulablePackage(sbn.getPackageName(), -1);		    
	} else {
	    createOrUpdateSchedulablePackage(sbn.getPackageName(), -5);
	}
	StatusBarNotification[] activeNotis = SPNotiListenerService.this.getActiveNotifications();
	for (StatusBarNotification noti :activeNotis) {
	    createOrUpdateSchedulablePackage(noti.getPackageName(), 5);				
	}
	count++;
	Thread t = new Thread() {
		public void run() {
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException ignore) { }
		    displaySchedulablePackage();
		    if (count > 1) {
			createOrUpdateSchedulablePackage(tmpPackageName, -5);
		    }
		    count = 0;
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
}

