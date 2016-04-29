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

public class RegexInputGroup {
	private static final String ROW_PREFIX = "row_";
	private static final String ROW_SUFFIX = ".txt";
	private static final String INPUT_SET_PREFIX = "rex_";
	private static final String INPUT_SET_SUFFIX = ".txt";
	private static final String PATH_SEP = "/";

	private final TreeMap<Integer, Regex> regexMap;
	private final int[] keys;
	private final String inputSetBase;
	private final String rowBase;
	private final String inputDelimiter;

	public RegexInputGroup(String filteredCorpusPath, String inputSetPath, String rowBase, int nMatchStrings,
			String inputDelimiter) throws Exception {
		this.inputSetBase = inputSetPath;
		this.rowBase = rowBase;
		this.inputDelimiter = inputDelimiter;

		// map from original corpus index to regex
		regexMap = new TreeMap<Integer, Regex>();

		Pattern numberFinder = Pattern.compile("(\\d+)\\t(.*)");
		List<String> lines = IOUtil.readLines(filteredCorpusPath);
		for (String line : lines) {
			Matcher lineMatcher = numberFinder.matcher(line);
			if (lineMatcher.find()) {
				int index = Integer.parseInt(lineMatcher.group(1));
				String pattern = lineMatcher.group(2);

				/**
				 * the keys for this map are the index of the regex in the
				 * corpus. Since many regexes may be excluded from this
				 * analysis, these keys are necessarily going to be different
				 * from the row indices of the matrix, but we will want to go
				 * between them.
				 * 
				 * Here we use a map where keys are kept in sorted order
				 * (TreeMap).
				 * 
				 * This allows the index of the matrix row to easily translate
				 * to the index in the original corpus, using the keyset of the
				 * map.
				 * 
				 */
				regexMap.put(index, new Regex(pattern));
			}
		}
		keys = new int[regexMap.size()];
		Iterator<Integer> it = regexMap.keySet().iterator();
		for (int i = 0; i < keys.length; i++) {
			keys[i] = it.next();
		}
		validateInputSetSizes(nMatchStrings);
		initializeBucketsIfNeeded();
	}

	public Regex getRegex(int colIndex) {
		return regexMap.get(keys[colIndex]);
	}

	public int size() {
		return keys.length;
	}

	public String[] getInputSetStrings(int rowIndex) throws IOException {
		String inputSetFileContent = IOUtil.readFileToString(getInputSetPath(rowIndex));
		return inputSetFileContent.split(inputDelimiter);
	}

	public String getRowPath(Integer rowIndex) {
		return rowBase + getBucketFolder(rowIndex) + PATH_SEP + ROW_PREFIX + rowIndex.toString() + ROW_SUFFIX;
	}

	private String getInputSetPath(Integer rowIndex) {
		return inputSetBase + getBucketFolder(rowIndex) + PATH_SEP + INPUT_SET_PREFIX + rowIndex.toString()
				+ INPUT_SET_SUFFIX;
	}

	public int nRowsExist() {
		// count the times a file exists for a row in its bucket
		int numRowsExist = 0;
		for (int rowIndex = 0; rowIndex < size(); rowIndex++) {
			if (rowFileExists(rowIndex)) {
				numRowsExist++;
			}
		}
		return numRowsExist;
	}

	public Integer[] getBatchOfIndicesForBuildingRows(int batchSize) throws IOException {
		return getIndices(batchSize, true);
	}

	public Integer[] getBatchOfIndicesForVerifyingRows(int batchSize) throws IOException {
		return getIndices(batchSize, true);
	}

	private boolean rowFileExists(int rowIndex) {
		File rowFile = new File(getRowPath(rowIndex));
		return rowFile.exists();
	}

	private void initializeBucketsIfNeeded() {
		int nBuckets = getNBuckets();
		for (int bucketNumber = 0; bucketNumber < nBuckets; bucketNumber++) {
			String bucketName = getBucketName(bucketNumber, nBuckets);
			File rowBucketFile = new File(rowBase + bucketName);
			if (!rowBucketFile.exists()) {
				rowBucketFile.mkdirs();
			}
		}
	}

	private int getNBuckets() {
		return (int) Math.sqrt(size()) + 1;
	}

	private String getBucketFolder(int rowIndex) {
		Integer nBuckets = (int) Math.sqrt(size()) + 1;
		Integer bucketID = rowIndex / nBuckets;
		return getBucketName(bucketID, nBuckets);
	}

	private String getBucketName(Integer bucketID, Integer nBuckets) {
		String lastBucketString = nBuckets.toString();
		String bucketIDString = bucketID.toString();

		int nZeroPads = lastBucketString.length() - bucketIDString.length();
		for (int padIndex = 0; padIndex < nZeroPads; padIndex++) {
			bucketIDString = "0" + bucketIDString;
		}
		return bucketIDString;
	}

	private void validateInputSetSizes(int nMatchStrings) throws IOException {

		for (int rowIndex = 0; rowIndex < size(); rowIndex++) {
			String[] inputSet = getInputSetStrings(rowIndex);

			HashSet<String> hashingStringSet = new HashSet<String>();
			for (int i = 0; i < inputSet.length; i++) {
				hashingStringSet.add(inputSet[i]);
			}
			if (hashingStringSet.size() < nMatchStrings) {
				throw new RuntimeException(
						"the number of rex generated Strings provided: (" + hashingStringSet.size() + ") for rowIndex: "
								+ rowIndex + " is lower than the number requested (" + nMatchStrings + ")");
			}
		}

	}

	private Integer[] getIndices(Integer batchSize, boolean buildingRows) throws IOException {
		List<Integer> indices = new LinkedList<Integer>();
		for (Integer rowIndex = 0; rowIndex < size(); rowIndex++) {
			boolean includeInBatch = buildingRows ? !rowFileExists(rowIndex) : hasUnverifiedTimeouts(rowIndex);
			if (includeInBatch) {
				indices.add(rowIndex);
			}
			if (indices.size() >= batchSize) {
				return indices.toArray(new Integer[indices.size()]);
			}
		}
		return indices.toArray(new Integer[indices.size()]);
	}

	private boolean hasUnverifiedTimeouts(int rowIndex) throws IOException {
		String rowFilePath = getRowPath(rowIndex);
		File rowFile = new File(rowFilePath);
		if (!rowFile.exists()) {
			return false;
		} else {
			List<String> lines = IOUtil.readLines(rowFilePath);
			String line1 = lines.get(0);
			String line2 = lines.get(1);
			String line3 = lines.get(2);
			if (line1 == null || line2 == null || line3 == null) {
				throw new RuntimeException("when checking for timeouts, missing line in path: " + rowFilePath);
			} else if (!(line1.equals("initializedList: []") && line2.equals("incompleteList: []")
					&& line3.equals("cancelledList: []"))) {
				return true;
			}
			return false;
		}
	}
}
