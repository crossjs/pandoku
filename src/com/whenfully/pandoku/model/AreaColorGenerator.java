package com.whenfully.pandoku.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import android.util.Log;

public class AreaColorGenerator {
	private static final String TAG = AreaColorGenerator.class.getName();

	public AreaColorGenerator() {
	}

	public int[] generate(Puzzle puzzle) {
		Node[] graph = buildGraph(puzzle);
		paintGraph(graph);
		return buildAreaColors(graph);
	}

	private Node[] buildGraph(Puzzle puzzle) {
		final int size = puzzle.getSize();

		Node[] nodes = new Node[size];
		for (int i = 0; i < size; i++)
			nodes[i] = new Node();

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				int areaCode = puzzle.getAreaCode(row, col);

				if (row < size - 1) {
					int nextRowAreaCode = puzzle.getAreaCode(row + 1, col);
					link(nodes, areaCode, nextRowAreaCode);
				}

				if (col < size - 1) {
					int nextColAreaCode = puzzle.getAreaCode(row, col + 1);
					link(nodes, areaCode, nextColAreaCode);
				}
			}
		}

		return nodes;
	}

	private void link(Node[] nodes, int areaCode1, int areaCode2) {
		if (areaCode1 == areaCode2)
			return;

		Node node1 = nodes[areaCode1];
		Node node2 = nodes[areaCode2];
		node1.neighbors.add(node2);
		node2.neighbors.add(node1);
	}

	private void paintGraph(Node[] graph) {
		Integer[] indexes = getIndexesByNodeDegree(graph);

		int maxColor = -1;

		for (int index : indexes) {
			final int freeColor = findFreeColor(graph[index]);
			graph[index].color = freeColor;
			maxColor = Math.max(maxColor, freeColor);
		}

		int numberOfColors = maxColor + 1;

		if (numberOfColors > 4) {
			// TODO: try a different algorithm (but numberOfColors is never > 4 for all
			//       1500 squiggly puzzles in andoku)
			Log.w(TAG, "Puzzle requires more than 4 colors: " + numberOfColors);
		}
	}

	private Integer[] getIndexesByNodeDegree(final Node[] graph) {
		Integer[] indexes = new Integer[graph.length];
		for (int index = 0; index < graph.length; index++)
			indexes[index] = index;

		Arrays.sort(indexes, new Comparator<Integer>() {
			public int compare(Integer index1, Integer index2) {
				int degree1 = graph[index1].neighbors.size();
				int degree2 = graph[index2].neighbors.size();
				return degree2 - degree1;
			}
		});

		return indexes;
	}

	private int findFreeColor(Node node) {
		HashSet<Integer> occupied = new HashSet<Integer>();
		for (Node neighbor : node.neighbors) {
			if (neighbor.color != -1)
				occupied.add(neighbor.color);
		}

		for (int color = 0;; color++) {
			if (!occupied.contains(color))
				return color;
		}
	}

	private int[] buildAreaColors(Node[] graph) {
		int[] areaColors = new int[graph.length];

		for (int i = 0; i < graph.length; i++)
			areaColors[i] = graph[i].color;

		return areaColors;
	}

	private static final class Node {
		public HashSet<Node> neighbors = new HashSet<Node>();
		public int color = -1;
	}
}
