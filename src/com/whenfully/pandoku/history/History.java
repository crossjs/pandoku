package com.whenfully.pandoku.history;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class History<C> {
	private CommandStack<C> undoStack = new CommandStack<C>();
	private CommandStack<C> redoStack = new CommandStack<C>();

	private C context;

	public History(C context) {
		this.context = context;
	}

	public void clear() {
		undoStack.clear();
		redoStack.clear();
	}

	public boolean execute(Command<C> command) {
		if (undoStack.isEmpty()) {
			undoStack.push(command);
		}
		else {
			Command<C> last = undoStack.peek();
			Command<C> merged = command.mergeDown(last);
			if (merged == null) {
				undoStack.push(command);
			}
			else {
				undoStack.pop();
				if (merged.isEffective())
					undoStack.push(merged);
			}
		}

		redoStack.clear();

		command.execute(context);
		return true;
	}

	public boolean canUndo() {
		return !undoStack.isEmpty();
	}

	public boolean undo() {
		if (undoStack.isEmpty()) {
			return false;
		}
		else {
			Command<C> command = undoStack.pop();
			redoStack.push(command);

			command.undo(context);
			return true;
		}
	}

	public boolean canRedo() {
		return !redoStack.isEmpty();
	}

	public boolean redo() {
		if (redoStack.isEmpty()) {
			return false;
		}
		else {
			Command<C> command = redoStack.pop();
			undoStack.push(command);

			command.redo(context);
			return true;
		}
	}

	public Bundle saveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("undoStack", undoStack);
		bundle.putParcelable("redoStack", redoStack);
		return bundle;
	}

	public void restoreInstanceState(Bundle bundle) {
		undoStack = bundle.getParcelable("undoStack");
		redoStack = bundle.getParcelable("redoStack");
	}

	private static class CommandStack<C> implements Parcelable {
		private final List<Command<C>> stack;

		private CommandStack(List<Command<C>> stack) {
			this.stack = stack;
		}

		public CommandStack() {
			stack = new ArrayList<Command<C>>();
		}

		public void clear() {
			stack.clear();
		}

		public boolean isEmpty() {
			return stack.isEmpty();
		}

		public void push(Command<C> command) {
			stack.add(command);
		}

		public Command<C> peek() {
			return stack.get(stack.size() - 1);
		}

		public Command<C> pop() {
			return stack.remove(stack.size() - 1);
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel dest, int flags) {
			final int size = stack.size();
			dest.writeInt(size);
			for (int i = 0; i < size; i++) {
				Command<C> command = stack.get(i);
				dest.writeParcelable(command, flags);
			}
		}

		@SuppressWarnings( { "unused", "unchecked" })
		public static final Parcelable.Creator<CommandStack<?>> CREATOR = new Parcelable.Creator<CommandStack<?>>() {
			@SuppressWarnings("rawtypes")
			public CommandStack<?> createFromParcel(Parcel in) {
				final int size = in.readInt();
				List<Command<?>> stack = new ArrayList<Command<?>>();
				for (int i = 0; i < size; i++) {
					Command<?> command = in.readParcelable(CommandStack.class.getClassLoader());
					stack.add(command);
				}
				return new CommandStack(stack);
			}

			public CommandStack<?>[] newArray(int size) {
				return new CommandStack<?>[size];
			}
		};
	}
}
