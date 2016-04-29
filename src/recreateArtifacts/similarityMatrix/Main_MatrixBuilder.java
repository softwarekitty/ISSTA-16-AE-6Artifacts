package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import main.io.IOUtil;
import recreateArtifacts.PathUtil;

public class Main_MatrixBuilder {

	private final static int N_MATCH_STRINGS = 400;
	private final static double MIN_SIM = 0.75;
	private final static String INPUT_DELIMITER = "\n2jxj8oSFLPEfrP4q8yVn6h0vf\n";
	// private final static int BUILD_BATCH_SIZE = 256;
	public final static int BUILD_BATCH_SIZE = 600;
	public final static int VERIFY_BATCH_SIZE = 45;


	public static void main(String[] args) throws Exception {
		System.out.println("beginning MatrixBuilder");
		RegexInputGroup group = getCompleteGroup();
		buildMatrix(group);
	}
	
	public static void buildMatrix(RegexInputGroup group) throws Exception{
		int[] unbuiltIndices = group.getUnbuiltRowIndices();
		if(unbuiltIndices.length>0){
			build(group, unbuiltIndices);
			System.out.println("Main_MatrixBuilder completed a building step");
		}else{
			
			
			// if some cells are unverified, then verify those
			// we use the total number in the next step, so get them all
			List<CellResult> unverifiedCells = group.getInvalidCellResults();
			if(unverifiedCells.size() >0){
				verify(group, unverifiedCells);
				System.out.println("Main_MatrixBuilder completed a verifying step");
			}else{
				
				// if all rows are verified, then export
				export(group);		
			}
		}
	}

	public static RegexInputGroup getCompleteGroup() throws Exception {
		List<String> indexRegexLines = IOUtil.readLines(PathUtil.pathToFilteredCorpus());
		PathBucketer bucketer = new PathBucketer(indexRegexLines.size(),PathUtil.pathToInputSetBase(),
				PathUtil.pathToRowsBase());
		return new RegexInputGroup(indexRegexLines, N_MATCH_STRINGS, INPUT_DELIMITER, bucketer);
	}

	public static void build(RegexInputGroup group,int[] unbuiltIndices) throws Exception {
		int nRowsBefore = group.size() - unbuiltIndices.length;
		if(unbuiltIndices.length > BUILD_BATCH_SIZE){
			unbuiltIndices = Arrays.copyOfRange(unbuiltIndices, 0, BUILD_BATCH_SIZE);
		}
		System.out.println("buildBatchSize: " + BUILD_BATCH_SIZE + " nThisBatch: " + unbuiltIndices.length + " nRowsBuiltBefore: "
				+ nRowsBefore + " nRows: " + group.size());
		BatchController.buildBatchOfRows(group, MIN_SIM, unbuiltIndices);
		int[] unbuiltIndices_after = group.getUnbuiltRowIndices();
		int nRowsAfter = group.size() - unbuiltIndices_after.length;
		System.out.println("nRowsAfter: " + nRowsAfter + " diff: " + (nRowsAfter - nRowsBefore) + " batchSize:"
				+ BUILD_BATCH_SIZE + " nThisBatch: " + unbuiltIndices.length);
	}

	public static void verify(RegexInputGroup group,List<CellResult> unverifiedCells) throws Exception {
		int nInvalidCellsBefore = unverifiedCells.size();
		if(unverifiedCells.size() > VERIFY_BATCH_SIZE){
			unverifiedCells = unverifiedCells.subList(0, VERIFY_BATCH_SIZE);
		}
		System.out.println("verifyBatchSize: " + VERIFY_BATCH_SIZE + " nThisBatch: " + unverifiedCells.size() + " nInvalidCellsBefore: "
				+ nInvalidCellsBefore + " nRows: " + group.size());
		BatchController.verifyBatchOfCells(group, MIN_SIM, unverifiedCells);
		List<CellResult> unverifiedCells_after = group.getInvalidCellResults();
		
		int nInvalidCellsAfter = unverifiedCells_after.size();
		System.out.println("verifyBatchSize: " + VERIFY_BATCH_SIZE + " nThisBatch: " + unverifiedCells_after.size() + " nInvalidCellsAfter: "
				+ nInvalidCellsAfter + " nRows: " + group.size());
	}

	public static void export(RegexInputGroup group) throws IOException {
		String abcOutputPath = PathUtil.getPathMatrix() + "output/similarityGraph.abc";
		createABC(abcOutputPath, group);
		System.out.println("abc file written - exiting");
	}

	private static void createABC(String abcOutputPath, RegexInputGroup group) throws IOException {
		DecimalFormat df5 = new DecimalFormat("0.#####");
		

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
			MatrixRow mr = group.getRow(rowIndex);
			double[] rowValues = mr.getValues();
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
					abcContent.append(key_i + " " + key_j + " " + df5.format(edgeWeight) + "\n");
				}
			}
			abcContent.append(key_i + " " + key_i + " 1\n");
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
