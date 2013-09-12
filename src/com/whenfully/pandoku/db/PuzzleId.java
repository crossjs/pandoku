package com.whenfully.pandoku.db;

public class PuzzleId {
	public final String puzzleSourceId;
	public final int number;

	public PuzzleId(String puzzleSourceId, int number) {
		this.puzzleSourceId = puzzleSourceId;
		this.number = number;
	}

	@Override
	public int hashCode() {
		return puzzleSourceId.hashCode() ^ number;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof PuzzleId))
			return false;

		PuzzleId other = (PuzzleId) obj;
		return puzzleSourceId.equals(other.puzzleSourceId) && number == other.number;
	}

	@Override
	public String toString() {
		return puzzleSourceId + ':' + number;
	}
}
