package recreateArtifacts.similarityMatrix;

import java.util.List;

public class PostProcess {
	public static void sayBye() {
		Console.WriteLine("PreProcess says bye");
	}

	public static void createMatricesAndABC(String allRowsBase, Integer nRows, double minSimilarity, String filteredCorpusPath)
    {
        Dictionary<Integer, Integer> keyConverter = Util.getKeyConverter(getKeyList(filteredCorpusPath));
        WholeMatrix wholeMatrix = populateWholeMatrix(allRowsBase, nRows, minSimilarity);

        System.IO.File.WriteAllText(@"\\vmware-host\Shared Folders\Documents\SoftwareProjects\tour_de_source\analysis\analysis_output\wholeMatrix.txt", wholeMatrix.ToString());
        HalfMatrix halfMatrix = new HalfMatrix(wholeMatrix, minSimilarity);
        System.IO.File.WriteAllText(@"\\vmware-host\Shared Folders\Documents\SoftwareProjects\tour_de_source\analysis\analysis_output\halfMatrix.txt", halfMatrix.ToString());
        String abcContent = halfMatrix.getABC(minSimilarity, keyConverter);
        System.IO.File.WriteAllText(@"\\vmware-host\Shared Folders\Documents\SoftwareProjects\tour_de_source\analysis\analysis_output\behavioralSimilarityGraph.abc", abcContent);
    }

	private static WholeMatrix populateWholeMatrix(String allRowsBase, Integer nRows, double minSimilarity) {
		WholeMatrix wholeMatrix = new WholeMatrix(nRows, minSimilarity);
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