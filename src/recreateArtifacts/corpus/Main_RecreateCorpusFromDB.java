package recreateArtifacts.corpus;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.json.JSONException;

import main.core.RegexProjectSet;
import main.core.features.AlienFeatureException;
import main.io.Config;
import main.io.IOUtil;
import main.io.LoadUtil;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;

public class Main_RecreateCorpusFromDB {

	public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException, SQLException,
			QuoteRuleException, PythonParsingException, IOException, JSONException {
		Config config = new Config();
		TreeSet<RegexProjectSet> corpusFromDB = initializeCorpus_and_refreshPatternTracking(config);
		String pathToCorpusFile = new File(Config.homePath, getPathToRecreateCorpus()+"fullCorpus.txt").getPath();
		TreeSet<RegexProjectSet> corpusLoadedFromFile = LoadUtil
				.loadRegexProjectSetInput(IOUtil.readLines(pathToCorpusFile));
		
		// will print "true" if the two sets are equal
		System.out.println(corpusFromDB.equals(corpusLoadedFromFile));
	}

	public static TreeSet<RegexProjectSet> initializeCorpus_and_refreshPatternTracking(Config config)
			throws ClassNotFoundException, SQLException, IllegalArgumentException, QuoteRuleException,
			PythonParsingException {

		HashMap<PatternEscapedPair, TreeSet<Integer>> patternProjectMM = new HashMap<PatternEscapedPair, TreeSet<Integer>>();
		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(getConnectionString(config));
		c.setAutoCommit(false);
		stmt = c.createStatement();

		// do the group by in memory, to get an accurate count
		// of projects per unquoted pattern
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
					throw new RuntimeException(
							"Failure to add pattern " + pattern + " - every RegexProjectSet must be unique!!!");
				}
			} catch (AlienFeatureException e) {
				String alienMessage = e.getMessage();
				if (alienMessage != null && !alienMessage.equals("")) {
					String token = e.getTokenName();
					if ("<invalid>".equals(token) && (pattern.startsWith("u") || pattern.contains("(?u"))) {
						unicodePatternSet.add(pattern);
					} else {
						alienPatternSet.add(pattern);
					}
				}
				// System.out.println(e.getMessage());
			} catch (IllegalArgumentException e) {
				System.out.println("initializeCorpus: Cannot parse " + pattern + " because: " + e.toString());
				errorPatternSet.add(pattern);
			} catch (QuoteRuleException e) {
				errorPatternSet.add(pattern);
			} catch (PythonParsingException e) {
				errorPatternSet.add(pattern);
			}
		}

		String pathToPatternTracking = getPathToRecreateCorpus() + "patternTracking/";
		File errorFile = new File(Config.homePath, pathToPatternTracking + "errorPatterns.txt");
		File alienFile = new File(Config.homePath, pathToPatternTracking + "alienPatterns.txt");
		File unicodeFile = new File(Config.homePath, pathToPatternTracking + "unicodePatterns.txt");
		File corpusFile = new File(Config.homePath, pathToPatternTracking + "corpusPatterns.txt");
		IOUtil.createAndWrite(errorFile, contentFromStringSet(errorPatternSet));
		IOUtil.createAndWrite(alienFile, contentFromStringSet(alienPatternSet));
		IOUtil.createAndWrite(unicodeFile, contentFromStringSet(unicodePatternSet));
		IOUtil.createAndWrite(corpusFile, contentFromStringSet(corpusPatternSet));
		return corpus;
	}

	private static String getConnectionString(Config config) {
		String pathToDb = new File(Config.homePath, "artifacts/merged_report.db").getPath();
		return "jdbc:sqlite:" + pathToDb;
	}

	private static String getPathToRecreateCorpus() {
		return "src/recreateArtifacts/corpus/";
	}

	private static String contentFromStringSet(Collection<String> stringSet) {
		StringBuilder sb = new StringBuilder();
		for (String s : stringSet) {
			sb.append(s + "\n");
		}
		return sb.toString();
	}
}
