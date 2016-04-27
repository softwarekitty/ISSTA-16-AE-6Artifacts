package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import main.io.IOUtil;

public class PostProcess {
	public static void sayBye() {
		System.out.println("PreProcess says bye");
	}

	public static void createMatricesAndABC(String abcOutputPath, double minSimilarity, RegexGroup group, String allRowsBase) throws IOException
    {
        HashMap<Integer, Integer> keyConverter = RowUtil.getKeyConverter(group.getKeyList());
        
        WholeMatrix wholeMatrix = populateWholeMatrix(allRowsBase, group.size(), minSimilarity);
        HalfMatrix halfMatrix = new HalfMatrix(wholeMatrix);
        String abcContent = halfMatrix.getABC(minSimilarity, keyConverter);
        IOUtil.createAndWrite(new File(abcOutputPath), abcContent);
    }

	private static WholeMatrix populateWholeMatrix(String allRowsBase, Integer nRows, double minSimilarity) throws IOException {
		WholeMatrix wholeMatrix = new WholeMatrix(nRows);
		for (Integer rowIndex = 0; rowIndex < nRows; rowIndex++) {
			MatrixRow mr = new MatrixRow(allRowsBase, rowIndex, nRows);
			double[] rowValues = mr.getValues();
			for (Integer j = 0; j < nRows; j++) {
				wholeMatrix.set(rowIndex, j, rowValues[j]);
			}

		}
		return wholeMatrix;
	}
}