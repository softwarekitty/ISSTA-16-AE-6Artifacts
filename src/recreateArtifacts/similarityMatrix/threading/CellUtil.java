package recreateArtifacts.similarityMatrix.threading;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import recreateArtifacts.similarityMatrix.RowUtil;

public class CellUtil {
	
	// these seem like some common names - package visibility please
	final static double INITIALIZED = 0.00000987654321;
	final static double INCOMPLETE = 0.00000123456789;
	final static double CANCELLED = 0.0000050101010101;
	final static double VERIFIED_TIMEOUT = 0.00000701702703;
	final static double BELOW_MIN = 0.00000307207107;
	
	public static Integer[] getBatchOfIndices(String allRowsBase, Integer nKeys, Integer batchSize,
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
	
	public static ExecutorService getCustomExecutorService(int nThreads, int priority) {
		ExecutorService eService = Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(priority);
				t.setDaemon(true);
				return t;
			}
		});
		return eService;
	}

}
