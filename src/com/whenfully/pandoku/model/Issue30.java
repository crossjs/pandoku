package com.whenfully.pandoku.model;

import java.io.UnsupportedEncodingException;

import android.util.Log;

/**
 * The app "Android Sudoku" published by "SS Studio" on the Android Market was (hopefully!) a
 * rip-off of Pandoku that did not respect the terms of the GNU GPL. All the app did was include
 * AdMob; otherwise it was identical to Pandoku.
 * <p>
 * The author had to rename the package 'com.googlecode.andoku' in 'com.googlecode.ansudoku' because
 * app package names have to be unique. As a consequence the database became incompatible with
 * Pandoku's because it contained Serializable objects and the class names have changed.
 * <p>
 * This code replaces the class name before the object gets deserialized.
 * 
 * @see http://code.google.com/p/andoku/issues/detail?id=30
 */
class Issue30 {
	private static final String TAG = Issue30.class.getName();

	private Issue30() {
	}

	public static byte[] workaround(byte[] serializable) {
		if (serializable.length < 2 || serializable[0] != (byte) 0xac
				|| serializable[1] != (byte) 0xed)
			throw new IllegalArgumentException("Not a Serializable");

		return replace(serializable, "com.googlecode.ansudoku.model.PandokuPuzzle$PuzzleMemento",
				"com.googlecode.andoku.model.PandokuPuzzle$PuzzleMemento");
	}

	private static byte[] replace(byte[] blob, String target, String replacement) {
		byte[] targetBytes = utf(target);
		int idx = indexOf(blob, targetBytes);
		if (idx == -1)
			return blob;

		Log.w(TAG, "Memento from 'Android Sudoku' found. Replacing memento class name...");

		byte[] replacementBytes = utf(replacement);

		final int len1 = idx;
		final int len2 = replacementBytes.length;
		final int len3 = blob.length - idx - targetBytes.length;
		byte[] result = new byte[len1 + len2 + len3];

		System.arraycopy(blob, 0, result, 0, len1);
		System.arraycopy(replacementBytes, 0, result, len1, len2);
		System.arraycopy(blob, len1 + targetBytes.length, result, len1 + len2, len3);

		return result;
	}

	private static byte[] utf(String target) {
		try {
			byte[] tail = target.getBytes("utf-8");
			byte[] result = new byte[tail.length + 2];
			System.arraycopy(tail, 0, result, 2, tail.length);
			result[0] = (byte) (tail.length >>> 8);
			result[1] = (byte) (tail.length & 0xff);
			return result;
		}
		catch (UnsupportedEncodingException e) {
			// cannot happen, utf-8 _is_ a supported encoding
			throw new IllegalStateException(e);
		}
	}

	private static int indexOf(byte[] blob, byte[] target) {
		final int lengthDifference = blob.length - target.length;
		outer: for (int idx = 0; idx < lengthDifference; idx++) {
			for (int i = 0; i < target.length; i++) {
				if (blob[idx + i] != target[i])
					continue outer;
			}
			return idx;
		}

		return -1;
	}
}
