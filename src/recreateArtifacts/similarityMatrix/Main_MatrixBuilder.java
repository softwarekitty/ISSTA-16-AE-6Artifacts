package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.io.IOException;

import main.io.IOUtil;
import recreateArtifacts.PathUtil;

public class Main_MatrixBuilder {

	private final static int N_MATCH_STRINGS = 400;
	private final static double MIN_SIM = 0.75;
	private final static String INPUT_DELIMITER = "\n2jxj8oSFLPEfrP4q8yVn6h0vf\n";
	private final static int BATCH_SIZE = 256;

	public static void main(String[] args) throws Exception {
		System.out.println("beginning MatrixBuilder");
		buildVerifyExport(getCompleteGroup());
	}

	// these are broken out of the main method to make testing easier
	public static RegexInputGroup getCompleteGroup() throws Exception {
		return new RegexInputGroup(PathUtil.pathToFilteredCorpus(), PathUtil.pathToInputSetBase(),
				PathUtil.pathToRowsBase(), N_MATCH_STRINGS, INPUT_DELIMITER);
	}

	public static void buildVerifyExport(RegexInputGroup group) throws Exception {
		int nRowsBefore = group.nRowsExist();
		if (nRowsBefore == group.size()) {
			System.out.println("all row files are present! Counting unverified rows");
			System.exit(0);
			// int unverifiedTimeoutRows =
			// RowUtil.countUnverifiedRows(allRowsBase, group.size());
			// if (unverifiedTimeoutRows > 0) {
			// System.out.println("why are there unverified rows?");
			// System.exit(1);
			// } else {
			// System.out.println("all cells are valid - creating matrices and
			// abc file");
			// String abcOutputPath = PathUtil.getPathMatrix() +
			// "output/similarityGraph.abc";
			// createABC(abcOutputPath, MIN_SIM, group);
			// System.out.println("matrix and abc files are written - exiting");
			// return;
			// }
		} else {

			System.out.println("batchSize: " + BATCH_SIZE + " nRowsBefore: " + nRowsBefore + " nRows: " + group.size());
			BatchController.runBatchOfRows(group, BATCH_SIZE, MIN_SIM);
			int nRowsAfter = group.nRowsExist();
			System.out.println(
					"nRowsAfter: " + nRowsAfter + " diff: " + (nRowsAfter - nRowsBefore) + " batchSize:" + BATCH_SIZE);
			return;
		}
	}

	private static void createABC(String abcOutputPath, RegexInputGroup group) throws IOException {

		// initialize an empty matrix
		double unsetValue = -2.1435465768;
		int n = group.size();
		double[][] matrix = new double[n][];
		for (int i = 0; i < n; i++) {
			matrix[i] = new double[n];
			for (int j = 0; j < n; j++) {
				// weird value should help detect errors
				matrix[i][j] = unsetValue;
			}
		}

		// populate the matrix with values pulled from row files
		for (Integer rowIndex = 0; rowIndex < n; rowIndex++) {
			double[] rowValues = group.getMatrixRowValuesFromFile(rowIndex);
			for (Integer j = 0; j < n; j++) {
				matrix[rowIndex][j] = rowValues[j];
			}
		}

		// use the whole matrix to populate a half-matrix
		double[][] halfMatrix = new double[n][];
		for (int i = 0; i < n; i++) {
			halfMatrix[i] = new double[i];
			for (int j = 0; j < i; j++) {
				halfMatrix[i][j] = getAvg(i, j, matrix);
			}
		}

		// build the string for the abc file from the half-matrix
		StringBuilder abcContent = new StringBuilder();
		for (int i = 0; i < halfMatrix.length; i++) {
			int key_i = group.getCorpusIndex(i);
			for (int j = 0; j < halfMatrix[i].length; j++) {
				int key_j = group.getCorpusIndex(j);
				double edgeWeight = halfMatrix[i][j];

				if (edgeWeight >= MIN_SIM) {
					abcContent.append(key_i + " " + key_j + " " + edgeWeight + "\n");
				}
			}
			abcContent.append(key_i + " " + key_i + " " + 1.0 + "\n");
		}
		IOUtil.createAndWrite(new File(abcOutputPath), abcContent.toString());
	}

	private static double getAvg(int row, int col, double[][] values) {
		if (row == col) {
			return 1;
		} else {
			return (values[row][col] + values[col][row]) / 2.0;
		}
	}
}
