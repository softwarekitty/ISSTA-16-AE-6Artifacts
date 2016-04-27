package recreateArtifacts.similarityMatrix.threading;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import recreateArtifacts.similarityMatrix.RowUtil;

public class RegexRunner {
	private final static double INITIALIZED = 0.00000987654321;
	private final static double INCOMPLETE = 0.00000123456789;
	private final static double CANCELLED = 0.0000050101010101;
	private final static double VERIFIED_TIMEOUT = 0.00000701702703;
	private final static double BELOW_MIN = 0.00000307207107;

	static void validateCell(Integer j, double[] row, HashSet<String> matchingStrings_outer, Regex regex_inner,
			Integer maxErrors) {

		double nMatchingStrings = matchingStrings_outer.size();
		Integer alsoMatchingCounter = 0;
		Integer errorCounter = 0;

		for (String matchingString : matchingStrings_outer) {

			if (errorCounter > maxErrors) {
				row[j] = BELOW_MIN;
				return;
			}
			if (regex_inner.match(matchingString)) {
				alsoMatchingCounter++;
			} else {
				errorCounter++;
			}
		}
		double similarity = alsoMatchingCounter / nMatchingStrings;
		row[j] = similarity;
	}

	private static Integer[] getBatchOfIndices(String allRowsBase, Integer nKeys, Integer batchSize,
			boolean buildingRows) throws IOException {
		Integer nAdded = 0;
		List<Integer> indices = new LinkedList<Integer>();
		for (Integer rowIndex = 0; rowIndex < nKeys; rowIndex++) {

			boolean includeInBatch = buildingRows ? !RowUtil.rowExists(allRowsBase, nKeys, rowIndex)
					: RowUtil.hasUnverifiedTimeouts(allRowsBase, nKeys, rowIndex);
			if (includeInBatch) {
				indices.add(rowIndex);
				nAdded++;
			}
			if (nAdded >= batchSize) {
				return indices.toArray(new Integer[indices.size()]);
			}
		}
		return indices.toArray(new Integer[indices.size()]);
	}
}
