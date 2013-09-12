package com.whenfully.pandoku.transfer;

import com.whenfully.pandoku.model.ExtraRegion;
import com.whenfully.pandoku.model.ExtraRegions;
import com.whenfully.pandoku.model.Puzzle;
import com.whenfully.pandoku.model.Solution;

public class PuzzleDecoder {
	private PuzzleDecoder() {
	}

	// format: clues|areas|x
	public static Puzzle decode(String puzzleStr) {
		String[] parts = puzzleStr.split("\\|");
		if (parts.length == 0)
			throw new IllegalArgumentException();

		String clues = parts[0];
		String areas = parts.length > 1 ? parts[1] : "";
		String extra = parts.length > 2 ? parts[2] : "";

		int size = (int) Math.sqrt(clues.length());
		if (clues.length() != size * size)
			throw new IllegalArgumentException();

		if (size < 5 || size > 9)
			throw new IllegalArgumentException();

		int[][] areaCodes = parseAreaCodes(size, areas);
		ExtraRegion[] extraRegions = parseExtraRegions(size, extra);

		Puzzle puzzle = new Puzzle(areaCodes, extraRegions);

		int idx = 0;
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				char clueChar = clues.charAt(idx++);
				if (clueChar == ' ' || clueChar == '.')
					continue;

				int clue = decode(clueChar);
				if (clue < 0 || clue >= size)
					throw new IllegalArgumentException();

				puzzle.set(row, col, clue);
			}
		}

		return puzzle;
	}

	public static Solution decodeValues(String values) {
		int size = (int) Math.sqrt(values.length());
		if (values.length() != size * size)
			throw new IllegalArgumentException();

		if (size < 5 || size > 9)
			throw new IllegalArgumentException();

		int[][] result = new int[size][size];

		int idx = 0;
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				char valueChar = values.charAt(idx++);
				if (valueChar == ' ' || valueChar == '.') {
					result[row][col] = Puzzle.UNDEFINED;
					continue;
				}

				int value = decode(valueChar);
				if (value < 0 || value >= size)
					throw new IllegalArgumentException();

				result[row][col] = value;
			}
		}

		return new Solution(result);
	}

	private static int[][] parseAreaCodes(int size, String areas) {
		if (areas.length() == 0)
			return StandardAreas.getAreas(size);

		if (areas.length() != size * size)
			throw new IllegalArgumentException();

		int idx = 0;
		int[][] areaCodes = new int[size][size];
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				char areaChar = areas.charAt(idx++);
				int areaCode = decode(areaChar);
				if (areaCode < 0 || areaCode >= size)
					throw new IllegalArgumentException();

				areaCodes[row][col] = areaCode;
			}
		}

		return areaCodes;
	}

	private static ExtraRegion[] parseExtraRegions(int size, String extra) {
		if (extra.length() == 0)
			return ExtraRegions.none();
		else if (extra.equalsIgnoreCase("X"))
			return ExtraRegions.x(size);
		else if (extra.equalsIgnoreCase("H"))
			return ExtraRegions.hyper(size);
		else if (extra.equalsIgnoreCase("P"))
			return ExtraRegions.percent(size);
		else if (extra.equalsIgnoreCase("C"))
			return ExtraRegions.color(size);
		else
			throw new IllegalArgumentException("Unsupported extra regions: " + extra);
	}

	private static int decode(char encodedValue) {
		if (encodedValue >= '1' && encodedValue <= '9')
			return encodedValue - '1';
		else if (encodedValue == '0')
			return 9;
		else if (encodedValue >= 'A' && encodedValue <= 'Z')
			return encodedValue + 10 - 'A';
		else if (encodedValue >= 'a' && encodedValue <= 'z')
			return encodedValue + 10 - 'a';
		else
			throw new IllegalArgumentException();
	}
}
