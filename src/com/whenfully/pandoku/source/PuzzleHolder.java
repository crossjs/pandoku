package com.whenfully.pandoku.source;

import com.whenfully.pandoku.db.PuzzleId;
import com.whenfully.pandoku.model.Level;
import com.whenfully.pandoku.model.Puzzle;

public class PuzzleHolder {
	private final PuzzleSource source;
	private final int number;

	private final String name;
	private final Puzzle puzzle;
	private final Level difficulty;

	public PuzzleHolder(PuzzleSource source, int number, String name, Puzzle puzzle,
			Level difficulty) {
		if (source == null)
			throw new IllegalArgumentException();
		if (puzzle == null)
			throw new IllegalArgumentException();
		if (difficulty == null)
			throw new IllegalArgumentException();

		this.source = source;
		this.number = number;
		this.name = name;
		this.puzzle = puzzle;
		this.difficulty = difficulty;
	}

	public PuzzleSource getSource() {
		return source;
	}

	public int getNumber() {
		return number;
	}

	public PuzzleId getPuzzleId() {
		return new PuzzleId(source.getSourceId(), number);
	}

	public String getName() {
		return name;
	}

	public Puzzle getPuzzle() {
		return puzzle;
	}

	public Level getLevel() {
		return difficulty;
	}
}
