package com.whenfully.pandoku.source;

public interface PuzzleSource {
	String getSourceId();

	int numberOfPuzzles();

	PuzzleHolder load(int number);

	void close();
}
