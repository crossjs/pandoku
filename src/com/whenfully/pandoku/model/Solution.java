package com.whenfully.pandoku.model;

public class Solution {
	private int[][] solution;

	public Solution(int[][] solution) {
		this.solution = solution;
	}

	public Solution(Puzzle puzzle) {
		if (!puzzle.isSolved())
			throw new IllegalArgumentException();

		final int size = puzzle.getSize();
		this.solution = new int[size][size];

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				int value = puzzle.getValue(row, col);
				solution[row][col] = value;
			}
		}
	}

	public int getValue(int row, int col) {
		return solution[row][col];
	}
}
