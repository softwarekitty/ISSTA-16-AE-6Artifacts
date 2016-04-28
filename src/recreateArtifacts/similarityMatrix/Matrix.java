package recreateArtifacts.similarityMatrix;

import java.util.HashMap;

public class Matrix {
	private double[][] values;
	private static double weirdValue = -2.0;

	public Matrix(int n) {
		values = new double[n][];
		for (int i = 0; i < n; i++) {
			values[i] = new double[n];
			for (int j = 0; j < n; j++) {
				// weird value should help detect errors
				values[i][j] = weirdValue;
			}
		}
	}

	public double getAvg(int row, int col) {
		if (row == col) {
			return 1;
		} else {
			return (values[row][col] + values[col][row]) / 2.0;
		}
	}

	public void set(int row, int col, double d) {
		if (row == col) {
			return;
		} else {
			values[row][col] = d;
		}
	}

	public int getN() {
		return values.length;
	}

	public boolean cellIsEmpty(int i, int j) {
		return values[i][j] == weirdValue;
	}

	public String getABC(double minSimilarity, HashMap<Integer, Integer> keyConverter) {
		int n = getN();
		double[][] halfMatrixValues = new double[n][];
		for (int i = 0; i < n; i++) {
			halfMatrixValues[i] = new double[i];
			for (int j = 0; j < i; j++) {
				halfMatrixValues[i][j] = getAvg(i, j);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < halfMatrixValues.length; i++) {
			int key_i = keyConverter.get(i);
			for (int j = 0; j < halfMatrixValues[i].length; j++) {
				int key_j = keyConverter.get(i);
				double edgeWeight = halfMatrixValues[i][j];

				if (edgeWeight >= minSimilarity) {
					sb.append(key_i + " " + key_j + " " + edgeWeight + "\n");
				}
			}
			sb.append(key_i + " " + key_i + " " + 1.0 + "\n");
		}
		return sb.toString();
	}
}