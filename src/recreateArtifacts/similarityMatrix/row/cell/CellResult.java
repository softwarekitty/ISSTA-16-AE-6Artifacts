package recreateArtifacts.similarityMatrix.row.cell;

public class CellResult {

	private final double resultValue;
	private final int colIndex;
	private final int rowIndex;

	public CellResult(double resultValue, int colIndex, int rowIndex) {
		this.resultValue = resultValue;
		this.colIndex = colIndex;
		this.rowIndex = rowIndex;
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
