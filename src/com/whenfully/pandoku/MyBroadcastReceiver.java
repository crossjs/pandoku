package com.whenfully.pandoku;

import java.util.LinkedList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = MainActivity.class.getName();

	private static LinkedList<Activity> allActivity = new LinkedList<Activity>();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Constants.LOG_V)
			Log.v(TAG, "onReceive(" + context + ", " + intent + ")");

		if ("com.whenfully.pandoku.exit".equals(intent.getAction())) {
			for (Activity myActivity : allActivity) {
				myActivity.finish();
			}
		}
	}

	public static void register(Activity activity) {
		if (Constants.LOG_V)
			Log.v(TAG, "register(" + activity + ")");
		
		allActivity.add(activity);
	}

	public static void unUegister(Activity activity) {
		if (Constants.LOG_V)
			Log.v(TAG, "unUegister(" + activity + ")");
		
		allActivity.remove(activity);
	}
}
