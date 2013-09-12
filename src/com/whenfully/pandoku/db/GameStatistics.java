package com.whenfully.pandoku.db;

public class GameStatistics {
	public final int numGamesSolved;
	public final long sumTime;
	public final long minTime;

	public GameStatistics(int numGamesSolved, long sumTime, long minTime) {
		this.numGamesSolved = numGamesSolved;
		this.sumTime = sumTime;
		this.minTime = minTime;
	}

	public long getAverageTime() {
		return numGamesSolved == 0 ? 0 : sumTime / numGamesSolved;
	}
}
