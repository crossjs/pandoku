package com.whenfully.pandoku.solver;

import com.whenfully.pandoku.model.Puzzle;

/**
 * Puzzle reporter that can be used to determine if a puzzle has a unique solution. Also stores that
 * solution.
 */
public class UniqueSolutionReporter implements PuzzleReporter {
	private Puzzle solution;
	private int solutions = 0;

	public boolean report(Puzzle solution) {
		this.solution = new Puzzle(solution);
		return ++solutions == 1;
	}

	public Puzzle getSolution() {
		return solution;
	}

	public boolean hasSolution() {
		return solutions > 0;
	}

	public boolean hasUniqueSolution() {
		return solutions == 1;
	}

	public boolean hasMultipleSolutions() {
		return solutions > 1;
	}
}
