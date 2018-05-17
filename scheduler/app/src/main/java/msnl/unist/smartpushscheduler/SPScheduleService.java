package msnl.unist.smartpushscheduler;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class SPScheduleService extends Service {
    
    static final int NOTI_ARRIVED = 1;
    static final int NOTI_REMOVED = 2;
    
    public static final String ACTION_CUSTOM = "msnl.unist.smartpushscheduler.ACTION_CUSTOM";
    private boolean isRegistered = false;
    Queue<SPNotification> notiQueue = new LinkedList<SPNotification>();
    // public static ArrayList<SPDay> dayList = new ArrayList<SPDay>();
    
    @Override
    public void onCreate() {
	super.onCreate();
	
	Log.i("SPScheduleService", "onCreate()");
	registerScreenEvent();
	if (!isRegistered) {
	    isRegistered = true;
	}

	// for (int i=0; i<7; i++) {
	//     dayList.add(new SPDay());
	// }

	// pushMockData();
	// showNotiProbMap();
    }

    // public void pushMockData() {
    // 	int seed = 0;
    // 	for (int i=0; i<7; i++) {
    // 	    seed++;
    // 	    SPDay d = dayList.get(i);
    // 	    for (SPHour h : d.getHourList()) {
    // 		seed++;
    // 		Random generator = new Random(seed);
    // 		h.decisionCount = generator.nextInt(10);
    // 	    }
    // 	}
    // }

    // public String showNotiProbMap() {
    // 	String ret = "";
    // 	int dayCount=0;
    // 	for (SPDay d : dayList) {
    // 	    int totalDCount = d.getDecisionCount();
    // 	    String text = "";
    // 	    for (SPHour h : d.getHourList()) {
    // 		// float prob = (float) h.decisionCount / totalDCount;
    // 		// text += prob;
    // 		text += h.decisionCount;
    // 		text += ", ";
    // 	    }

    // 	    ret = "[" + dayCount + "] " + text;
    // 	    // text += "\n";
    // 	    Log.i("SPNotiListenerService", "[" + dayCount + "] " + text);
    // 	    dayCount++;
    // 	}
    // 	return ret;
    // }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.i("SPScheduleService", "onStartCommand()");
	return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	if (isRegistered) {
	    unregisterReceiver(mReceiver);
	    isRegistered = false;
	}
	Log.i("SPScheduleService", "onDestroy()");
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
    
}

