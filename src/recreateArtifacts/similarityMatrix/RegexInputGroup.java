package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.io.IOUtil;

/**
 * this class represents a group of regexes and inputs used to test for
 * similarity.
 * 
 * This class also hides a technique for partitioning an arbitrarily large
 * number of files into a number of folders roughly equal to the number of files
 * per folder, based on the square root (of the number of files).
 * 
 * It also hides the complexity of two different types of indices for regexes:
 * the row index (same as column index) and the index within the corpus, which
 * is based on the ordering of raw patterns.
 * 
 * Since many regexes may be excluded from this analysis, we will want to be
 * able to use the matrix indexes in the rest of this code, but be able to
 * output the indexes from the corpus in the final .abc file, so that other code
 * can access the original regex and project set information.
 * 
 * @author cc
 *
 */
public class RegexInputGroup {

	private final TreeMap<Integer, Regex> regexMap;
	private final int[] keys;
	private final String inputDelimiter;
	protected final PathBucketer bucketer;

	/**
	 * creates a regex input group by extracting the regexes from the
	 * filteredCorpus path. Also validates that the input sets are the
	 * appropriate size and creates a folder system for saving row data.
	 */
	public RegexInputGroup(List<String> indexRegexLines, int nInputs, String inputDelimiter, PathBucketer bucketer)
			throws Exception {
		this.inputDelimiter = inputDelimiter;
		this.bucketer = bucketer;

		// map from original corpus index to regex
		regexMap = new TreeMap<Integer, Regex>();

		Pattern numberFinder = Pattern.compile("(\\d+)\\t(.*)");
		for (String line : indexRegexLines) {
			Matcher lineMatcher = numberFinder.matcher(line);
			if (lineMatcher.find()) {
				int index = Integer.parseInt(lineMatcher.group(1));
				String pattern = lineMatcher.group(2);
				regexMap.put(index, new Regex(pattern));
			}
		}
		keys = new int[regexMap.size()];
		Iterator<Integer> it = regexMap.keySet().iterator();
		for (int i = 0; i < keys.length; i++) {
			keys[i] = it.next();
		}
		validateInputSetSizes(nInputs);
	}

	public RegexInputGroup(RegexInputGroup other) {
		this.inputDelimiter = other.inputDelimiter;
		this.regexMap = other.regexMap;
		this.keys = other.keys;
		this.bucketer = other.bucketer;
	}

	/**
	 * gets the regex
	 * 
	 * @param colIndex
	 *            What regex to get.
	 * @return The regex for the given column.
	 */
	public Regex getRegex(int colIndex) {
		return regexMap.get(keys[colIndex]);
	}

	/**
	 * the number of regexes in this group.
	 * 
	 * @return the number of regexes in this group.
	 */
	public int size() {
		return keys.length;
	}

	/**
	 * gets the input set from file
	 * 
	 * @param rowIndex
	 *            What row to get the input set for.
	 * @return The input set.
	 * @throws IOException
	 */
	public String[] getInputSetStrings(int rowIndex) throws IOException {
		String inputSetFileContent = IOUtil.readFileToString(bucketer.getInputSetPath(rowIndex));
		return inputSetFileContent.split(inputDelimiter);
	}

	/**
	 * gets the matrix row from file
	 * 
	 * @param rowIndex
	 *            What row to get.
	 * @return The row.
	 * @throws IOException
	 */
	public MatrixRow getRow(int rowIndex) throws IOException {
		return new MatrixRow(bucketer.getRowPath(rowIndex), size());
	}

	/**
	 * sets the row file contents
	 * 
	 * @param rowIndex
	 *            What row to set.
	 * @param values
	 *            All values to set.
	 * @param minSimilarity
	 *            Below this, specific values are not written to file.
	 */
	public void setRow(int rowIndex, double[] values, double minSimilarity) {
		MatrixRow mr = new MatrixRow(values);
		mr.writeRowToFile(bucketer.getRowPath(rowIndex), minSimilarity);
	}

	/**
	 * gets the corpus index
	 * 
	 * @param i
	 *            The row index.
	 * @return The original corpus index for that matrix index.
	 */
	public int getCorpusIndex(int i) {
		return keys[i];
	}

	/**
	 * gets rowIndices for all unbuilt rows
	 * 
	 * @return
	 */
	public int[] getUnbuiltRowIndices() {
		List<Integer> indices = new LinkedList<Integer>();
		for (Integer rowIndex = 0; rowIndex < size(); rowIndex++) {
			if (!(new File(bucketer.getRowPath(rowIndex)).exists())) {
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

	/**
	 * set an individual cell value
	 * 
	 * @param cellValue
	 * @throws IOException
	 */
	public void setCellValue(CellResult cellResult, double minSimilarity) throws IOException {
		int rowIndex = cellResult.getRowIndex();
		int colIndex = cellResult.getColIndex();
		MatrixRow mr = this.getRow(rowIndex);
		mr.setColValue(colIndex, cellResult.getValue());
		mr.writeRowToFile(bucketer.getRowPath(rowIndex), minSimilarity);
	}

	/**
	 * a regex input set maintains the contract that it will provide a set of
	 * inputs for a given regex, so on creation, the existence of these inputs
	 * on file is verified. They are not kept in memory, which could be
	 * intractable.
	 * 
	 * @param nInputs
	 *            The expected number of inputs to have per regex.
	 * @throws IOException
	 */
	private void validateInputSetSizes(int nInputs) throws IOException {

		for (int rowIndex = 0; rowIndex < size(); rowIndex++) {
			String[] inputSet = getInputSetStrings(rowIndex);

			HashSet<String> hashingStringSet = new HashSet<String>();
			for (int i = 0; i < inputSet.length; i++) {
				hashingStringSet.add(inputSet[i]);
			}
			if (hashingStringSet.size() < nInputs) {
				throw new RuntimeException("the number of input Strings provided: (" + hashingStringSet.size()
						+ ") for rowIndex: " + rowIndex + " is lower than the number requested (" + nInputs + ")");
			}
		}
	}
}
