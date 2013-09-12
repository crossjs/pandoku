package com.whenfully.pandoku.model;

import java.util.Arrays;
import java.util.List;

public final class Region {
	public final int id;
	public final String type;
	public final int number;
	public final Position[] positions;
	public final ValueSet values;

	public Region(int id, String type, int number, List<Position> positions) {
		this.id = id;
		this.type = type;
		this.number = number;
		this.positions = positions.toArray(new Position[positions.size()]);
		values = new ValueSet();
	}

	public Region(int id, String type, int number, Position[] positions) {
		this.id = id;
		this.type = type;
		this.number = number;
		this.positions = positions;
		values = new ValueSet();
	}

	public String getName() {
		return type + " " + number;
	}

	@Override
	public String toString() {
		return "Region " + type + " " + number + ": " + Arrays.asList(positions);
	}
}
