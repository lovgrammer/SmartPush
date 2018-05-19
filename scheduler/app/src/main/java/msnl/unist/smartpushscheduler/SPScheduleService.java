package msnl.unist.smartpushscheduler;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import java.lang.Math;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

public class SPScheduleService extends Service implements SensorEventListener {
    
    static final int NOTI_ARRIVED = 1;
    static final int NOTI_REMOVED = 2;
    
    public static final String ACTION_CUSTOM = "msnl.unist.smartpushscheduler.ACTION_CUSTOM";
    private boolean isRegistered = false;
    Queue<SPNotification> notiQueue = new LinkedList<SPNotification>();

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyro;
    
    @Override
    public void onCreate() {
	super.onCreate();
	Log.i("SPScheduleService", "onCreate()");
	registerScreenEvent();

	if (!isRegistered) {
	    isRegistered = true;
	}

	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.i("SPScheduleService", "onStartCommand()");
	return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	Log.i("SPScheduleService", "onDestroy()");
	if (isRegistered) {
	    unregisterReceiver(mReceiver);
	    isRegistered = false;
	}
    }

    class IncomingHandler extends Handler {
	@Override
	public void handleMessage(Message msg) {
	    Log.i("SPScheduleService", "message received : " + msg.what);
	    switch (msg.what) {
	    case NOTI_ARRIVED:
		try {
		    String rmsg = (String) msg.getData().getString("data");
		    JSONObject json = new JSONObject(rmsg);
		    String title = json.getString("title");
		    String body = json.getString("body");
		    String packageName = json.getString("package");
		    Log.i("SPScheduleService", "title : " + title);
		    Log.i("SPScheduleService", "body : " + body);
		    Log.i("SPScheduleService", "package : " + packageName);
		    notiQueue.offer(new SPNotification(title, body, packageName, System.currentTimeMillis()));
		} catch (JSONException e) {
		    Log.i("SPScheduleService", "exception : " + e.getMessage());
		}
		
		// showNotiProbMap();
		    
		break;
	    case NOTI_REMOVED:
		Log.i("SPScheduleService", "Noti Removed");
		Calendar cal = Calendar.getInstance();
		// SPDay d =  dayList.get(cal.get(Calendar.DAY_OF_WEEK) - 1);
		// SPHour h = d.getHourList().get(cal.get(Calendar.HOUR_OF_DAY));

		// h.decisionCount += 1;
		// showNotiProbMap();
		
		break;
	    default:
		super.handleMessage(msg);
		break;
	    }
	}
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
	Log.i("SPScheduleService", "onBind()");
	return mMessenger.getBinder();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
		    Log.i("SPScheduleService", "screen off");
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
		    Log.i("SPScheduleService", "screen on");
		    SPNotification noti = notiQueue.poll();
		    if (noti != null) noti.show(SPScheduleService.this);
		}
	    }
	};

    private void registerScreenEvent() {
	IntentFilter filterScreenON = new IntentFilter(Intent.ACTION_SCREEN_ON);
	registerReceiver(mReceiver, filterScreenON);

	IntentFilter filterScreenOFF = new IntentFilter(Intent.ACTION_SCREEN_OFF);
	registerReceiver(mReceiver, filterScreenOFF);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
	
    }
    
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
	    SPNotification noti = notiQueue.poll();
	    if (noti != null) noti.show(SPScheduleService.this);	    
	}
    }
    
}

