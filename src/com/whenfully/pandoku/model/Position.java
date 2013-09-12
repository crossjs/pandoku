package com.whenfully.pandoku.model;

public final class Position implements Comparable<Position> {
	public final int row;
	public final int col;

	public Position(int row, int col) {
		this.row = row;
		this.col = col;
	}

	@Override
	public int hashCode() {
		return row * 9901 + col;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Position))
			return false;

		Position other = (Position) obj;
		return row == other.row && col == other.col;
	}

	public int compareTo(Position o) {
		int diff = row - o.row;
		if (diff != 0)
			return diff;
		return col - o.col;
	}

	@Override
	public String toString() {
		return row + "x" + col;
	}
}
