package com.whenfully.pandoku.model;

import java.util.ArrayList;
import java.util.List;

public class ExtraRegions {
	private static final ExtraRegion[] NONE = {};

	private ExtraRegions() {
	}

	public static ExtraRegion[] none() {
		return NONE;
	}

	public static ExtraRegion[] x(int size) {
		return new ExtraRegion[] { diag1(size), diag2(size) };
	}

	public static int hyperCount(int size) {
		return size == 9 ? 4 : 0;
	}

	public static ExtraRegion[] hyper(int size) {
		if (size != 9)
			throw new IllegalArgumentException("Hyper restricted to 9x9 for now..");

		return new ExtraRegion[] { square(1, 1), square(5, 1), square(1, 5), square(5, 5) };
	}

	public static int percentCount(int size) {
		return size == 9 ? 3 : 0;
	}

	public static ExtraRegion[] percent(int size) {
		if (size != 9)
			throw new IllegalArgumentException("Percent restricted to 9x9 for now..");

		return new ExtraRegion[] { square(1, 1), diag2(size), square(5, 5) };
	}

	public static int colorCount(int size) {
		return size == 9 ? 9 : 0;
	}

	public static ExtraRegion[] color(int size) {
		if (size != 9)
			throw new IllegalArgumentException("Color restricted to 9x9 for now..");

		return new ExtraRegion[] { col(0), col(1), col(2), col(3), col(4), col(5), col(6), col(7),
				col(8) };
	}

	private static ExtraRegion diag1(int size) {
		List<Position> diag1 = new ArrayList<Position>();

		for (int i = 0; i < size; i++)
			diag1.add(new Position(i, i));

		return new ExtraRegion(diag1);
	}

	private static ExtraRegion diag2(int size) {
		List<Position> diag2 = new ArrayList<Position>();

		for (int i = 0; i < size; i++)
			diag2.add(new Position(i, size - 1 - i));

		return new ExtraRegion(diag2);
	}

	private static ExtraRegion square(int rowOffset, int colOffset) {
		List<Position> positions = new ArrayList<Position>();

		for (int row = rowOffset; row < rowOffset + 3; row++) {
			for (int col = colOffset; col < colOffset + 3; col++) {
				positions.add(new Position(row, col));
			}
		}

		return new ExtraRegion(positions);
	}

	private static ExtraRegion col(int i) {
		int row = i / 3;
		int col = i % 3;

		List<Position> color = new ArrayList<Position>();

		for (int r = 0; r < 9; r += 3)
			for (int c = 0; c < 9; c += 3)
				color.add(new Position(row + r, col + c));

		return new ExtraRegion(color);
	}
}
