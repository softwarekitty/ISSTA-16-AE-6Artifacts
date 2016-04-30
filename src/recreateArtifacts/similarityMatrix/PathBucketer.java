package recreateArtifacts.similarityMatrix;

import java.io.File;

public class PathBucketer {

	private static final String ROW_PREFIX = "row_";
	private static final String ROW_SUFFIX = ".txt";
	private static final String INPUT_SET_PREFIX = "rex_";
	private static final String INPUT_SET_SUFFIX = ".txt";
	private static final String PATH_SEP = "/";

	private final int size;
	private final String inputSetBase;
	private final String rowBase;

	public PathBucketer(int size, String inputSetBase, String rowBase) {
		this.size = size;
		this.inputSetBase = inputSetBase;
		this.rowBase = rowBase;
		initializeBucketFoldersIfNeeded();
	}

	public String getRowPath(Integer rowIndex) {
		return rowBase + getBucketFolder(rowIndex) + PATH_SEP + ROW_PREFIX + rowIndex.toString() + ROW_SUFFIX;
	}

	public String getInputSetPath(Integer rowIndex) {
		return inputSetBase + getBucketFolder(rowIndex) + PATH_SEP + INPUT_SET_PREFIX + rowIndex.toString()
				+ INPUT_SET_SUFFIX;
	}

	private void initializeBucketFoldersIfNeeded() {
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
		return (int) Math.sqrt(size) + 1;
	}

	private String getBucketFolder(int rowIndex) {
		Integer nBuckets = getNBuckets();
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
}
