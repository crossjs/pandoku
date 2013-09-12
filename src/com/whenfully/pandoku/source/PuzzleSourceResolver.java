package com.whenfully.pandoku.source;

import android.content.Context;
import android.content.res.AssetManager;

public class PuzzleSourceResolver {
	private PuzzleSourceResolver() {
	}

	public static PuzzleSource resolveSource(Context context,
			String puzzleSourceId) {

		return resolveAssetSource(context,
				PuzzleSourceIds.getAssetFolderName(puzzleSourceId));
	}

	private static PuzzleSource resolveAssetSource(Context context,
			String folderName) {
		AssetManager assets = context.getAssets();
		return new AssetsPuzzleSource(assets, folderName);
	}
}
