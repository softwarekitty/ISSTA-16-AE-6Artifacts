package recreateArtifacts;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.core.RegexProjectSet;
import main.io.IOUtil;
import main.io.LoadUtil;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;

public class MainScratch {
	public static void main(String[] args) throws IllegalArgumentException, IOException, QuoteRuleException,
			PythonParsingException, ClassNotFoundException, SQLException {
		
		TreeSet<RegexProjectSet> corpus = LoadUtil
				.loadRegexProjectSetInput(IOUtil.readLines(PathUtil.pathToCorpusFile()));

		String wholeOrderingFileContents = IOUtil.readFileToString(PathUtil.pathToOriginalOrdering());
		HashMap<String,Integer> pimFile = getPatternIndexMap(wholeOrderingFileContents);
		HashMap<String, Integer> pimMem = getPatternIndexMapFromCorpus(corpus);
		
		// prints true
		System.out.println("equal: "+pimFile.equals(pimMem));
	}
	
	public static HashMap<Integer, RegexProjectSet> getLookup(TreeSet<RegexProjectSet> corpus,
			HashMap<String, Integer> patternIndexMap) {
		HashMap<Integer, RegexProjectSet> lookup = new HashMap<Integer, RegexProjectSet>();

		for (RegexProjectSet regex : corpus) {
			String originalPattern = regex.getPattern();
			Integer javaIndex = patternIndexMap.get(originalPattern);
			lookup.put(javaIndex, regex);
		}
		return lookup;
	}

	private static String getMapContent(TreeMap<String,Integer> anyMap) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> entry : anyMap.entrySet()) {
			sb.append(entry.getValue()+"\t"+entry.getKey()+"\n");
		}
		return sb.toString();
	}

	public static HashMap<String,Integer> getPatternIndexMap(String exportedCorpusRaw) throws IOException {
		HashMap<String,Integer> patternIndexMap = new HashMap<String,Integer>();
		Pattern finder = Pattern.compile("(\\d+)\\t(\\d+)\\t(.*)");
		Matcher pairMatcher = finder.matcher(exportedCorpusRaw);
		while (pairMatcher.find()) {
			String indexString = pairMatcher.group(1);
			String originalPattern = pairMatcher.group(3);
			Integer existsAlready = patternIndexMap.get(originalPattern);
			if (existsAlready != null) {
				System.out.println("duplicate original pattern: " + originalPattern);
				System.exit(1);
			}
			patternIndexMap.put(originalPattern, Integer.parseInt(indexString));
		}
		return patternIndexMap;
	}
	
	private static HashMap<String, Integer> getPatternIndexMapFromCorpus(TreeSet<RegexProjectSet> corpus) {
		/**
		 * define a comparator that orders the regexes by rawPattern, as this
		 * was the order used before exporting to Rex, and is needed to map the
		 * indices from clusters back to patterns
		 * 
		 * @author cc
		 *
		 */
		class PatternComparator implements Comparator<RegexProjectSet> {
			@Override
			public int compare(RegexProjectSet r1, RegexProjectSet r2) {
				return r1.getRawPattern().compareTo(r2.getRawPattern());
			}
		}
		TreeSet<RegexProjectSet> rawPatternOrderedCorpus = new TreeSet<RegexProjectSet>(new PatternComparator());
		rawPatternOrderedCorpus.addAll(corpus);
		HashMap<String, Integer> patternIndexMap = new HashMap<String, Integer>();
		int i = 0;
		for (RegexProjectSet rps : rawPatternOrderedCorpus) {
			patternIndexMap.put(rps.getPattern(), i++);
		}
		return patternIndexMap;
	}
}
