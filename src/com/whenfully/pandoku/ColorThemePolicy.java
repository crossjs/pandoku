package com.whenfully.pandoku;

import com.whenfully.pandoku.ColorTheme.Builder;

public enum ColorThemePolicy {
	CLASSIC {
		@Override
		public void customize(Builder builder) {
		}
	},

	DARK {
		@Override
		public void customize(Builder builder) {
			builder.backgroudColors = new int[] { 0xff4a4d51 };
			builder.puzzleBackgroundColor = 0xff000000;
			builder.nameTextColor = 0xffffffdd;
			builder.difficultyTextColor = 0xffffffdd;
			builder.sourceTextColor = 0xffffffdd;
			builder.timerTextColor = 0xffffffdd;
			builder.gridColor = 0x66bfbfbf;
			builder.borderColor = 0xffffffff;
			builder.extraRegionColor = 0xcd7b89cd;
			builder.colorSudokuExtraRegionColors = new int[] { 0x77ff0000,
					0x77000000, 0x7700ff00, 0x77808080, 0x7700ffff, 0x770000ff,
					0x77ffff00, 0x77ff00ff, 0x77ffffff };
			builder.valueColor = 0xffddffdd;
			builder.clueColor = 0xffffffdd;
			builder.errorColor = 0xffe60000;
			builder.markedPositionColor = 0xb300ff00;
			builder.markedPositionClueColor = 0xb3ff0000;
			builder.areaColors2 = new int[] { 0xff000000, 0xff333333 };
			builder.areaColors3 = Util.colorRing(0xff000033, 3);
			builder.areaColors4 = Util.colorRing(0xff000033, 4);
			builder.highlightedCellColorSingleDigit = 0xe6e6e600;
			builder.highlightedCellColorMultipleDigits = 0xe6a6a600;
		}
	};

	public abstract void customize(Builder builder);
}
