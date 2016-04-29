package recreateArtifacts.similarityMatrix;

public class CellResult {

	private final double resultValue;
	private final int rowIndex;
	private final int colIndex;

	public CellResult(double resultValue, int rowIndex, int colIndex) {
		this.resultValue = resultValue;
		this.rowIndex = rowIndex;
		this.colIndex = colIndex;
	}

	public double getValue() {
		return resultValue;
	}

	public int getColIndex() {
		return colIndex;
	}

	public int getRowIndex() {
		return rowIndex;
	}

}
