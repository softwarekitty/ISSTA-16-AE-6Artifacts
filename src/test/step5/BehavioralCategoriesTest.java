package step5;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.RegexProjectSet;
import core.categories.Category;
import core.categories.Cluster;
import io.DumpUtil;
import io.IOUtil;
import parse.PythonParsingException;
import parse.QuoteRuleException;
import tmp_testing_fixtures.CorpusUtil;

public class BehavioralCategories {
	
	private static boolean addClusterToCategoryClusters(Cluster cluster, List<Category> categories,
			HashMap<String, Integer> patternIndexMap) {
		Integer javaIndex = patternIndexMap.get(cluster.getHeaviest().getPattern());
		int i = 0;
		//TODO
		for (; i < 0; i++) {
			List<Integer> categoryMembers = new LinkedList<Integer>();
			if (categoryMembers.contains(javaIndex)) {
				categories.get(i).add(cluster);
				return true;
			}
		}
		categories.get(i).add(cluster);
		return false;
	}

	private static void dumpCategories(TreeSet<Cluster> behavioralClusters, String outFilename,
			HashMap<String, Integer> patternIndexMap, TreeSet<RegexProjectSet> corpus) throws ClassNotFoundException,
			SQLException, IllegalArgumentException, IOException, QuoteRuleException, PythonParsingException {
		HashMap<Integer, TreeSet<RegexProjectSet>> projectPatternMM = reloadProjectPatternMM(corpus);

		StringBuilder sb = new StringBuilder();

		List<Category> categoryClusters = new LinkedList<Category>();
		//TODO
//		for (int catID = 0; catID <= 0; catID++) {
//			categoryClusters.add(new Category());
//		}

		int touchedPatterns = 0;
		int categorizedClusters = 0;
		TreeSet<Integer> allProjectIDs = new TreeSet<Integer>();
		Cluster allCategorizedRegexes = new Cluster();
		Category nonCategorizedClusters = new Category();

		// main loop
		for (Cluster cluster : behavioralClusters) {
			String clusterShortestCode = "" + patternIndexMap.get(cluster.getShorty().getPattern());
			System.out.println(clusterShortestCode);
			touchedPatterns += cluster.size();
			allProjectIDs.addAll(cluster.getAllProjectIDs());
			if (addClusterToCategoryClusters(cluster, categoryClusters, patternIndexMap)) {
				categorizedClusters++;
				allCategorizedRegexes.addAll(cluster);
			} else {
				nonCategorizedClusters.add(cluster);
			}
		}

		int p = 0;
		for (Category cat : categoryClusters) {
			System.out.println("p: " + p++ + " size: " + cat.size());
		}

		// measuring coverage
		int totallyCoveredProjects = 0;
		int partiallyCoveredProjects = 0;
		int nProjectsLoaded = projectPatternMM.size();
		for (Entry<Integer, TreeSet<RegexProjectSet>> entry : projectPatternMM.entrySet()) {
			TreeSet<RegexProjectSet> regexes = entry.getValue();
			TreeSet<RegexProjectSet> regexesCopy = new TreeSet<RegexProjectSet>();
			regexesCopy.addAll(regexes);
			regexesCopy.removeAll(allCategorizedRegexes);
			if (regexesCopy.isEmpty()) {
				totallyCoveredProjects++;
			} else if (regexes.size() - regexesCopy.size() > 0) {
				partiallyCoveredProjects++;
			}
		}
		int untouchedProjects = nProjectsLoaded - (partiallyCoveredProjects + totallyCoveredProjects);

		// print a summary report
		sb.append("Cluster stats:\n\ntotalClusters: " + behavioralClusters.size() + "\nCategorizedClusters: "
				+ categorizedClusters + "\nTotalPatterns: " + corpus.size() + " (in the corpus)" + "\nTouchedPatterns: "
				+ touchedPatterns + " (by some cluster)" + "\nnTotalProjects: " + nProjectsLoaded
				+ " (containing a corpus regex)" + "\nnProjectsTouched: " + allProjectIDs.size() + " (by some cluster)"
				+ "\ntotallyCoveredProjects: " + totallyCoveredProjects + " (by categorized regexes)"
				+ "\npartiallyCoveredProjects: " + partiallyCoveredProjects + " (by categorized regexes)"
				+ "\nuntouchedProjects: " + untouchedProjects + " (no category touches these)" + "\n\n");

		// get the bulk of data
		sb.append(getCategoryProjectInfo(categoryClusters, patternIndexMap) + "\n\n\n");
		//TODO
//		File output = new File(homePath, outFilename);
//		IOUtil.createAndWrite(output, sb.toString());
	}

	// this builds the giant string printed in the cluster dump
	// by printing all clusters in each category
	private static String getCategoryProjectInfo(List<Category> categories, HashMap<String, Integer> patternIndexMap)
			throws ClassNotFoundException, SQLException {
		StringBuilder sb = new StringBuilder();
		String categoryHeader = "\\begin{multicols}{1}\n\\begin{description}[noitemsep,topsep=0pt]\n";
		String categoryFooter = "\\end{description}\n\\end{multicols}\n\n\n\n";
		int i = 1;
		for (Category category : categories) {
			System.out.println("category.i: " + i);
			sb.append(categoryHeader);
			int nClusters = category.size();
			int nPatternsTotal = category.categoryTotalPatterns();
			int nProjectsTotal = category.categoryTotalProjects();
			//TODO false?
			boolean useHeaviest = false;
			RegexProjectSet categoryRep = category.getRepresentative(useHeaviest );
			String categoryRepString = categoryRep == null ? "NO CONTENT" : DumpUtil.verbatimWrap(categoryRep.getRawPattern());
			//TODO names?
			String name = "TODO NAMES";
			sb.append("categoryCluster " + i + " stats:\nname: " + name + "\nnClusters: " + nClusters
					+ "\nnPatternsTotal: " + nPatternsTotal + "\nnProjectsTotal: " + nProjectsTotal + "\nshortest: "
					+ categoryRepString + "\n\n");
			i++;
			for (Cluster currentCluster : category) {
				sb.append(DumpUtil.getItemLineLatex(patternIndexMap, currentCluster, useHeaviest));
			}
			sb.append(categoryFooter);
		}
		return sb.toString();
	}
	
	public static HashMap<Integer, TreeSet<RegexProjectSet>> reloadProjectPatternMM(TreeSet<RegexProjectSet> corpus)
			throws IOException, IllegalArgumentException, QuoteRuleException,
			PythonParsingException {
		HashMap<Integer, TreeSet<RegexProjectSet>> reloadedProjectPatternMM = new HashMap<Integer, TreeSet<RegexProjectSet>>();
		HashMap<String, Integer> patternIndexMap = getPatternIndexMap();
		//TODO
//		HashMap<Integer, RegexProjectSet> lookup = BehavioralCategories.getLookup(BehavioralCategories.filtered_corpus_path, corpus, patternIndexMap);

//		File dumpWithIndices = new File(BehavioralCategories.homePath, "projectIDPatternIDMultiMap.txt");
		//TODO
		String serializedProjectPatternMM = IOUtil.readFileToString("fix this file path");
		Pattern finder = Pattern.compile("(\\d+)\\t(.*)");
		Matcher pairMatcher = finder.matcher(serializedProjectPatternMM);
		while (pairMatcher.find()) {
			String projectID = pairMatcher.group(1);
			String patternIDList = pairMatcher.group(2);
			List<String> patternIDs = Arrays.asList(patternIDList.split(","));
			TreeSet<RegexProjectSet> regexesInAProject = new TreeSet<RegexProjectSet>();
			for (String IDString : patternIDs) {
				Integer ID = Integer.parseInt(IDString);
				//TODO
//				regexesInAProject.add(lookup.get(ID));
			}
			reloadedProjectPatternMM.put(Integer.parseInt(projectID), regexesInAProject);

		}
		return reloadedProjectPatternMM;
	}
	
	public static HashMap<String, Integer> getPatternIndexMap() throws IOException {
		HashMap<String, Integer> patternIndexMap = new HashMap<String, Integer>();
		// TODO
		// String content = IOUtil.readFileToString(homePath +
		// "exportedCorpusRaw.txt");
		String content = "";
		Pattern finder = Pattern.compile("(\\d+)\\t(\\d+)\\t(.*)");
		Matcher pairMatcher = finder.matcher(content);
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
		// System.out.println(patternIndexMap.size());
		return patternIndexMap;
	}
	
	public static HashMap<Integer, TreeSet<RegexProjectSet>> initializeProjectPatternMM(
			String connectionString,HashMap<String, Integer> patternIndexMap) throws IOException,
			IllegalArgumentException, QuoteRuleException,
			PythonParsingException, SQLException, ClassNotFoundException {
		HashMap<Integer, TreeSet<RegexProjectSet>> initialProjectPatternMM = new HashMap<Integer, TreeSet<RegexProjectSet>>();

		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(connectionString);
		c.setAutoCommit(false);
		stmt = c.createStatement();


		//TODO
		TreeSet<RegexProjectSet> corpus = null;//CorpusUtil.reloadCorpus();
		//TODO
		HashMap<Integer, RegexProjectSet> lookup = getLookup("was!!!!!!!!! filtered corpus path", corpus, patternIndexMap);

		String query = "select pattern, uniqueSourceID from RegexCitationMerged where (flags=0 or flags like 'arg%' or flags=128 or flags='re.DEBUG') and pattern!='arg1';";

		// these are all the distinct patterns with weight
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			int projectID = rs.getInt("uniqueSourceID");
			String pattern = rs.getString("pattern");
			Integer patternID = patternIndexMap.get(pattern);
			if (patternID == null) {
				continue;
			} else {
				RegexProjectSet regex = lookup.get(patternID);
				TreeSet<RegexProjectSet> regexesInAPattern = initialProjectPatternMM.get(projectID);
				if (regexesInAPattern == null) {
					regexesInAPattern = new TreeSet<RegexProjectSet>();
				}
				regexesInAPattern.add(regex);
				initialProjectPatternMM.put(projectID, regexesInAPattern);
			}
		}

		rs.close();
		stmt.close();
		c.close();
		return initialProjectPatternMM;
	}
	
	public static HashMap<Integer, RegexProjectSet> getLookup(String filtered_corpus_path,
			TreeSet<RegexProjectSet> corpus, HashMap<String, Integer> patternIndexMap) {
		HashMap<Integer, RegexProjectSet> lookup = new HashMap<Integer, RegexProjectSet>();

		for (RegexProjectSet regex : corpus) {
			String originalPattern = regex.getPattern();
			Integer javaIndex = patternIndexMap.get(originalPattern);
			lookup.put(javaIndex, regex);
		}
		return lookup;
	}
}
