package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * wraps a RegexInputGroup to make it smaller, keeping the rest of the
 * functionality the same.
 * 
 * The primary intention of this class is for testing and development!
 * 
 * This could mean some empty row folders, because the bucketer of the original
 * RegexInputGroup is still used the same.
 * 
 * @author cc
 *
 */
public class RegexInputSubset extends RegexInputGroup {

	private int[] subsetKeys;

	public RegexInputSubset(RegexInputGroup completeGroup, TreeSet<Integer> includedRowIndices) {
		super(completeGroup);
		subsetKeys = new int[includedRowIndices.size()];
		int subsetKeyIndex = 0;
		for (Integer regexInputRowIndex : includedRowIndices) {
			subsetKeys[subsetKeyIndex++] = regexInputRowIndex;
		}
	}

	public Regex getRegex(int colIndex) {
		return super.getRegex(subsetKeys[colIndex]);
	}

	public int size() {
		return subsetKeys.length;
	}

	public String[] getInputSetStrings(int rowIndex) throws IOException {
		return super.getInputSetStrings(subsetKeys[rowIndex]);
	}

	public MatrixRow getRow(int rowIndex) throws IOException {
		return super.getRow(subsetKeys[rowIndex]);
	}

	public void setRow(int rowIndex, double[] values, double minSimilarity) {
		super.setRow(subsetKeys[rowIndex], values, minSimilarity);
	}

	public int getCorpusIndex(int i) {
		return super.getCorpusIndex(subsetKeys[i]);
	}

	/**
	 * gets rowIndices for all unbuilt rows
	 * 
	 * @return
	 */
	public int[] getUnbuiltRowIndices() {
		List<Integer> indices = new LinkedList<Integer>();
		for (Integer rowIndex = 0; rowIndex < size(); rowIndex++) {
			if (!(new File(bucketer.getRowPath(subsetKeys[rowIndex])).exists())) {
				indices.add(rowIndex);
			}
		}
		int[] unbuiltRowIndices = new int[indices.size()];
		int arrayIndex = 0;
		for (Integer unbuiltRowIndex : indices) {
			unbuiltRowIndices[arrayIndex++] = unbuiltRowIndex;
		}
		return unbuiltRowIndices;
	}

	/**
	 * gets all unverified CellResults
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<CellResult> getInvalidCellResults() throws IOException {
		List<CellResult> invalidCells = new LinkedList<CellResult>();
		for (Integer rowIndex = 0; rowIndex < size(); rowIndex++) {
			if (!(new File(bucketer.getRowPath(rowIndex)).exists())) {
				throw new RuntimeException("Cannot find invald cells in row, because row does not exist: " + rowIndex);
			} else {
				MatrixRow mr = this.getRow(rowIndex);
				List<CellResult> rowInvalidResults = mr.getInvalidResults(rowIndex);
				invalidCells.addAll(rowInvalidResults);
			}
		}
		return invalidCells;
	}
	
}
