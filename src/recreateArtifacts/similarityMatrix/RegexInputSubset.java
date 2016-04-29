package recreateArtifacts.similarityMatrix;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * wraps a RegexInputGroup to make it smaller, keeping the rest of the
 * functionality the same.
 * 
 * The primary intention of this class is for testing and development!
 * 
 * This could mean some empty row folders, but that is not so bad.
 * 
 * @author cc
 *
 */
public class RegexInputSubset extends RegexInputGroup {

	private int[] subsetKeys;

	public RegexInputSubset(String filteredCorpusPath, String inputSetPath, String rowBase, int nMatchStrings,
			String inputDelimiter, List<Integer> includedRowIndices) throws Exception {
		super(filteredCorpusPath, inputSetPath, rowBase, nMatchStrings, inputDelimiter);

		// this represents yet another mapping -
		// this time from a the smaller matrix index
		// to the larger matrix index, used to get all values
		// EXCEPT row outputs
		subsetKeys = new int[includedRowIndices.size()];
		for (int i = 0; i < subsetKeys.length; i++) {
			subsetKeys[i] = includedRowIndices.get(i);
		}
	}

	public RegexInputSubset(RegexInputGroup completeGroup, LinkedList<Integer> includedRowIndices) {
		super(completeGroup);
		subsetKeys = new int[includedRowIndices.size()];
		for (int i = 0; i < subsetKeys.length; i++) {
			subsetKeys[i] = includedRowIndices.get(i);
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

	public String getRowPath(Integer rowIndex) {
		return super.getRowPath(subsetKeys[rowIndex]);
	}

	public double[] getMatrixRowValuesFromFile(Integer rowIndex) throws IOException {
		return super.getMatrixRowValuesFromFile(subsetKeys[rowIndex]);
	}

	public int getCorpusIndex(int i) {
		return super.getCorpusIndex(subsetKeys[i]);
	}
	
	/**
	 * this one will give the wrong keys if it is not re-implemented
	 */
	public Integer[] getBatchOfIndicesForBuildingRows(int batchSize) throws IOException {
		return getIndices(batchSize, true);
	}
	
	private Integer[] getIndices(Integer batchSize, boolean buildingRows) throws IOException {
		List<Integer> indices = new LinkedList<Integer>();
		
		// use the right range
		for (Integer rowIndex = 0; rowIndex < subsetKeys.length; rowIndex++) {
			
			boolean includeInBatch = buildingRows ? !super.rowFileExists(subsetKeys[rowIndex]) : super.hasUnverifiedTimeouts(subsetKeys[rowIndex]);
			if (includeInBatch) {
				indices.add(rowIndex);
			}
			if (indices.size() >= batchSize) {
				return indices.toArray(new Integer[indices.size()]);
			}
		}
		return indices.toArray(new Integer[indices.size()]);
	}

}
