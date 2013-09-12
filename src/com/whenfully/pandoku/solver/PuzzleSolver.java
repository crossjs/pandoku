package com.whenfully.pandoku.solver;

import com.whenfully.pandoku.model.Puzzle;

/**
 * Can solve sudoku puzzles and notify a puzzle reporter of its solution(s).
 */
public interface PuzzleSolver {
	/**
	 * Solves the specified puzzle and passes solutions to the specified puzzle reporter.
	 * 
	 * @param puzzle puzzle to solve.
	 * @param reporter will be notified of solutions and decides if more solutions should be searched
	 *           for.
	 */
	void solve(Puzzle puzzle, PuzzleReporter reporter);
}
