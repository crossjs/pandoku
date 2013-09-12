package com.whenfully.pandoku;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TickTimer {
	private static final String TAG = TickTimer.class.getName();

	private static final int TICK_TIMER_MSG = 0;

	private boolean running = false;

	private long startTime;
	private long stoppedTime = 0;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case TICK_TIMER_MSG:
					long time = sendTick();
					int delay = 1000 - (int) (time % 1000) + 10;
					handler.sendMessageDelayed(handler.obtainMessage(TICK_TIMER_MSG), delay);
					break;

				default:
					super.handleMessage(msg);
			}
		}
	};

	final TickListener listener;

	public TickTimer(TickListener listener) {
		this.listener = listener;
	}

	public void reset() {
		if (Constants.LOG_V)
			Log.v(TAG, "reset()");

		running = false;
		stoppedTime = 0;

		stopTicking();
	}

	public void start() {
		if (Constants.LOG_V)
			Log.v(TAG, "start()");

		if (running)
			return;

		startTime = System.currentTimeMillis() - stoppedTime;
		running = true;

		startTicking();
	}

	public void stop() {
		if (Constants.LOG_V)
			Log.v(TAG, "stop()");

		if (!running)
			return;

		stoppedTime = System.currentTimeMillis() - startTime;
		running = false;

		stopTicking();
	}

	public boolean isRunning() {
		return running;
	}

	public long getTime() {
		if (running)
			return System.currentTimeMillis() - startTime;
		else
			return stoppedTime;
	}

	public void setTime(long time) {
		if (Constants.LOG_V)
			Log.v(TAG, "setTime(" + time + ")");

		if (running)
			startTime = System.currentTimeMillis() - time;
		else
			stoppedTime = time;

		sendTick();
	}

	@Override
	public String toString() {
		return (running ? "R:" : "S:") + getTime();
	}

	private void startTicking() {
		handler.removeMessages(TICK_TIMER_MSG);

		long time = sendTick();
		int delay = 1000 - (int) (time % 1000) + 10;
		handler.sendMessageDelayed(handler.obtainMessage(TICK_TIMER_MSG), delay);
	}

	private void stopTicking() {
		handler.removeMessages(TICK_TIMER_MSG);
		sendTick(); // one last tick to update current time
	}

	private long sendTick() {
		long time = getTime();
		listener.onTick(time);
		return time;
	}
}
