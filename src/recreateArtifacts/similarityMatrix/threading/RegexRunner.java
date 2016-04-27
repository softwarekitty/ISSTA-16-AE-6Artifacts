package recreateArtifacts.similarityMatrix.threading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import recreateArtifacts.similarityMatrix.MatrixRow;
import recreateArtifacts.similarityMatrix.RegexGroup;
import recreateArtifacts.similarityMatrix.RowUtil;

public class RegexRunner {

	public static void runBatchOfRows(int batchSize, String allRowsBase, RegexGroup group, String rexStringsBase,
			double minSimilarity, int nMatchStrings) throws Exception {
		int rowTimeLimitMS = 8000;

		boolean buildingRows = true;
		Integer[] batchIndices = CellUtil.getBatchOfIndices(allRowsBase, group.size(), batchSize, buildingRows);

		// this needs to block anyway, so let it wait until things clear up
		ExecutorService lowPriorityExec = CellUtil.getCustomExecutorService(4, Thread.MIN_PRIORITY);
		List<RowTask> batchOfRowTasks = new ArrayList<RowTask>(batchSize);
		for (int i = 0; i < batchIndices.length; i++) {
			int rowIndex = batchIndices[i];
			String[] matchStrings = RowUtil.getRexGeneratedStrings(rowIndex, group.size(), rexStringsBase,
					nMatchStrings);
			batchOfRowTasks.set(i,
					new RowTask(rowIndex, matchStrings, group.getRegexMap(), rowTimeLimitMS, minSimilarity));
		}
		List<Future<RowResult>> resultFutures = lowPriorityExec.invokeAll(batchOfRowTasks);
		for (Future<RowResult> resultFuture : resultFutures) {
			RowResult result = resultFuture.get();
			int currentRowIndex = result.getRowIndex();

			MatrixRow mr = new MatrixRow(currentRowIndex, result.getRowArray(), group.size());
			mr.writeRowToFile(allRowsBase, minSimilarity);
			System.out.println(
					"completed i: " + currentRowIndex + "/" + group.size() + " nMatchStrings:" + nMatchStrings);
		}
	}
}
