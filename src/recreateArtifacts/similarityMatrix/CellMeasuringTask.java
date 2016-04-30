package recreateArtifacts.similarityMatrix;

import java.util.concurrent.Callable;

public class CellMeasuringTask implements Callable<CellResult> {
	private final int rowIndex;
	private final int colIndex;
	private final double minSim;
	private final Regex regex;
	private final String[] matchStrings;

	public CellMeasuringTask(int rowIndex, int colIndex, double minSim, Regex regex, String[] matchStrings) {
		this.rowIndex = rowIndex;
		this.colIndex = colIndex;
		this.minSim = minSim;
		this.regex = regex;
		this.matchStrings = matchStrings;
	}

	public Integer getColIndex() {
		return colIndex;
	}

	// the promise here is that even though a regex may hang,
	// we don't get a non-flag value unless all the evaluations go through
	@Override
	public CellResult call() throws Exception {
		double resultValue = BatchController.INCOMPLETE;
		double alsoMatchingCounter = 0;
		int notMatchingCounter = 0;

		int maxNonMatches = getMaxNonMatches(minSim, matchStrings.length);
		for (int i = 0; i < matchStrings.length; i++) {
			try {
				// hangs here, may be interrupted
				if (regex.match(matchStrings[i])) {
					alsoMatchingCounter++;
				} else {
					notMatchingCounter++;
				}
				if (notMatchingCounter > maxNonMatches) {
					return new CellResult(BatchController.BELOW_MIN, rowIndex, colIndex);
				}

				// if it hangs, it should be interrupted eventually
			} catch (RuntimeException re) {
				Throwable cause = re.getCause();
				if (cause != null && cause instanceof InterruptedException) {
					System.err.println("interruption for col: " + colIndex + " row: " + rowIndex);
					return new CellResult(BatchController.CANCELLED, rowIndex, colIndex);
				} else {

					// if something else happens, consider this incomplete
					System.err.println("unexpected exception in cell - row: " + rowIndex + " col: " + colIndex
							+ " exception type: " + re.toString());
					re.printStackTrace();
					return new CellResult(resultValue, rowIndex, colIndex);
				}
			}
		}
		resultValue = alsoMatchingCounter / matchStrings.length;
		return new CellResult(resultValue, rowIndex, colIndex);
	}

	private int getMaxNonMatches(double minSimilarity, int nMatchingStrings) {
		/**
		 * this is a simple calculation, but an important number, if we want to
		 * skip doing n calculations and claim that it is because that row would
		 * have turned out to be below the minimum similarity anyway.
		 */
		double partMaxNonMatch = 1 - minSimilarity;
		double maxNonMatchDouble = nMatchingStrings * partMaxNonMatch;

		// this may round down, so add one back just to be sure
		int nMaxNonMatch = (int) maxNonMatchDouble + 1;
		return nMaxNonMatch;
	}
}

// catch (CancellationException e) {
// System.out.println("cancelation for col: " + colIndex + " row: " + rowIndex);
// return new CellResult(BatchController.CANCELLED, rowIndex, colIndex);
//
// // if it hangs, it should be cancelled eventually
// }
