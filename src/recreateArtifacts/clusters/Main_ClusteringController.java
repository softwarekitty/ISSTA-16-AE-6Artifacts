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

		HashMap<Integer, RegexProjectSet> lookup = getLookup(corpus);
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

		// each line of the output is a cluster
		for (String line : lines) {
			String[] indices = line.split("\t");
			Cluster cluster = new Cluster();

			// each tab-separated index is one regex
			for (String index : indices) {
				int indexValue = Integer.parseInt(index);

				RegexProjectSet rps = lookup.get(indexValue);
				/**
				 * rps can be null when the abc file has some index not in the
				 * lookup - 232 values from 13597 to 13910 - these are unique
				 * patterns that were added back with only edges to themselves to
				 * help with accounting as documented in the git comments on
				 * page: https://github.com/softwarekitty/tour_de_source/commit/
				 * 020651fca048452df4569e636aebc8e42f9a6153
				 */
				if (rps != null) {
					boolean added = cluster.add(rps);
					if (!added) {
						System.out.println(
								"indexValue: " + indexValue + " failure to add: " + rps.toString()
										+ " problem with: " + Arrays.toString(indices) + "on line: " + lineNumber);
					}
				}
			}
			if (!cluster.isEmpty()) {
				clusters.add(cluster);
			}
			lineNumber++;
		}
		return clusters;
	}

	public static HashMap<Integer, RegexProjectSet> getLookup(TreeSet<RegexProjectSet> corpus) {
		/**
		 * define a comparator that orders the regexes by rawPattern, as this
		 * was the order used before exporting to Rex, and is needed to map the
		 * indices from clusters back to regexes
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
		HashMap<Integer, RegexProjectSet> theLookup = new HashMap<Integer, RegexProjectSet>();
		int i = 0;
		for (RegexProjectSet rps : rawPatternOrderedCorpus) {
			theLookup.put(i++, rps);
		}
		return theLookup;
	}
}