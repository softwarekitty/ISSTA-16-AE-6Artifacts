package recreateArtifacts.clusters;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import main.core.RegexProjectSet;
import main.core.categories.Cluster;
import main.io.DumpUtil;
import main.io.IOUtil;
import main.io.LoadUtil;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;
import recreateArtifacts.PathUtil;

//######Input: *similarityGraph.abc*
//
//######Output: *clusters.tsv* and *patternClusterDump.tsv*

public class Main_ClusteringController {

	public static void main(String[] args) throws InterruptedException, IOException, IllegalArgumentException,
			QuoteRuleException, PythonParsingException {
		TreeSet<RegexProjectSet> corpus = LoadUtil
				.loadRegexProjectSetInput(IOUtil.readLines(PathUtil.pathToCorpusFile()));

		String mclInputPath = PathUtil.pathToSimilarityGraph();
		String mclOutputPath = PathUtil.getPathCluster() + "output/clusters.tsv";
		String mclInput = getMCLInput(mclInputPath, mclOutputPath);

		HashMap<Integer, RegexProjectSet> lookup = getLookup(corpus, getPatternIndexMap(corpus));
		TreeSet<Cluster> clusters = getClustersFromGraph(mclOutputPath, mclInput, lookup);

		String patternClusterDumpPath = PathUtil.getPathCluster() + "output/patternClusterDump.tsv";
		DumpUtil.dumpClusters(clusters, new File(patternClusterDumpPath), false);
	}

	private static String getMCLInput(String fullInputFilePath, String fullOutputFilePath) {
		DecimalFormat df = new DecimalFormat("0.00");

		// TODO - break these out into a config
		double i_value = 1.8;
		double p_value = 0.75;
		double k_value = 83;

		String newOptions = " -tf gq(" + df.format(p_value) + ") -tf #knn(" + k_value + ")";
		String mclInput = fullInputFilePath + " -I " + df.format(i_value) + newOptions + " --abc -o "
				+ fullOutputFilePath;
		return mclInput;
	}

	private static TreeSet<Cluster> getClustersFromGraph(String fullOutputFilePath, String mclInput,
			HashMap<Integer, RegexProjectSet> lookup) throws InterruptedException, IOException {

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
		// int m = 0;
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
					// m++;

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

	private static HashMap<String, Integer> getPatternIndexMap(TreeSet<RegexProjectSet> corpus) {
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