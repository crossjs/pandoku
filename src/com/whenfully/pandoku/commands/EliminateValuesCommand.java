package com.whenfully.pandoku.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.whenfully.pandoku.TickTimer;
import com.whenfully.pandoku.model.ValueSet;

public class EliminateValuesCommand extends AbstractCommand {
	private static final long TIME_PENALTY_PER_ELIMINATED_VALUE = 1000;

	private ValueSet[][] originalValues;

	public EliminateValuesCommand() {
	}

	private EliminateValuesCommand(ValueSet[][] originalValues) {
		this.originalValues = originalValues;
	}

	public void execute(PandokuContext context) {
		originalValues = saveValues(context.getPuzzle());

		int numberValuesEliminated = context.getPuzzle().eliminateValues();

		long penalty = TIME_PENALTY_PER_ELIMINATED_VALUE * numberValuesEliminated;
		TickTimer timer = context.getTimer();
		timer.setTime(timer.getTime() + penalty);
	}

	public void undo(PandokuContext context) {
		restoreValues(context.getPuzzle(), originalValues);
	}

	public void redo(PandokuContext context) {
		context.getPuzzle().eliminateValues();
	}

	public void writeToParcel(Parcel dest, int flags) {
		final int size = originalValues.length;
		int[] data = new int[size * size];
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				data[row * size + col] = originalValues[row][col].toInt();
			}
		}
		dest.writeInt(size);
		dest.writeIntArray(data);
	}

	public static final Parcelable.Creator<EliminateValuesCommand> CREATOR = new Parcelable.Creator<EliminateValuesCommand>() {
		public EliminateValuesCommand createFromParcel(Parcel in) {
			final int size = in.readInt();
			final int[] data = in.createIntArray();
			ValueSet[][] originalValues = new ValueSet[size][size];
			for (int row = 0; row < size; row++) {
				for (int col = 0; col < size; col++) {
					originalValues[row][col] = new ValueSet(data[row * size + col]);
				}
			}

			return new EliminateValuesCommand(originalValues);
		}

		public EliminateValuesCommand[] newArray(int size) {
			return new EliminateValuesCommand[size];
		}
	};
}
