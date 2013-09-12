package com.whenfully.pandoku.commands;

import com.whenfully.pandoku.TickTimer;
import com.whenfully.pandoku.model.PandokuPuzzle;

public interface PandokuContext {
	PandokuPuzzle getPuzzle();

	TickTimer getTimer();
}
