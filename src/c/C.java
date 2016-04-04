package c;

public class C {

	// change this string to point to your project root (assumes Unix-style slashing)
	public static final String artifactPath = "/Users/carlchapman/git/ISSTA-16-AE-6Artifacts/";

	public static final String outputPath = artifactPath + "output/";
	public static final String connectionString = "jdbc:sqlite:" +
		artifactPath + "input/merged_report.db";
}
