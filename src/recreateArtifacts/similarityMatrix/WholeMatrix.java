package recreateArtifacts.similarityMatrix;

public class WholeMatrix {
	private double[][] values;
	private static double weirdValue = -2.0;

	public WholeMatrix(int n) {
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
}