package recreateArtifacts;

import java.io.File;

public class PathUtil {
	
	public static final String homePath = System.getProperty("user.dir");
	
	public static String getConnectionString() {
		String absPathToDb = new File(getDBPath()).getPath();
		return "jdbc:sqlite:" + absPathToDb;
	}
	
	public static String getDBPath(){
		return getArtifactPath() + "merged_report.db";
	}
	
	public static String getPathCluster() {
		return homePath + getRelPath()+"clusters/";
	}

	public static String getPathCorpus() {
		return homePath + getRelPath()+"corpus/";
	}
	
	public static String getPathFeature() {
		return homePath + getRelPath()+"featureTable/";
	}
	
	public static String getPathMatrix() {
		return homePath + getRelPath()+"similarityMatrix/";
	}
	
	public static String getPathSource() {
		return homePath + getRelPath()+"sourceInfo/";
	}
	
	private static String getRelPath(){
		return "/src/recreateArtifacts/";
	}
	
	private static String getArtifactPath(){
		return homePath + "/artifacts/";
	}

	public static String pathToCorpusFile() {
		return getArtifactPath() + "fullCorpus.tsv";
	}

	public static String pathToSimilarityGraph() {
		return getArtifactPath() + "similarityGraph.abc";
	}

	public static String pathToFilteredCorpus() {
		return getArtifactPath() + "filteredCorpus.tsv";
	}

	public static String pathToOriginalOrdering() {
		return getArtifactPath() + "originalCorpusOrdering.tsv";
	}

	public static String rexStringsBase() {
		return getArtifactPath()+"rexStrings/";
	}

	public static String allRowsBase() {
		return pathToSimilarityGraph()+"output/allRows/";
	}

}
