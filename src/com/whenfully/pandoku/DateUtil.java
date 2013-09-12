package com.whenfully.pandoku;

import android.content.res.Resources;

public class DateUtil {
	private DateUtil() {
	}

	public static String formatTime(long time) {
		int seconds = (int) (time / 1000);
		int hh = seconds / 3600;
		seconds -= hh * 3600;
		int mm = seconds / 60;
		seconds -= mm * 60;

		return format(hh, mm, seconds);
	}

	private static String format(int hh, int mm, int ss) {
		StringBuilder sb = new StringBuilder();

		if (hh > 0) {
			sb.append(hh);
			sb.append(':');
		}

		if (mm < 10) {
			sb.append('0');
		}
		sb.append(mm);
		sb.append(':');

		if (ss < 10) {
			sb.append('0');
		}
		sb.append(ss);

		return sb.toString();
	}

	public static final String formatTimeSpan(Resources resources, long now, long then) {
		int minutes = (int) ((now - then) / 60000);
		if (minutes == 0)
			return resources.getString(R.string.age_0_minutes);
		if (minutes == 1)
			return resources.getString(R.string.age_1_minute);
		if (minutes < 60)
			return resources.getString(R.string.age_n_minutes, minutes);
		int hours = minutes / 60;
		if (hours == 1)
			return resources.getString(R.string.age_1_hour);
		if (hours < 24)
			return resources.getString(R.string.age_n_hours, hours);
		int days = hours / 24;
		if (days == 1)
			return resources.getString(R.string.age_1_day);
		if (days == 2)
			return resources.getString(R.string.age_2_days);
		if (days < 7)
			return resources.getString(R.string.age_n_days, days);
		int weeks = days / 7;
		if (weeks == 1)
			return resources.getString(R.string.age_1_week);
		return resources.getString(R.string.age_n_weeks, weeks);
	}
}
