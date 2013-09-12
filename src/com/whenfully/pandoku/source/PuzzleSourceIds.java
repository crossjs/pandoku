package com.whenfully.pandoku.source;

public class PuzzleSourceIds {
	private static final char SEPARATOR = ':';
	private static final String ASSET_PREFIX = "asset" + SEPARATOR;

	private PuzzleSourceIds() {
	}

	public static String forAssetFolder(String folderName) {
		return ASSET_PREFIX + folderName;
	}

	public static boolean isAssetSource(String puzzleSourceId) {
		return puzzleSourceId.startsWith(ASSET_PREFIX);
	}

	public static String getAssetFolderName(String puzzleSourceId) {
		return puzzleSourceId.substring(ASSET_PREFIX.length());
	}
}
