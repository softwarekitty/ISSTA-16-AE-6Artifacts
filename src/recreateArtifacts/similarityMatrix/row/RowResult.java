package recreateArtifacts.similarityMatrix.row;

public class RowResult {

	private final double[] rowArray;
	private final int rowIndex;

	public RowResult(double[] rowArray, int rowIndex) {
		super();
		this.rowArray = rowArray;
		this.rowIndex = rowIndex;
	}

	public double[] getRowArray() {
		return rowArray;
	}

	public int getRowIndex() {
		return rowIndex;
	}

}
