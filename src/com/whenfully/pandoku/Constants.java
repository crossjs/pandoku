package com.whenfully.pandoku;

public class Constants {
	private static final String PREFIX = Constants.class.getPackage().getName()
			+ ".";

	public static final String EXTRA_PUZZLE_SOURCE_ID = PREFIX
			+ "puzzleSourceId";
	public static final String EXTRA_PUZZLE_NUMBER = PREFIX + "puzzleNumber";
	public static final String EXTRA_START_PUZZLE = PREFIX + "start";

	public static final String DIRSEP = "/";
	public static final String PANDOKU_BASE_DIR_NAME = "Pandoku";
	public static final String PANDOKU_BACKUP_DIR_NAME = "backup";
	public static final String PANDOKU_UPDATE_DIR_NAME = "update";
	
	public static final String PACKAGE_NAME = "com.whenfully.pandoku";

	public static final boolean LOG_V = false;
	// TODO: 发布版本时应设置为false

	private Constants() {
	}
}
