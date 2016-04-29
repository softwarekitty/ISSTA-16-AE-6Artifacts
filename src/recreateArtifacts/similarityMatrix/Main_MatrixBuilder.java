package recreateArtifacts.similarityMatrix;

import recreateArtifacts.PathUtil;

public class Main_MatrixBuilder {

	private final static int N_MATCH_STRINGS = 400;
	private final static double MIN_SIM = 0.75;
	private final static String INPUT_DELIMITER = "\n2jxj8oSFLPEfrP4q8yVn6h0vf\n";
	private final static int BATCH_SIZE = 256;

	public static void main(String[] args) throws Exception {
		System.out.println("beginning MatrixBuilder");
		RegexInputGroup group = new RegexInputGroup(PathUtil.pathToFilteredCorpus(), PathUtil.pathToInputSetBase(),
				PathUtil.pathToRowsBase(), N_MATCH_STRINGS, INPUT_DELIMITER);
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

	// private static void createABC(String abcOutputPath, double minSimilarity,
	// RegexInputGroup group, String allRowsBase)
	// throws IOException {
	// int nRows = group.size();
	// Matrix matrix = new Matrix(group);
	// for (Integer rowIndex = 0; rowIndex < nRows; rowIndex++) {
	//
	//
	//
	//
	// MatrixRow mr = new MatrixRow(allRowsBase, rowIndex, nRows);
	// double[] rowValues = mr.getValues();
	// for (Integer j = 0; j < nRows; j++) {
	// matrix.set(rowIndex, j, rowValues[j]);
	// }
	// }
	// String abcContent = matrix.getABC(minSimilarity, group);
	// IOUtil.createAndWrite(new File(abcOutputPath), abcContent);
	// }
}
