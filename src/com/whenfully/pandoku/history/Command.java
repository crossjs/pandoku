package com.whenfully.pandoku.history;

import android.os.Parcelable;

public interface Command<C> extends Parcelable {
	void execute(C context);

	void undo(C context);

	void redo(C context);

	Command<C> mergeDown(Command<C> last);

	boolean isEffective();
}
