package com.whenfully.pandoku.transfer;

public class StandardAreas {
	private static final int[][] STD_5 = { { 0, 0, 0, 1, 1 }, { 0, 0, 4, 1, 1 }, { 2, 4, 4, 4, 1 },
			{ 2, 2, 4, 3, 3 }, { 2, 2, 3, 3, 3 } };

	private static final int[][] STD_6 = { { 0, 0, 0, 1, 1, 1 }, { 0, 0, 0, 1, 1, 1 },
			{ 2, 2, 2, 3, 3, 3 }, { 2, 2, 2, 3, 3, 3 }, { 4, 4, 4, 5, 5, 5 }, { 4, 4, 4, 5, 5, 5 } };

	private static final int[][] STD_7 = { { 0, 0, 0, 0, 1, 1, 1 }, { 0, 0, 0, 1, 1, 1, 1 },
			{ 2, 2, 2, 3, 4, 4, 4 }, { 2, 3, 3, 3, 3, 3, 4 }, { 2, 2, 2, 3, 4, 4, 4 },
			{ 5, 5, 5, 5, 6, 6, 6 }, { 5, 5, 5, 6, 6, 6, 6 } };

	private static final int[][] STD_8 = { { 0, 0, 0, 0, 1, 1, 1, 1 }, { 0, 0, 0, 0, 1, 1, 1, 1 },
			{ 2, 2, 2, 2, 3, 3, 3, 3 }, { 2, 2, 2, 2, 3, 3, 3, 3 }, { 4, 4, 4, 4, 5, 5, 5, 5 },
			{ 4, 4, 4, 4, 5, 5, 5, 5 }, { 6, 6, 6, 6, 7, 7, 7, 7 }, { 6, 6, 6, 6, 7, 7, 7, 7 } };

	private static final int[][] STD_9 = { { 0, 0, 0, 1, 1, 1, 2, 2, 2 },
			{ 0, 0, 0, 1, 1, 1, 2, 2, 2 }, { 0, 0, 0, 1, 1, 1, 2, 2, 2 },
			{ 3, 3, 3, 4, 4, 4, 5, 5, 5 }, { 3, 3, 3, 4, 4, 4, 5, 5, 5 },
			{ 3, 3, 3, 4, 4, 4, 5, 5, 5 }, { 6, 6, 6, 7, 7, 7, 8, 8, 8 },
			{ 6, 6, 6, 7, 7, 7, 8, 8, 8 }, { 6, 6, 6, 7, 7, 7, 8, 8, 8 } };

	private StandardAreas() {
	}

	public static int[][] getAreas(int size) {
		switch (size) {
			case 5:
				return STD_5;
			case 6:
				return STD_6;
			case 7:
				return STD_7;
			case 8:
				return STD_8;
			case 9:
				return STD_9;
			default:
				throw new IllegalArgumentException();
		}
	}
}
