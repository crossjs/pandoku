package com.whenfully.pandoku.dlx;

public interface DlxListener {
	boolean select(Data row);

	void deselect(Data row);

	boolean solutionFound();
}
