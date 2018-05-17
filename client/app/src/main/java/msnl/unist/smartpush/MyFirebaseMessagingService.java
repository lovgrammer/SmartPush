package msnl.unist.smartpush;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jylee on 2018-04-23.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "MyFirebase";
    
    private Messenger mService = null;
    private boolean mBound;
        
    static final int NOTI_ARRIVED = 1;
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
	if (!mBound) {
	    doBindService();
	}
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	Log.i("MyFirebaseMessagingService", "onDestroy()");
	if (mBound) {
	    doUnbindService();
	}
    }

    public void doBindService() {
	try {
	    Intent intentForSPService = new Intent();
	    Log.i("MyFirebaseMessagingService", "init intent.componentName");
	    intentForSPService.setComponent(new ComponentName("msnl.unist.smartpushscheduler", "msnl.unist.smartpushscheduler.SPScheduleService"));
	    intentForSPService.setAction(ACTION_CUSTOM);
	    Log.i("MyFirebaseMessagingService", "Before bindService");
	    if (bindService(intentForSPService, mConnection, 0)) {
		Log.i("MyFirebaseMessagingService", "Binding to Scheduler returned true");
	    } else {
		Log.i("MyFirebaseMessagingService", "Binding to Scheduler returned false");
	    }
	} catch (SecurityException e)  {
	    Log.e("MyFirebaseMessagingService", "cannot bind to Scheduler check permission in Manifest : " + e.getMessage());
	}
    }

    public void doUnbindService() {
	if (mBound) {
	    unbindService(mConnection);
	    mBound = false;
	}
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

	scheduleJob(remoteMessage);
    }

    @Override
    public void handleIntent(Intent intent) {
	try {
	    if (intent.getExtras() != null) {
		RemoteMessage.Builder builder = new RemoteMessage.Builder("MyFirebaseMessagingService");
		for (String key : intent.getExtras().keySet()) {
		    builder.addData(key, intent.getExtras().get(key).toString());
		}
		onMessageReceived(builder.build());
	    } else {
		super.handleIntent(intent);
	    }
	} catch (Exception e) {
	    super.handleIntent(intent);
	}
    }

    public void scheduleJob(RemoteMessage remoteMessage) {
	Log.i("MyFirebaseMessagingService", "scheduleJob!");
	if (!mBound)  return;
	Log.i("MyFirebaseMessagingService", "say hello!");

	try {
	    JSONObject json = new JSONObject();
	    json.put("title", remoteMessage.getNotification().getTitle());
	    json.put("body", remoteMessage.getNotification().getBody());
	    json.put("package", "msnl.unist.smartpush");
	
	    Message msg = Message.obtain(null, NOTI_ARRIVED, 0, 0);
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

    public void handleNow() {

    }

}
