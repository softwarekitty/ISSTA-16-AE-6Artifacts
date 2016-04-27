package recreateArtifacts.similarityMatrix.threading;

import java.util.concurrent.Callable;

import recreateArtifacts.similarityMatrix.RowUtil;

public class CellMeasuringTask implements Callable<CellResult> {
	private final int rowIndex;
	private final int colIndex;
	private final double minSim;
	private final Regex regex;
	private final String[] matchingStrings;
	private final int rowTimeLimitMS;

	public CellMeasuringTask(int rowIndex, int colIndex, double minSim, Regex regex, String[] matchingStrings, int rowTimeLimitMS) {
		this.rowIndex = rowIndex;
		this.colIndex = colIndex;
		this.minSim = minSim;
		this.regex = regex;
		this.matchingStrings = matchingStrings;
		this.rowTimeLimitMS = rowTimeLimitMS;
	}

	@Override
	public CellResult call() throws Exception {
		int maxErrors = RowUtil.getMaxErrors(minSim, matchingStrings.length);
		return null;
	}

}
