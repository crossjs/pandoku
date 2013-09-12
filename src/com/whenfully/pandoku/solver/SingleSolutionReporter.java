package com.whenfully.pandoku.solver;

import com.whenfully.pandoku.model.Puzzle;

/**
 * Puzzle reporter that captures a single solution and stops searching.
 */
public final class SingleSolutionReporter implements PuzzleReporter {
	private Puzzle solution;

	public boolean report(Puzzle solution) {
		this.solution = new Puzzle(solution);
		return false;
	}

	public Puzzle getSolution() {
		return solution;
	}
}
