package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import main.io.IOUtil;
import recreateArtifacts.similarityMatrix.threading.CellMeasuringTask;
import recreateArtifacts.similarityMatrix.threading.CellResult;

public class RowUtil {

	// these seem like some common names - package visibility please
	public final static double INITIALIZED = 0.00000987654321;
	public final static double INCOMPLETE = 0.00000123456789;
	public final static double CANCELLED = 0.0000050101010101;
	public final static double VERIFIED_TIMEOUT = 0.00000701702703;
	public final static double BELOW_MIN = 0.00000307207107;

	public static int nRowsExist(String allRowsBase, int nRows) {

		// create all the bucket directories if this is the first time here
		List<String> bucketList = RowUtil.getBucketList(nRows);
		for (String bucketName : bucketList) {
			String rowBucketDirectory = allRowsBase + bucketName;
			File rowBucketFile = new File(rowBucketDirectory);
			if (!rowBucketFile.exists()) {
				rowBucketFile.mkdirs();
			}
		}

		// count the times a file exists for a row in its bucket
		int numRowsExist = 0;
		for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {

			String rowFilePath = RowUtil.getRowFilePath(allRowsBase, nRows, rowIndex);
			File rowFile = new File(rowFilePath);
			if (rowFile.exists()) {
				numRowsExist++;
			}
		}
		return numRowsExist;
	}

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
		File rowFile = new File(rowFilePath);
		if (!rowFile.exists()) {
			return false;
		} else {
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
	}

	public static String[] getRexGeneratedStrings(int rowIndex, int nKeys, String rexStringsBase, int nMatchStrings)
			throws Exception {
		String rexFilePath = getRexFilePath(rexStringsBase, nKeys, rowIndex);
		HashSet<String> generatedStrings = new HashSet<String>();
		String fullTestStringContent = IOUtil.readFileToString(rexFilePath);
		String[] testStrings = fullTestStringContent.split(getRexDelimiter());
		if (testStrings.length < nMatchStrings) {
			throw new Exception("the number of rex generated Strings available (" + testStrings.length
					+ ") is lower than the number requested (" + nMatchStrings + ")");
		}

		// eliminate duplicates - there shouldn't be any, but just to be sure
		generatedStrings.addAll(Arrays.asList(testStrings));
		if( generatedStrings.contains(null)){
			System.err.println("null string");
			System.exit(1);
		}
		if (generatedStrings.size() < testStrings.length) {
			throw new Exception("the number of rex generated Strings provided: (" + testStrings.length
					+ ") is lower than the number of unique Strings (" + generatedStrings.size()
					+ ") - some must be a duplicate for regex at row: " + rowIndex);
		}
		return testStrings;
	}

	public static boolean rowExists(String allRowsBase, Integer nKeys, Integer rowIndex) {
		return new File(getRowFilePath(allRowsBase, nKeys, rowIndex)).exists();
	}

	public static Future<CellResult> executeTask(CellMeasuringTask task, int timeoutMS, ExecutorService service,
			ScheduledExecutorService canceller) {
		final Future<CellResult> future = service.submit(task);
		canceller.schedule(new Callable<Void>() {
			public Void call() {
				future.cancel(true);
				return null;
			}
		}, timeoutMS, TimeUnit.MILLISECONDS);
		return future;
	}
}
