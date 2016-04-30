package recreateArtifacts.similarityMatrix.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scratch {
	private static Pattern lCurly = Pattern.compile("[\\[\\(\\{] | [\\]\\}\\),;:]");

	public static void main(String[] args) {
		System.out.println("pattern: " + lCurly.pattern());
		Matcher m = lCurly.matcher("\\{}");
		String newString = m.replaceAll("\\\\{");
		System.out.println("new string: " + newString);

	}

}
