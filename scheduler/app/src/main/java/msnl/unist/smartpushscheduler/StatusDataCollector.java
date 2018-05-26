package msnl.unist.smartpushscheduler;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StatusDataCollector {

    // immediate -> 1 / scheduled : 0
    public static void saveNotiInfo(Context context, long postedTime, String packageName, int uid, int immediate) {
	String content = postedTime + "," + packageName + "," + uid + "\n";
	writeToFile(context, content, "noti.csv", "time,package,uid\n");
    }
    
    // type 0 : seen, type 1 : decision
    public static void saveSeenDecisionTime(Context context, long postedTime, String packageName, int uid, int type) {
	String content = System.currentTimeMillis() + "," + postedTime + "," + packageName + "," + uid + "," + type + "\n";
	writeToFile(context, content, "noti.csv", "time,posttime,package,uid,type\n");
    }

    private static void writeToFile(Context context, String content, String fileName, String firstLine) {
	FileWriter fileWriter = null;
	// String dir = context.getFilesDir().getAbsolutePath();
	// File file = new File("/storage/emulated/0/" + fileName);
	File file = new File(context.getFilesDir(), fileName);
	Log.i("StatusDataCollector", file.getAbsolutePath());
	boolean notfound = false;
	if (!file.exists()) {
	    Log.i("StatusDataCollector", "Not found");
	    notfound = true;
	}
	try {
	    fileWriter = new FileWriter(file, true);
	    if (notfound) fileWriter.append(firstLine);
	    fileWriter.append(content);
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    try {
		fileWriter.flush();
		fileWriter.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
	
}
