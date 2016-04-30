package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.io.IOUtil;

public class MatrixRow {
	private double[] values;

	public MatrixRow(double[] values) {
		this.values = values;
	}

	// should only be called from within the group
	public MatrixRow(String rowFilePath, int nCols) throws IOException {
		this.values = computeValuesFromFileContents(rowFilePath, nCols);
	}

	public double[] getValues() {
		return values;
	}

	private double[] computeValuesFromFileContents(String rowFilePath, int nCols) throws IOException {
		double[] valuesFromFile = new double[nCols];
		for (int i = 0; i < nCols; i++) {
			valuesFromFile[i] = BatchController.CELL_VALUE_ERROR;
		}
		List<String> lines = IOUtil.readLines(rowFilePath);
		for (String line : lines) {
			if (line.startsWith("initializedList")) {
				setPopulateValues(line, BatchController.INITIALIZED, valuesFromFile);
			} else if (line.startsWith("incompleteList:")) {
				setPopulateValues(line, BatchController.INCOMPLETE, valuesFromFile);
			} else if (line.startsWith("cancelledList:")) {
				setPopulateValues(line, BatchController.CANCELLED, valuesFromFile);
			} else if (line.startsWith("belowMinimumList:")) {
				setPopulateValues(line, BatchController.BELOW_MIN, valuesFromFile);
			} else if (line.startsWith("similarityValues:")) {
				setSimilarityValues(line, valuesFromFile);
			}
		}
		for (int i = 0; i < nCols; i++) {
			if (valuesFromFile[i] == BatchController.CELL_VALUE_ERROR) {
				throw new RuntimeException(
						"when loading row from file: " + rowFilePath + " missing data for index: " + i);
			}
		}
		return valuesFromFile;
	}

	private void setPopulateValues(String line, double flagValue, double[] vals) {
		String[] indices = removeBrackets(line).split(",");

		// some of these are empty
		for (String indexString : indices) {
			if (indexString.length() > 0) {
				int colIndex = Integer.parseInt(indexString);
				vals[colIndex] = flagValue;
			}
		}
	}

	private void setSimilarityValues(String line, double[] vals) {
		String similarityValueList = removeBrackets(line);
		Pattern parens = Pattern.compile("(\\((.*?)\\))");
		Matcher m = parens.matcher(similarityValueList);
		while (m.find()) {
			String pair = m.group(2);
			String[] splitPair = pair.split(":");
			int colIndex = Integer.parseInt(splitPair[0]);
			double colValue = Double.parseDouble(splitPair[1]);
			vals[colIndex] = colValue;
		}
	}

	private String removeBrackets(String line) {
		int startIndex = line.indexOf("[");
		int endIndex = line.indexOf("]");
		return line.substring(startIndex + 1, endIndex);
	}

	public void writeRowToFile(String rowFilePath, double minSimilarity) {
		DecimalFormat df5 = new DecimalFormat("0.0000");

		StringBuilder initializedList = new StringBuilder();
		StringBuilder incompleteList = new StringBuilder();
		StringBuilder cancelledList = new StringBuilder();
		StringBuilder belowMinimumList = new StringBuilder();
		StringBuilder similarityValues = new StringBuilder();

		initializedList.append("initializedList: [");
		incompleteList.append("incompleteList: [");
		cancelledList.append("cancelledList: [");
		belowMinimumList.append("belowMinimumList: [");
		similarityValues.append("similarityValues: [");

		for (int j = 0; j < values.length; j++) {
			double value_j = values[j];
			if (value_j == BatchController.INITIALIZED) {
				initializedList.append(j);
				initializedList.append(",");
			} else if (value_j == BatchController.INCOMPLETE) {
				incompleteList.append(j);
				incompleteList.append(",");
			} else if (value_j == BatchController.CANCELLED) {
				cancelledList.append(j);
				cancelledList.append(",");

				// notice that unchecked flags are consumed here:
				// verified timeout is treated as below min
			} else if (value_j < minSimilarity) {
				belowMinimumList.append(j);
				belowMinimumList.append(",");
			} else {
				similarityValues.append("(");
				similarityValues.append(j);
				similarityValues.append(":");
				similarityValues.append(df5.format(value_j));
				similarityValues.append(")");
				similarityValues.append(",");
			}
		}

		StringBuilder rowFileContent = new StringBuilder();
		rowFileContent.append(closedList(initializedList));
		rowFileContent.append(closedList(incompleteList));
		rowFileContent.append(closedList(cancelledList));
		rowFileContent.append(closedList(belowMinimumList));
		rowFileContent.append(closedList(similarityValues));

		IOUtil.createAndWrite(new File(rowFilePath), rowFileContent.toString());
	}

	private String closedList(StringBuilder listBuilder) {
		char lastChar = listBuilder.charAt(listBuilder.length() - 1);
		if (lastChar == ',') {
			return listBuilder.substring(0, listBuilder.length() - 1) + "]\n";
		} else {
			return listBuilder.toString() + "]\n";
		}
	}

	public List<CellResult> getInvalidResults(int rowIndex) {
		List<CellResult> invalidCells = new LinkedList<CellResult>();
		for (int colIndex = 0; colIndex < values.length; colIndex++) {
			double cellValue = values[colIndex];
			if (cellValue == BatchController.INITIALIZED || cellValue == BatchController.INCOMPLETE
					|| cellValue == BatchController.CANCELLED) {
				invalidCells.add(new CellResult(cellValue, rowIndex, colIndex));
			}
		}
		return invalidCells;
	}

	public void setColValue(int colIndex, double value) {
		values[colIndex] = value;
	}
}