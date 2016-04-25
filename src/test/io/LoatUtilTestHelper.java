package tmp_testing_fixtures;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.RegexProjectSet;
import core.features.AlienFeatureException;
import io.IOUtil;
import parse.PythonParsingException;
import parse.QuoteRuleException;
import step5.BehavioralCategories;


// use these to test loading and for step 5
public class CorpusUtil {

	public static final String connectionString = "jdbc:sqlite:/Users/carlchapman/Documents/SoftwareProjects/tour_de_source/tools/merged/merged_report.db";

	public static TreeSet<RegexProjectSet> initializeCorpus(
			String connectionString) throws ClassNotFoundException,
			SQLException, IllegalArgumentException, QuoteRuleException,
			PythonParsingException {

		HashMap<PatternEscapedPair, TreeSet<Integer>> patternProjectMM = new HashMap<PatternEscapedPair, TreeSet<Integer>>();
		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(connectionString);
		c.setAutoCommit(false);
		stmt = c.createStatement();

		// unlike the previous version, we will now do the group by in memory,
		// to be able to finally get an accurate count of projects per unquoted
		// pattern
		String query = "select pattern, uniqueSourceID from RegexCitationMerged where (flags=0 or flags like 'arg%' or flags=128 or flags='re.DEBUG') and pattern!='arg1';";

		// these are all the distinct patterns with weight
		ResultSet rs = stmt.executeQuery(query);
		TreeSet<String> errorPatternSet = new TreeSet<String>();
		TreeSet<String> alienPatternSet = new TreeSet<String>();
		TreeSet<String> unicodePatternSet = new TreeSet<String>();
		TreeSet<String> corpusPatternSet = new TreeSet<String>();
		while (rs.next()) {
			String pattern = rs.getString("pattern");
			int projectID = rs.getInt("uniqueSourceID");
			try {

				// the important thing to know about patternEscapedPair is that
				// it compares
				// and hashes to others using ONLY the unescaped version
				PatternEscapedPair patternEscapedPair = new PatternEscapedPair(pattern);
				if (patternEscapedPair.getPattern().equals("")) {
					System.out.println("found empty: " + pattern);
				} else {
					TreeSet<Integer> projectIDs = patternProjectMM.get(patternEscapedPair);
					if (projectIDs == null) {
						projectIDs = new TreeSet<Integer>();
					}
					projectIDs.add(projectID);
					patternProjectMM.put(patternEscapedPair, projectIDs);
				}
			} catch (QuoteRuleException e) {
				// System.out.println("problem unquoting pattern: " + pattern);
				errorPatternSet.add(pattern);
			}
		}
		// allPatterns[0] = patternProjectMM.size();

		rs.close();
		stmt.close();
		c.close();

		// sort so that we always get the same order (sets do not guarantee an
		// ordering)
		LinkedList<SortableEntry> entryList = new LinkedList<SortableEntry>();
		for (Entry<PatternEscapedPair, TreeSet<Integer>> entry : patternProjectMM.entrySet()) {
			entryList.add(new SortableEntry(entry.getKey(), entry.getValue()));
		}
		Collections.sort(entryList);
		TreeSet<RegexProjectSet> corpus = new TreeSet<RegexProjectSet>();
		for (SortableEntry entry : entryList) {
			String pattern = entry.getKey().getPattern();
			try {
				RegexProjectSet r = new RegexProjectSet(pattern, entry.getValue());
				corpusPatternSet.add(pattern);
				if (!corpus.add(r)) {
					throw new RuntimeException("Failure to add pattern " +
						pattern + " - every RegexProjectSet must be unique!!!");
				}
			} catch (AlienFeatureException e) {
				String alienMessage = e.getMessage();
				if (alienMessage != null && !alienMessage.equals("")) {
					String token = e.getTokenName();
					if ("<invalid>".equals(token) &&
						(pattern.startsWith("u") || pattern.contains("(?u"))) {
						unicodePatternSet.add(pattern);
					} else {
						alienPatternSet.add(pattern);
					}
				}
				// System.out.println(e.getMessage());
			} catch (IllegalArgumentException e) {
				System.out.println("initializeCorpus: Cannot parse " + pattern +
					" because: " + e.toString());
				errorPatternSet.add(pattern);
				// e.printStackTrace();
			} catch (QuoteRuleException e) {
				errorPatternSet.add(pattern);
			} catch (PythonParsingException e) {
				errorPatternSet.add(pattern);
			}
		}
		return corpus;
	}


////     builds the original projectIDPatternIDMultiMap
//////	public static void main(String[] args) throws ClassNotFoundException,
//////			IllegalArgumentException, SQLException, QuoteRuleException,
//////			PythonParsingException, IOException {
//////		HashMap<String, Integer> patternIndexMap = BehavioralCategories.getPatternIndexMap();
//////		TreeSet<RegexProjectSet> corpus = reloadCorpus();
//////
//////		// now building a reloadable file that maps projectIDs to their
//////		// patterns' javaIDs
//////		StringBuilder sb = new StringBuilder();
//////		File dumpWithIndices = new File(BehavioralCategories.homePath, "projectIDPatternIDMultiMap.txt");
//////		HashMap<Integer, TreeSet<RegexProjectSet>> initial = initializeProjectPatternMM(connectionString,patternIndexMap);
//////
//////		StringBuilder contents = new StringBuilder();
//////		for (Entry<Integer, TreeSet<RegexProjectSet>> e : initial.entrySet()) {
//////			contents.append(e.getKey().toString() + "\t" +
//////				getCSV(e.getValue(),patternIndexMap) + "\n");
//////		}
//////		IOUtil.createAndWrite(dumpWithIndices, contents.toString());
//////		HashMap<Integer, TreeSet<RegexProjectSet>> reloaded = reloadProjectPatternMM(corpus);
//////		System.out.println(reloaded.equals(initial));
//////	}
//
//	private static String getCSV(TreeSet<RegexProjectSet> value,HashMap<String, Integer> patternIndexMap) {
//		StringBuilder sb = new StringBuilder();
//		for(RegexProjectSet y : value){
//			sb.append(patternIndexMap.get(y.getPattern()));
//			sb.append(",");
//		}
//		sb.deleteCharAt(sb.lastIndexOf(","));
//		return sb.toString();
//	}
	
	
//  this one just dumps patterns with their index so you can try to cluster, boring
	// later versions are better
	//
//	// public static void main(String[] args) throws ClassNotFoundException,
//	// IllegalArgumentException, SQLException, QuoteRuleException,
//	// PythonParsingException, IOException {
//	//
//	// // now we are dumping the corpus with the java indices, for behavioral
//	// clustering
//	// StringBuilder sb = new StringBuilder();
//	// File dumpWithIndices = new File(BehavioralCategories.behavioralPath,
//	// "indexedOrderedCorpusDump.txt");
//	// HashMap<String, Integer> patternIndexMap =
//	// BehavioralCategories.getPatternIndexMap();
//	// TreeSet<RegexProjectSet> loadedC = reloadCorpus();
//	// for(RegexProjectSet rps : loadedC){
//	// String original = rps.getContent();
//	// Integer index = patternIndexMap.get(original);
//	// sb.append(index+"\t"+rps.getRankableValue()+"\t"+original+"\n");
//	// }
//	//
//	// IOUtil.createAndWrite(dumpWithIndices, sb.toString());
//	// }

	
//	build the original serialized corpus file, should build into a test fixture.
//	// public static void main(String[] args) throws ClassNotFoundException,
//	// IllegalArgumentException, SQLException, QuoteRuleException,
//	// PythonParsingException, IOException{
//	// //here we serialize the corpus, to avoid lag in development waiting for
//	// corpus to build again
//	// File corpusFile = new File(IOUtil.dataPath +
//	// IOUtil.CORPUS,"serializedCorpus.txt");
//	// File loadedFile = new File(IOUtil.dataPath +
//	// IOUtil.CORPUS,"loadedCorpus.txt");
//	// TreeSet<RegexProjectSet> corpus =
//	// initializeCorpus(Step1_CreateCandidateFiles.connectionString);
//	// StringBuilder contents = new StringBuilder();
//	// for(RegexProjectSet rps :corpus){
//	// contents.append(rps.getContent()+"\t"+rps.getProjectsCSV()+"\n");
//	// }
//	// IOUtil.createAndWrite(corpusFile,contents.toString());
//	// TreeSet<RegexProjectSet> loadedC = reloadCorpus();
//	// StringBuilder contents2 = new StringBuilder();
//	// for(RegexProjectSet rps :loadedC){
//	// contents2.append(rps.getContent()+"\t"+rps.getProjectsCSV()+"\n");
//	// }
//	// IOUtil.createAndWrite(loadedFile,contents2.toString());
//	// System.out.println(corpus.equals(loadedC));
//	// }

}
