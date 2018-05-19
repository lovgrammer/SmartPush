package msnl.unist.smartpushscheduler;

import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;

public class SchedulingManager {
    
    private SPNotiListenerService mService;
    private NotificationManager mManager;
    // private MarketSession mMarketSession;
    
    public SchedulingManager(SPNotiListenerService service) {
	mService = service;
	mManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
	// mMarketSession = new MarketSession();
	// mMarketSession.login("lovgrammer@gmail.com", "sinabro1");
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
	
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
	
    }

    public void onScreenOff() {
	
    }
    
    public void onScreenOn() {
	if ( mService == null ) return;
	StatusBarNotification sbn = mService.getNotiQueue().poll();
	if (sbn != null) {
	    sbn.getNotification().category = "scheduled";
	    mManager.notify(0, sbn.getNotification());
	}
    }

    public void onFgAppChanged(String prevPackageName, String packageName) {
	if ( mService == null ) return;
	// getCategoryWithPackageName(packageName);
	for (StatusBarNotification noti : mService.getNotiQueue()) {
	    if (packageName.equals(noti.getPackageName())) {
		mManager.notify(0, noti.getNotification());
		mService.getNotiQueue().remove(noti);
		break;
	    }
	}
    }

    public void onSensorEventTriggered(String action) {
	if (action.equals("moving")) {
	    StatusBarNotification sbn = mService.getNotiQueue().poll();
	    if (sbn != null) {
		sbn.getNotification().category = "scheduled";
		mManager.notify(0, sbn.getNotification());
	    }	    
	}
    }

    // private void getCategoryWithPackageName(final String packageName) {
    // 	Log.i("SchedulingManager", "category search : " + packageName);
    // 	Thread t = new Thread() {
    // 		public void run() {
    // 		    String query = "pname:" + packageName;
	
    // 		    AppsRequest appsRequest = AppsRequest.newBuilder()
    // 			.setQuery(query)
    // 			.setStartIndex(0)
    // 			.setEntriesCount(10)
    // 			.setWithExtendedInfo(true)
    // 			.build();
	
    // 		    mMarketSession.append(appsRequest, new Callback<AppsResponse>() {
    // 			    @Override
    // 			    public void onResult(ResponseContext context, AppsResponse response) {
    // 				Log.i("SchedulingManager", "response : " + response.toString());
    // 				if (response.getAppCount() > 0) {
    // 				    String category = response.getApp(0).getExtendedInfo().getCategory();
    // 				    Log.i("SchedulingManager", "category : " + category);
    // 				}
    // 			    }
    // 			});
    // 		    mMarketSession.flush();
    // 		}
    // 	    };
    // 	t.start();
    // }
}
