package recreateArtifacts.similarityMatrix.test;

import java.util.Random;
import java.util.TreeSet;

import recreateArtifacts.similarityMatrix.Main_MatrixBuilder;
import recreateArtifacts.similarityMatrix.RegexInputGroup;
import recreateArtifacts.similarityMatrix.RegexInputSubset;

public class Test_MatrixBuilder {

	private static final int N_TEST_REGEXES = 45;

	/**
	 * this test runs through the same code as the main, but with a subset of
	 * the regex input group. This allows some faster evaluation of the code,
	 * without having to go through the whole process on a large input to get to
	 * a later step.
	 */
	public static void main(String[] args) throws Exception {
		RegexInputGroup completeGroup = Main_MatrixBuilder.getCompleteGroup();
		Random gen = new Random(Integer.MAX_VALUE);
		TreeSet<Integer> randomRowIndices = new TreeSet<Integer>();
		while (randomRowIndices.size() < N_TEST_REGEXES) {
			Integer candidate = gen.nextInt(completeGroup.size());
			if (!randomRowIndices.contains(candidate)) {
				randomRowIndices.add(candidate);
			}
		}
		RegexInputSubset subset = new RegexInputSubset(completeGroup, randomRowIndices);
		Main_MatrixBuilder.buildMatrix(subset);
	}

}
