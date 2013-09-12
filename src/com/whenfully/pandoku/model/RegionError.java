package com.whenfully.pandoku.model;

public class RegionError {
	public final Position p1;
	public final Position p2;

	public RegionError(Position p1, Position p2) {
		boolean swap = p1.compareTo(p2) > 0;
		this.p1 = swap ? p2 : p1;
		this.p2 = swap ? p1 : p2;
	}

	@Override
	public int hashCode() {
		return p1.hashCode() * 9901 + p2.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof RegionError))
			return false;

		RegionError other = (RegionError) obj;
		return p1.equals(other.p1) && p2.equals(other.p2);
	}

	@Override
	public String toString() {
		return p1 + "-" + p2;
	}
}
