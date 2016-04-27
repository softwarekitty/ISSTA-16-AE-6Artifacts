package recreateArtifacts.similarityMatrix;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import main.io.IOUtil;

public class RowUtil {

	public static List<String> getBucketList(int nKeys) {
		List<String> bucketNames = new LinkedList<String>();

		// number of buckets is close to number of files in buckets
		// this is just for ease of use when file exploring
		Integer nBuckets = (int) Math.sqrt(nKeys) + 1;
		int nChars = nBuckets.toString().length();
		for (int i = 0; i <= nBuckets; i++) {
			StringBuilder bucketString = new StringBuilder();
			String intString = new Integer(i).toString();
			bucketString.append(intString);
			int nZeroPads = nChars - intString.length();
			for (int padIndex = 0; padIndex < nZeroPads; padIndex++) {
				bucketString.insert(0, "0");
			}
			bucketNames.add(bucketString.toString());
		}
		return bucketNames;
	}

	public static String getRexDelimiter() {
		return "\n2jxj8oSFLPEfrP4q8yVn6h0vf\n";
	}

	public static String decideBucket(int rowNumber, Integer nBuckets) {
		Integer bucketID = rowNumber / nBuckets;
		int nChars = nBuckets.toString().length();
		StringBuilder bucketString = new StringBuilder();
		bucketString.append(bucketID.toString());
		int nZeroPads = nChars - bucketString.length();
		for (int padIndex = 0; padIndex < nZeroPads; padIndex++) {
			bucketString.insert(0, "0");
		}
		return bucketString.toString();
	}

	// here so that we get the same name when calling from different places
	public static String getRowFilePath(String allRowsBase, int nKeys, Integer rowIndex) {
		int nBuckets = (int) Math.sqrt(nKeys) + 1;
		String bucketName = decideBucket(rowIndex, nBuckets);
		String rowFilePath = allRowsBase + bucketName + "/row_" + rowIndex.toString() + ".txt";
		return rowFilePath;
	}

	public static HashMap<Integer, Integer> getKeyConverter(List<Integer> keyList) {
		HashMap<Integer, Integer> keyConverter = new HashMap<Integer, Integer>();
		for (int i = 0; i < keyList.size(); i++) {
			keyConverter.put(i, keyList.get(i));
		}
		return keyConverter;
	}

	public static String getRexFilePath(String rexStringsBase, int nKeys, Integer rowIndex) {
		int nBuckets = (int) Math.sqrt(nKeys) + 1;
		String bucketName = decideBucket(rowIndex, nBuckets);
		String rowFilePath = rexStringsBase + bucketName + "/rex_" + rowIndex.toString() + ".txt";
		return rowFilePath;
	}

	public static String setToString(HashSet<String> matchingStrings) {
		StringBuilder sb = new StringBuilder();
		for (String ms : matchingStrings) {
			sb.append(ms + "\n");
		}
		return sb.toString();
	}

	public static int countUnverifiedRows(String allRowsBase, int nRows) throws IOException {
		int nUnverifiedRows = 0;
		for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
			if (hasUnverifiedTimeouts(allRowsBase, nRows, rowIndex)) {
				nUnverifiedRows++;
			}
		}
		return nUnverifiedRows;
	}

	public static boolean hasUnverifiedTimeouts(String allRowsBase, int nRows, int rowIndex) throws IOException {
		String rowFilePath = getRowFilePath(allRowsBase, nRows, rowIndex);

		List<String> lines = IOUtil.readLines(rowFilePath);
		String line1 = lines.get(0);
		String line2 = lines.get(1);
		String line3 = lines.get(2);
		if (line1 == null || line2 == null || line3 == null) {
			System.err.println("missing line in path: " + rowFilePath);
			System.exit(1);
		} else if (!(line1.equals("initializedList: []") && line2.equals("incompleteList: []")
				&& line3.equals("cancelledList: []"))) {
			return true;
		}
		return false;
	}

	public static HashSet<String> getRexGeneratedStrings(int rowIndex, int nKeys, String rexStringsBase,
			int nMatchStrings) throws Exception {
		String rexFilePath = getRexFilePath(rexStringsBase, nKeys, rowIndex);
		HashSet<String> generatedStrings = new HashSet<String>();
		String fullTestStringContent = IOUtil.readFileToString(rexFilePath);
		String[] testStrings = fullTestStringContent.split(getRexDelimiter());
		if (testStrings.length < nMatchStrings) {
			throw new Exception("the number of rex generated Strings available (" + testStrings.length
					+ ") is lower than the number requested (" + nMatchStrings + ")");
		}
		generatedStrings.addAll(Arrays.asList(testStrings));
		return generatedStrings;
	}

	public static Integer getMaxErrors(double minSimilarity, Integer nMatchingStrings) {
		/**
		 * this is a simple calculation, but an important number, if we want to
		 * skip doing n calculations and claim that it is because that row would
		 * have turned out to be below the minimum similarity anyway.
		 */
		double partMaxError = 1 - minSimilarity;
		double maxErrorsDouble = nMatchingStrings * partMaxError;
		
		// this may round down, so add one back just to be sure
		int nMaxErrors = (int)maxErrorsDouble + 1;
		return nMaxErrors;
	}
	
	public static List<Integer> getKeyList(String filteredCorpusPath)
    {
        List<Integer> keyList = new List<Integer>();
        Regex numberFinder = new Regex(@"(\d+)\t(.*)");
        using (StreamReader r = new StreamReader(filteredCorpusPath))
        {
            String line = null;
            while ((line = r.ReadLine()) != null)
            {
                Match lineMatch = numberFinder.Match(line);
                if (lineMatch.Success)
                {
                    Integer index = Integer.Parse(lineMatch.Groups[1].Value);
                    keyList.Add(index);
                }
            }
        }
        keyList.Sort();
        return keyList;
    }
}
