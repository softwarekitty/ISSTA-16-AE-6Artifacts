package recreateArtifacts.similarityMatrix;

import java.io.File;

import recreateArtifacts.PathUtil;
import recreateArtifacts.similarityMatrix.threading.RegexRunner;

public class Main_MatrixBuilder {

	public static void main(String[] args) throws Exception {
		System.out.println("hello from MatrixBuilder");
		String allRowsBase = PathUtil.allRowsBase();
		String rexStringsBase = PathUtil.rexStringsBase();
		File filteredCorpusFile = new File(PathUtil.pathToFilteredCorpus());
		if (!filteredCorpusFile.exists()) {
			throw new RuntimeException("missing required file: " + filteredCorpusFile);
		}
		RegexGroup group = new RegexGroup(filteredCorpusFile.getAbsolutePath());

		// TODO - thought this was 400, was this part of that one experiment?
		int nRexStringsToUse = 300;
		double minSimilarity = 0.75;

		// we have to do batches bc runaway regex matchings never release memory
		int batchSize = 256;
		int nRowsBefore = RowUtil.nRowsExist(allRowsBase, group.size());
		if (nRowsBefore == group.size()) {
			System.out.println("all row files are present! Counting unverified rows");
			int unverifiedTimeoutRows = RowUtil.countUnverifiedRows(allRowsBase, group.size());
			if (unverifiedTimeoutRows > 0) {
				System.out.println("why are there unverified rows?");
				System.exit(1);

//				int nRunnawaysWithoutStress = 1024;
//				System.out
//						.println(unverifiedTimeoutRows + " rows have unverified timeouts.  verifying a chunk of up to "
//								+ nRunnawaysWithoutStress + " cells with timeouts.");
//				TimeoutVerifier.verifyRows(allRowsBase, minSimilarity, group, nRunnawaysWithoutStress, batchSize,
//						rexStringsBase, nRexStringsToUse);
//				System.out.println("chunk of timeout verification complete");
			} else {
				System.out.println("all cells are valid - creating matrices and abc file");
				String abcOutputPath = PathUtil.getPathMatrix() + "output/similarityGraph.abc";
				PostProcess.createMatricesAndABC(abcOutputPath,minSimilarity, group, allRowsBase);
				System.out.println("matrix and abc files are written - exiting");
				return;
			}
		} else {

			System.out.println("batchSize: " + batchSize + " nRowsBefore: " + nRowsBefore + " nRows: " + group.size());
			RegexRunner.runBatchOfRows(batchSize, allRowsBase, group, rexStringsBase, minSimilarity,
					nRexStringsToUse);
			int nRowsAfter = RowUtil.nRowsExist(allRowsBase, group.size());
			System.out.println(
					"nRowsAfter: " + nRowsAfter + " diff: " + (nRowsAfter - nRowsBefore) + " batchSize:" + batchSize);
			return;
		}
	}
}
