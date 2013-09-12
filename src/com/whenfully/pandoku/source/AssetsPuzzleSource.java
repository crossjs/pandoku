package com.whenfully.pandoku.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import android.content.res.AssetManager;

import com.whenfully.pandoku.model.Level;
import com.whenfully.pandoku.model.Puzzle;
import com.whenfully.pandoku.transfer.PuzzleDecoder;

class AssetsPuzzleSource implements PuzzleSource {
	private static final String PUZZLES_FOLDER = "puzzles/";

	private final AssetManager assets;
	private final String folderName;

	private final List<String> entries;

	public AssetsPuzzleSource(AssetManager assets, String folderName) {
		this.assets = assets;
		this.folderName = folderName;

		this.entries = loadEntries();
	}

	private List<String> loadEntries() {
		List<String> entries = new ArrayList<String>(100);

		try {
			String puzzleFile = PUZZLES_FOLDER + folderName + ".adk";

			InputStream in = assets.open(puzzleFile);
			try {
				Reader reader = new InputStreamReader(in, "US-ASCII");
				BufferedReader br = new BufferedReader(reader, 512);
				while (true) {
					String line = br.readLine();
					if (line == null)
						break;

					if (line.length() == 0 || line.startsWith("#"))
						continue;
						
						entries.add(line);
				}

				return entries;
			}
			finally {
				in.close();
			}
		}
		catch (IOException e) {
			throw new AssetsPuzzleSourceException(e);
		}
	}

	public String getSourceId() {
		return PuzzleSourceIds.forAssetFolder(folderName);
	}

	public int numberOfPuzzles() {
		return entries.size();
	}

	public PuzzleHolder load(int number) {
		String puzzleStr = entries.get(number);

		Puzzle puzzle = PuzzleDecoder.decode(puzzleStr);

		return new PuzzleHolder(this, number, null, puzzle, getLevel());
	}

	private Level getLevel() {
		final int difficulty = folderName.charAt(folderName.length() - 1) - '0' - 1;
		if (difficulty < 0 || difficulty > 4)
			throw new IllegalStateException();

		return Level.values()[difficulty];
	}

	public void close() {
	}
}
