package recreateArtifacts.similarityMatrix.row.cell;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import recreateArtifacts.similarityMatrix.BatchController;
import recreateArtifacts.similarityMatrix.Regex;

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

	@Override
	public CellResult call() throws Exception {
		double resultValue = BatchController.INCOMPLETE;
		double alsoMatchingCounter = 0;
		int notMatchingCounter = 0;
		try {

			int maxNonMatches = getMaxNonMatches(minSim, matchStrings.length);
			for (int i = 0; i < matchStrings.length; i++) {
				if (regex.match(matchStrings[i])) {
					alsoMatchingCounter++;
				} else {
					notMatchingCounter++;
				}
				if (notMatchingCounter > maxNonMatches) {
					return new CellResult(BatchController.BELOW_MIN, colIndex, rowIndex);
				}
			}
		} catch (CancellationException e) {
			System.out.println("timeout for col: " + colIndex + " row: " + rowIndex);
			return new CellResult(BatchController.CANCELLED, colIndex, rowIndex);
		} catch (Exception e) {
			System.err.println("unexpected exception in cell - row: " + rowIndex + " col: " + colIndex
					+ " exception type: " + e.toString());
			e.printStackTrace();
			return new CellResult(resultValue, colIndex, rowIndex);
		}
		resultValue = alsoMatchingCounter / matchStrings.length;
		return new CellResult(resultValue, colIndex, rowIndex);
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
