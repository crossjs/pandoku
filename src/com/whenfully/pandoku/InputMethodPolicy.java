package com.whenfully.pandoku;

import com.whenfully.pandoku.im.AutomaticInputMethod;
import com.whenfully.pandoku.im.CellThenValuesInputMethod;
import com.whenfully.pandoku.im.InputMethod;
import com.whenfully.pandoku.im.InputMethodTarget;
import com.whenfully.pandoku.im.ValuesThenCellInputMethod;

public enum InputMethodPolicy {
	CELL_THEN_VALUES {
		@Override
		public InputMethod createInputMethod(InputMethodTarget target) {
			return new CellThenValuesInputMethod(target);
		}
	},
	VALUES_THEN_CELL {
		@Override
		public InputMethod createInputMethod(InputMethodTarget target) {
			return new ValuesThenCellInputMethod(target);
		}
	},
	AUTOMATIC {
		@Override
		public InputMethod createInputMethod(InputMethodTarget target) {
			return new AutomaticInputMethod(target);
		}
	};

	public abstract InputMethod createInputMethod(InputMethodTarget target);
}
