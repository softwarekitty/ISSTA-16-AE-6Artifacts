package analysis.categories;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import analysis.build_corpus.CorpusUtil;
import analysis.build_corpus.RegexProjectSet;
import analysis.exceptions.PythonParsingException;
import analysis.exceptions.QuoteRuleException;
import analysis.util.DumpUtil;
import analysis.util.IOUtil;

public class BehavioralCategories {


	// looks like this is step 4
	private static TreeSet<Cluster> getClustersFromFile(String fullInputFilePath, String fullOutputFilePath,
			String mclInput, HashMap<Integer, RegexProjectSet> lookup) throws InterruptedException, IOException {

		// run the mcl script
		List<String> cmds = new ArrayList<String>(Arrays.asList(mclInput.split(" ")));
		System.out.println("mcl input: " + mclInput);
		cmds.add(0, "/usr/local/bin/mcl");
		ProcessBuilder pb = new ProcessBuilder(cmds);
		Process p = pb.start();
		int x = p.waitFor();
		System.out.println("process int: " + x);

		// parse mcl output
		TreeSet<Cluster> clusters = new TreeSet<Cluster>();

		// this outputFile is where mcl wrote its output
		List<String> lines = IOUtil.readLines(fullOutputFilePath);
		int lineNumber = 0;
		int m = 0;
		// TreeSet<Integer> missingSet = new TreeSet<Integer>();

		// each line of the output is a cluster
		for (String line : lines) {
			String[] indices = line.split("\t");
			Cluster cluster = new Cluster();

			// each tab-separated index is one regex
			for (String index : indices) {
				int indexValue = Integer.parseInt(index);

				RegexProjectSet rps = lookup.get(indexValue);
				if (rps == null) {
					// happens when the abc file has some index not in
					// the lookup - 232 values from 13597 to 13910
					// these are unique patterns were added back
					// with only edges to themselves to help
					// to understand a missing 2.5% of projects,
					// as documented in the git comments on page:
					// https://github.com/softwarekitty/tour_de_source/commit/020651fca048452df4569e636aebc8e42f9a6153
					// System.out.println("missing rps at: " + indexValue +
					// " in cluster: " + cluster.thisClusterID);
					// missingSet.add(indexValue);
					m++;

				} else {
					boolean added = cluster.add(rps);
					if (!added) {
						System.out.println(
								"indexValue: " + indexValue + " failure to add: " + DumpUtil.dumpRegex(0, 1, rps)
										+ " problem with: " + Arrays.toString(indices) + "on line: " + lineNumber);
						// System.out.println("cluster: "+cluster.getContent());
						// waitNsecsOrContinue(12);
					}
				}
			}
			if (cluster.isEmpty()) {
				// these clusters are dummy clusters of size 1 to deal with
				// an accounting issue - pay them no mind
				// System.out.println("missing cluster "+m+" on: "+ lineNumber);
			} else {
				clusters.add(cluster);
			}
			lineNumber++;
		}
		// System.out.println("nMissing: " + missingSet.size());
		// System.out.println("missing set: " + missingSet.toString());
		// System.exit(0);
		return clusters;
	}

	// belongs in Corpus util?
	public static HashMap<Integer, RegexProjectSet> getLookup(String filtered_corpus_path,
			TreeSet<RegexProjectSet> corpus, HashMap<String, Integer> patternIndexMap) {
		HashMap<Integer, RegexProjectSet> lookup = new HashMap<Integer, RegexProjectSet>();

		for (RegexProjectSet regex : corpus) {
			String originalPattern = regex.getContent();
			Integer javaIndex = patternIndexMap.get(originalPattern);
			lookup.put(javaIndex, regex);
		}
		return lookup;
	}

	// who says this?
	public static HashMap<String, Integer> getPatternIndexMap() throws IOException {
		HashMap<String, Integer> patternIndexMap = new HashMap<String, Integer>();
		//TODO
//		String content = IOUtil.readFileToString(homePath + "exportedCorpusRaw.txt");
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

	///////////categorizing///////////
	
	private static boolean addClusterToCategoryClusters(Cluster cluster, List<Category> categories,
			HashMap<String, Integer> patternIndexMap) {
		Integer javaIndex = patternIndexMap.get(cluster.getHeaviest().getContent());
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
		HashMap<Integer, TreeSet<RegexProjectSet>> projectPatternMM = CorpusUtil.reloadProjectPatternMM(corpus);

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
			String clusterShortestCode = "" + patternIndexMap.get(cluster.getShorty().getContent());
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
			String categoryRepString = categoryRep == null ? "NO CONTENT" : DumpUtil.verbatimWrap(categoryRep);
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
}
