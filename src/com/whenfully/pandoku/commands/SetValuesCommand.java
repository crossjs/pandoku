package com.whenfully.pandoku.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.whenfully.pandoku.history.Command;
import com.whenfully.pandoku.model.Position;
import com.whenfully.pandoku.model.ValueSet;

public class SetValuesCommand extends AbstractCommand {
	private final Position position;
	private final ValueSet values;
	private ValueSet originalValues;

	public SetValuesCommand(Position position, ValueSet values) {
		this.position = position;
		this.values = values;
	}

	private SetValuesCommand(Position position, ValueSet values, ValueSet originalValues) {
		this.position = position;
		this.values = values;
		this.originalValues = originalValues;
	}

	public void execute(PandokuContext context) {
		originalValues = context.getPuzzle().getValues(position.row, position.col);
		redo(context);
	}

	public void undo(PandokuContext context) {
		context.getPuzzle().setValues(position.row, position.col, originalValues);
	}

	public void redo(PandokuContext context) {
		context.getPuzzle().setValues(position.row, position.col, values);
	}

	@Override
	public Command<PandokuContext> mergeDown(Command<PandokuContext> last) {
		if (!(last instanceof SetValuesCommand))
			return null;

		SetValuesCommand other = (SetValuesCommand) last;
		if (!position.equals(other.position))
			return null;

		return new SetValuesCommand(position, values, other.originalValues);
	}

	@Override
	public boolean isEffective() {
		return !values.equals(originalValues);
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(position.row);
		dest.writeInt(position.col);
		dest.writeInt(values.toInt());
		if (originalValues != null)
			dest.writeInt(originalValues.toInt());
	}

	public static final Parcelable.Creator<SetValuesCommand> CREATOR = new Parcelable.Creator<SetValuesCommand>() {
		public SetValuesCommand createFromParcel(Parcel in) {
			int row = in.readInt();
			int col = in.readInt();
			Position position = new Position(row, col);
			ValueSet values = new ValueSet(in.readInt());
			ValueSet originalValues = in.dataAvail() > 0 ? new ValueSet(in.readInt()) : null;
			return new SetValuesCommand(position, values, originalValues);
		}

		public SetValuesCommand[] newArray(int size) {
			return new SetValuesCommand[size];
		}
	};
}
