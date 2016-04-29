package recreateArtifacts.similarityMatrix.test;

import java.util.LinkedList;
import java.util.Random;

import recreateArtifacts.similarityMatrix.Main_MatrixBuilder;
import recreateArtifacts.similarityMatrix.RegexInputGroup;
import recreateArtifacts.similarityMatrix.RegexInputSubset;

public class Test_MatrixBuilder {
	
	private static final int N_TEST_REGEXES = 45;
	
	public static void main(String[] args) throws Exception{
		RegexInputGroup completeGroup = Main_MatrixBuilder.getCompleteGroup();
		Random gen = new Random(Integer.MAX_VALUE);
		LinkedList<Integer> randomRowIndices = new LinkedList<Integer>();
		while(randomRowIndices.size()<N_TEST_REGEXES){
			Integer candidate = gen.nextInt(completeGroup.size());
			if(!randomRowIndices.contains(candidate)){
				randomRowIndices.add(candidate);
			}
		}
		RegexInputSubset subset = new RegexInputSubset(completeGroup, randomRowIndices);
		Main_MatrixBuilder.buildVerifyExport(subset);
	}

}
