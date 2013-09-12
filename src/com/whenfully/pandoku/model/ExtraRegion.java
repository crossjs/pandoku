package com.whenfully.pandoku.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ExtraRegion {
	public final Position[] positions;

	public ExtraRegion(List<Position> positions) {
		this.positions = positions.toArray(new Position[positions.size()]);
	}

	public ExtraRegion(Position[] positions) {
		this.positions = positions;
	}

	@Override
	public int hashCode() {
		// not very fast but probably only needed by PuzzleEncoder/Decoder
		return new HashSet<Position>(Arrays.asList(positions)).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ExtraRegion))
			return false;

		// not very fast but probably only needed by PuzzleEncoder
		ExtraRegion other = (ExtraRegion) obj;
		return new HashSet<Position>(Arrays.asList(positions)).equals(new HashSet<Position>(Arrays
				.asList(other.positions)));
	}
}
