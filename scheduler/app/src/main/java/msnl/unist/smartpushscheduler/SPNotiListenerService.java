package msnl.unist.smartpushscheduler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

public class SPNotiListenerService extends NotificationListenerService implements SensorEventListener {
    
    class PackageWrapper {
	public String packageName;
	public int score;
	public PackageWrapper(String packageName, int score) {
	    this.packageName = packageName;
	    this.score = score;
	}
    }
    
    private Queue<StatusBarNotification> notiQueue = new LinkedList<StatusBarNotification>();
    private ArrayList<PackageWrapper> schedulablePackages = new ArrayList<PackageWrapper>();
    private SchedulingManager mSchedulingManager;
    
    private boolean isRegistered = false;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyro;

    public void initSensors() {
	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);	
    }

    public Queue<StatusBarNotification> getNotiQueue() {
	return notiQueue;
    }
    
    @Override
    public void onCreate() {
	super.onCreate();
	Log.i("SPNotiListenerService", "onCreate()");
	mSchedulingManager = new SchedulingManager(SPNotiListenerService.this);
	registerScreenEvent();
	registerFgAppInfoReceiver();
	if (!isRegistered) {
	    isRegistered = true;
	}

	initSensors();
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
	    unregisterReceiver(mFgAppInfoReceiver);
	    isRegistered = false;
	}
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
	Log.i("SPScheduleService", "PackageName : " + sbn.getPackageName());
	Log.i("SPScheduleService", "PostTime : " + sbn.getPostTime());
	
	boolean schedulingOn = isSchedulingOn();

	Notification notification = sbn.getNotification();
	Bundle extras = notification.extras;
	String title = extras.getString(Notification.EXTRA_TITLE);
	int smallIconRes = extras.getInt(Notification.EXTRA_SMALL_ICON);
	Bitmap largeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON));
	CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
	CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
	int uid = getUidFromPackagename(sbn.getPackageName());
	
	Log.i("SPScheduleService", "Title : " + title);
	Log.i("SPScheduleService", "Text : " + text);
	Log.i("SPScheduleService", "Sub Text : " + subText);
	
	displaySchedulablePackage();
	// String category = sbn.getNotification().category;
	// if (category != null && category.equals("scheduled")) {
	String s = sbn.getNotification().extras.getString("scheduled");
	if (s != null && s.equals("1")) {
	    // scheduled notification finally posted
	    StatusDataCollector.saveNotiInfo(SPNotiListenerService.this, System.currentTimeMillis(), sbn.getPackageName(), uid, 0, schedulingOn ? 1 : 0);
	    return;
	}
	
	if (schedulingOn) {
	    for (PackageWrapper p : schedulablePackages) {
		if (sbn.getPackageName().equals(p.packageName)) {
		    if (p.score > 7) {
			// scheduled if score > 7
			notiQueue.offer(sbn);
			SPNotiListenerService.this.cancelNotification(sbn.getKey());		    
		    }
		    return;
		}
	    }
	}
	
	// immediate push reach here
	StatusDataCollector.saveNotiInfo(SPNotiListenerService.this, System.currentTimeMillis(), sbn.getPackageName(), uid, 1, schedulingOn ? 1 : 0);
	mSchedulingManager.onNotificationPosted(sbn);
    }
    
    public static int count = 0;
    String tmpPackageName;
    ArrayList<String> clearAllPackageNames = new ArrayList<String>();
    
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
	if (isInNotiQueue(sbn.getKey())) return; // scheduled notification

	boolean schedulingOn = isSchedulingOn();
	    
	// decision occurs for both scheduled/unscheduled notifications
	StatusDataCollector.saveSeenDecisionTime(SPNotiListenerService.this, sbn.getPostTime(), sbn.getPackageName(), getUidFromPackagename(sbn.getPackageName()), 1, schedulingOn ? 1 : 0);
	// String category = sbn.getNotification().category;
	// if (category != null && category.equals("scheduled")) return;
	String s = sbn.getNotification().extras.getString("scheduled");
	if (s != null && s.equals("1")) return;

	mSchedulingManager.onNotificationRemoved(sbn);
	
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
		    mHandler.sendEmptyMessage(2);
		    // mSchedulingManager.onScreenOff();
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
		    Log.i("SPNotiListenerService", "screen on");
		    StatusBarNotification[] activeNotis = SPNotiListenerService.this.getActiveNotifications();
		    // assume all the notification is noticed by user whenever screen on occurs
		    boolean schedulingOn = isSchedulingOn();
		    for (StatusBarNotification noti :activeNotis) {
			StatusDataCollector.saveSeenDecisionTime(SPNotiListenerService.this, noti.getPostTime(), noti.getPackageName(), getUidFromPackagename(noti.getPackageName()), 0, schedulingOn ? 1 : 0);
			createOrUpdateSchedulablePackage(noti.getPackageName(), 1);				
		    }
		    displaySchedulablePackage();
		    mHandler.sendEmptyMessage(1);
		    // mSchedulingManager.onScreenOn();
		}
	    }
	};

    private void registerScreenEvent() {
	IntentFilter filterScreenON = new IntentFilter(Intent.ACTION_SCREEN_ON);
	registerReceiver(mReceiver, filterScreenON);

	IntentFilter filterScreenOFF = new IntentFilter(Intent.ACTION_SCREEN_OFF);
	registerReceiver(mReceiver, filterScreenOFF);
    }

    public boolean isInNotiQueue(String notiKey) {
	for (StatusBarNotification sbn : notiQueue) {
	    if (sbn.getKey().equals(notiKey)) return true;
	}
	return false;
    }
    
    String prevPackageName = "";

    private BroadcastReceiver mFgAppInfoReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ScreenMonitorService.fgInfoAction)) {
		    String packageName = intent.getStringExtra("packageName");
		    Message message = Message.obtain(mHandler, 0, packageName);
		    mHandler.sendMessage(message);
		}
	    }
	};

    public void registerFgAppInfoReceiver() {
	IntentFilter filter = new IntentFilter();
	filter.addAction(ScreenMonitorService.fgInfoAction);
	registerReceiver(mFgAppInfoReceiver, filter);
    }

    Handler mHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		switch(msg.what) {
		case 0:
		    String packageName = ((String)msg.obj);
		    mSchedulingManager.onFgAppChanged(prevPackageName, packageName);
		    prevPackageName = packageName;		    
		    break;
		case 1:
		    mSchedulingManager.onScreenOn();		    
		    break;
		case 2:
		    mSchedulingManager.onScreenOff();		    
		    break;
		}
	    }
	};

    float prevAccAverage = 0;
    float prevGyroAverage = 0;
    boolean accFlag = false;
    boolean gyroFlag = false;
	
    @Override
    public void onSensorChanged(SensorEvent event) {
	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	    float accX = event.values[0];
	    float accY = event.values[1];
	    float accZ = event.values[2];
	    
	    float accAverage = (float)Math.sqrt((accX * accX + accY * accY + accZ * accZ));
	    accFlag = (accAverage - prevAccAverage > 5);
	    prevAccAverage = accAverage;
	    // Log.i("SPScheduleService", "accAverage :" + accAverage);
	} else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
	    float gyroX = event.values[0];
	    float gyroY = event.values[1];
	    float gyroZ = event.values[2];
	    float gyroAverage = (float)Math.sqrt((gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ));
	    gyroFlag = (gyroAverage - prevGyroAverage > 3);
	    prevGyroAverage = gyroAverage;
	    // Log.i("SPScheduleService", "gyroAverage :" + gyroAverage);
	}
	if (accFlag || gyroFlag) {
	    String action = "moving";
	    mSchedulingManager.onSensorEventTriggered(action);
	}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
	
    }

    public int getUidFromPackagename(String packageName) {
	int uid = 0;
	try {
	    uid = this.getPackageManager().getApplicationInfo(packageName, 0).uid;
	} catch (PackageManager.NameNotFoundException e) {
	    e.printStackTrace();
	}
	return uid;
    }

    public boolean isSchedulingOn() {
	return (System.currentTimeMillis() / (3600000 * 5)) % 2 == 0;
    }
}

