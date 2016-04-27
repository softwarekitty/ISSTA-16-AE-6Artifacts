package recreateArtifacts.similarityMatrix;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.io.IOUtil;

public class TimeoutVerifier {
	public static void verifyRows(String allRowsBase,double minSimilarity, RegexGroup group,
			Integer nRunnawaysWithoutStress, Integer batchSize, String rexStringsBase, Integer nMatchingStrings) throws IOException {
//		domain.SetData("REGEX_DEFAULT_MATCH_TIMEOUT", TimeSpan.FromMilliseconds(1200));

		Integer[] stressCounter = { 0 };

		Integer[] indicesWithTimeouts = getBatchOfIndicesWithTimeouts(allRowsBase, group.size(), batchSize);
		for (Integer indexKey = 0; indexKey < indicesWithTimeouts.length; indexKey++) {
			if (stressCounter[0] >= nRunnawaysWithoutStress) {
				break;
			}
			validateRow(indicesWithTimeouts[indexKey], group, minSimilarity, allRowsBase,
					rexStringsBase, stressCounter, nMatchingStrings);
		}
	}

	private static void validateRow(Integer rowIndex, RegexGroup group, double minSimilarity, String allRowsBase, String rexStringsBase, Integer[] stressCounter, Integer nMatchingStrings){

        MatrixRow mr = new MatrixRow(allRowsBase, rowIndex, group.size());
        HashSet<String> matchingStrings_outer = RowUtil.getRexGeneratedStrings(rowIndex, group.size(), rexStringsBase, nMatchingStrings);
        Integer maxErrors = RowUtil.getMaxErrors(minSimilarity,nMatchingStrings);
        double[] values = mr.getValues();
        Integer nTimeouts = 0;



        for (Integer j = 0; j < values.length; j++){
            //remember this keyList was built from the filteredCorpus,
            //with keys added in order
            Integer innerKey = group.getKeyList().get(j);
            Regex regex_inner = group.getRegexMap().get(innerKey);
            double similarity = values[j];

            // note that when reading the row from file, everything that
            // was not a valid similarity or below the minimum was
            // initialized to verifiedTimeout, which makes sense in an 
            // optimistic expectation that this step will be completed soon
            // after that, or has already been done.

            // so this will mean we only do a small fraction of most rows
            if (similarity == SimilarityMatrixBuilder.verifiedTimeoutFlag){
                try{
                    validateCell(j, values, matchingStrings_outer, regex_inner, maxErrors);
                }
                catch (RegexMatchTimeoutException e){
                    nTimeouts++;
                    stressCounter[0]++;
                }
            }
        }
        mr.writeRowToFile(allRowsBase,minSimilarity);
        System.out.println("verified i: " + rowIndex + "/" + group.size() + " nTimeouts: " + nTimeouts + " nMatchStrings:" + matchingStrings_outer.size());
    }

	static void validateCell(Integer j, double[] row, HashSet<String> matchingStrings_outer, Regex regex_inner,
			Integer maxErrors) {
//		AppDomain domain = AppDomain.CurrentDomain;
//		domain.SetData("REGEX_DEFAULT_MATCH_TIMEOUT", TimeSpan.FromMilliseconds(1200));

		double nMatchingStrings = matchingStrings_outer.size();
		Integer alsoMatchingCounter = 0;
		Integer errorCounter = 0;

		for (String matchingString : matchingStrings_outer) {

			if (errorCounter > maxErrors) {
				row[j] = SimilarityMatrixBuilder.belowMinFlag;
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

	private static Integer[] getBatchOfIndicesWithTimeouts(String allRowsBase, Integer nKeys, Integer batchSize) throws IOException {
		Integer nAdded = 0;
		List<Integer> indices = new LinkedList<Integer>();
		for (Integer rowIndex = 0; rowIndex < nKeys; rowIndex++) {
			if (RowUtil.hasUnverifiedTimeouts(allRowsBase, nKeys, rowIndex)) {
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
