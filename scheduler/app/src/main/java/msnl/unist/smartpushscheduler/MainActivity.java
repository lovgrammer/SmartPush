package msnl.unist.smartpushscheduler;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    
    // private Button mNotiButton;
    // private Button mAccButton;

    private Button mStartButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	// mNotiButton = (Button) findViewById(R.id.btn_noti_permission);
	// mAccButton = (Button) findViewById(R.id.btn_acc_permission);
	mStartButton = (Button) findViewById(R.id.btn_start);
	// mNotiButton.setOnClickListener(onButtonsClick);
	// mAccButton.setOnClickListener(onButtonsClick);
	mStartButton.setOnClickListener(onButtonsClick);
    }

    OnClickListener onButtonsClick = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		switch(v.getId()) {
		// case R.id.btn_noti_permission:
		//     boolean isPermissionAllowed = isNotiPermissionAllowed();
		//     if (!isPermissionAllowed) {
		// 	Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
		// 	startActivity(intent);
		//     }
		//     // Intent intent = new Intent(MainActivity.this, SPScheduleService.class);
		//     // startService(intent);
		//     break;
		// case R.id.btn_acc_permission:
		//     startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
		//     break;
		case R.id.btn_start:
		    // The request code used in ActivityCompat.requestPermissions()
		    // and returned in the Activity's onRequestPermissionsResult()
		    int PERMISSION_ALL = 1; 
		    String[] PERMISSIONS = {
			Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.BIND_ACCESSIBILITY_SERVICE
		    };

		    if(!hasPermissions(MainActivity.this, PERMISSIONS)){
			ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
		    }		    
		    break;
		}
	    }
	};

    private boolean isNotiPermissionAllowed() {
	Set<String> notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(this);
	String myPackageName = getPackageName();
	for (String packageName : notiListenerSet) {
	    if (packageName == null) continue;
	    if (packageName.equals(myPackageName)) return true;
	}
	return false;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
	if (context != null && permissions != null) {
	    for (String permission : permissions) {
		if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
		    return false;
		}
	    }
	}
	return true;
    }
}
