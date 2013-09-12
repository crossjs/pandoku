package com.whenfully.pandoku.solver;

import com.whenfully.pandoku.model.Puzzle;

/**
 * Gets notified of puzzle solutions and decides if the search should continue.
 */
public interface PuzzleReporter {
	/**
	 * Report a solution.
	 * 
	 * @param solution a solution to the original puzzle. The reporter implementation has to copy
	 *           this solution if it wants to keep it.
	 * @return <code>true</code> if the solver should continue to look for solutions,
	 *         <code>false</code> otherwise.
	 */
	boolean report(Puzzle solution);
}
