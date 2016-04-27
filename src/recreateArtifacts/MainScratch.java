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
		
		TreeSet<RegexProjectSet> corpusLoadedFromFile = LoadUtil
				.loadRegexProjectSetInput(IOUtil.readLines(PathUtil.pathToCorpusFile()));

		String wholeOrderingFileContents = IOUtil.readFileToString(PathUtil.pathToOriginalOrdering());
		TreeMap<String,Integer> loadedPatternIndexMap = getPatternIndexMap(wholeOrderingFileContents);
		
		
		
		TreeSet<RegexProjectSet> patternOrderedCorpus = new TreeSet<RegexProjectSet>(new PatternComparator());
		patternOrderedCorpus.addAll(corpusLoadedFromFile);
		

		TreeMap<String,Integer> loadedCorpusMap = new TreeMap<String,Integer>();
		int i = 0;
		for (RegexProjectSet rps : patternOrderedCorpus) {
			loadedCorpusMap.put(rps.getPattern(), i++);
		}
		boolean areEqual = loadedPatternIndexMap.equals(loadedCorpusMap);
		if(!areEqual){
			File f1 = new File(PathUtil.homePath,"fromEscRaw.tsv");
			File f2 = new File(PathUtil.homePath,"fromCorpus.tsv");
			IOUtil.createAndWrite(f1, getMapContent(loadedPatternIndexMap));
			IOUtil.createAndWrite(f2, getMapContent(loadedCorpusMap));
			System.out.println("maps are not equal.");	
		}else{
			System.out.println("maps are equal.");	
		}


		
		
//		System.out.println("old map:: " + loadedPatternIndexMap.size());
//		System.out.println("new map:: " + newPatternIndexMap.size());
//
//		Set<Entry<String, Integer>> olds = loadedPatternIndexMap.entrySet();
//		Set<Entry<String, Integer>> oldCopy = new HashSet<Entry<String, Integer>>();
//		for (Entry<String, Integer> oldEntry : olds) {
//			oldCopy.add(oldEntry);
//		}
//
//		Set<Entry<String, Integer>> news = newPatternIndexMap.entrySet();
//		for (Entry<String, Integer> oldEntry : olds) {
//			for (Entry<String, Integer> newEntry : news) {
//				if (newEntry.getKey().equals(oldEntry.getKey()) && newEntry.getValue().equals(oldEntry.getValue())) {
//					oldCopy.remove(oldEntry);
//				}
//			}
//		}
//		System.out.println("nDifferent entries: " + oldCopy.size());
	}

	private static String getMapContent(TreeMap<String,Integer> anyMap) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> entry : anyMap.entrySet()) {
			sb.append(entry.getValue()+"\t"+entry.getKey()+"\n");
		}
		return sb.toString();
	}

	private static HashMap<String, Integer> getPatternOrder(String connectionString)
			throws SQLException, ClassNotFoundException {
		List<String> originalOrderedPatterns = new LinkedList<String>();

		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(connectionString);
		c.setAutoCommit(false);
		stmt = c.createStatement();

		// original query, which used sql to group by pattern
		String query = "select pattern, count(distinct uniqueSourceID) as weight from RegexCitationMerged where (flags=0 or flags like 'arg%' or flags=128 or flags='re.DEBUG') and pattern!='arg1' group by pattern order by weight desc;";

		// these are all the distinct patterns with weight
		ResultSet rs = stmt.executeQuery(query);
		TreeSet<Integer> dummyProjectIDSet = new TreeSet<Integer>();
		dummyProjectIDSet.add(4);
		while (rs.next()) {
			String pattern = rs.getString("pattern");
			try {
				RegexProjectSet rps = new RegexProjectSet(pattern, dummyProjectIDSet);
				originalOrderedPatterns.add(pattern);
			} catch (Exception e) {
				// ignore these
			}
		}

		// wind down sql
		rs.close();
		stmt.close();
		c.close();
		HashMap<String, Integer> brandNewPatternIndexMap = new HashMap<String, Integer>();
		int i = 0;
		for (String pattern : originalOrderedPatterns) {
			brandNewPatternIndexMap.put(pattern, i++);
		}
		return brandNewPatternIndexMap;
	}

	public static TreeMap<String,Integer> getPatternIndexMap(String exportedCorpusRaw) throws IOException {
		TreeMap<String,Integer> patternIndexMap = new TreeMap<String,Integer>();
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
}

class PatternComparator implements Comparator<RegexProjectSet> {
	@Override
	public int compare(RegexProjectSet r1, RegexProjectSet r2) {
		return r1.getRawPattern().compareTo(r2.getRawPattern());
	}
}
