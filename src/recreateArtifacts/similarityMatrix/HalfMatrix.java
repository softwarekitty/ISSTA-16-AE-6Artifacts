package recreateArtifacts.similarityMatrix;

import java.util.HashMap;

public class HalfMatrix {
	private double[][] values;

	public HalfMatrix(WholeMatrix original) {
		int n = original.getN();
		values = new double[n][];
		for (int i = 0; i < n; i++) {
			values[i] = new double[i];
			for (int j = 0; j < i; j++) {
				values[i][j] = original.getAvg(i, j);
			}
		}
	}

	// row is i, col is j
	// so triangle is in lower left
	public double get(int row, int col) {
		if (row == col) {
			return 1;
		} else if (row < col) {
			return values[col][row];
		} else {
			return values[row][col];
		}
	}

	// row is i, col is j
	// so triangle is in lower left
	public void set(int row, int col, double d) {
		if (row == col) {
			return;
		}
		if (row < col) {
			values[col][row] = d;
		} else {
			values[row][col] = d;
		}
	}

	public String getABC(double minEdgeWeight, HashMap<Integer, Integer> keyConverter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			int key_i = keyConverter.get(i);
			for (int j = 0; j < values[i].length; j++) {
				int key_j = keyConverter.get(i);
				double edgeWeight = values[i][j];

				if (edgeWeight >= minEdgeWeight) {
					sb.append(key_i + " " + key_j + " " + edgeWeight + "\n");
				}
			}
			sb.append(key_i + " " + key_i + " " + 1.0 + "\n");
		}
		return sb.toString();
	}
}