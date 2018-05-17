package msnl.unist.smartpushscheduler;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

public class SPNotification {
    
    public String title;
    public String body;
    public String packageName;
    public long time;
    
    public SPNotification(String title, String body, String packageName, long time) {
	this.title = title;
	this.body = body;
	this.packageName = packageName;
	this.time = time;
    }

    public void show(Context context)  {
	try {
	    Drawable icon = context.getPackageManager().getApplicationIcon(packageName);
	    BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
	    // imageView.setImageDrawable(icon);

	    NotificationCompat.Builder mBuilder =
		new NotificationCompat.Builder(context)
		.setSmallIcon(context.getApplicationInfo().icon)
		.setLargeIcon(bitmapDrawable.getBitmap())
		.setContentTitle(title)
		// .setContentIntent(pendingIntent)
		.setContentText(body);
	
	    mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
	    NotificationManager notificationManager
		= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    notificationManager.notify(1, mBuilder.build());
	} catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
