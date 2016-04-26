package test.step4;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import main.core.RegexProjectSet;

public final class ClusteringControllerTest {

	@BeforeClass
	public static void setup(){
		// TODO -setup
	}

	@Test
	public void test() {
		fail("clustering controller");
	}
}

//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.TreeSet;
//
//import core.RegexProjectSet;
//import core.categories.Cluster;
//import io.DumpUtil;
//import io.IOUtil;
//
//public class ClusteringController {
//	// looks like this is step 4
//	private static TreeSet<Cluster> getClustersFromFile(String fullInputFilePath, String fullOutputFilePath,
//			String mclInput, HashMap<Integer, RegexProjectSet> lookup) throws InterruptedException, IOException {
//
//		// run the mcl script
//		List<String> cmds = new ArrayList<String>(Arrays.asList(mclInput.split(" ")));
//		System.out.println("mcl input: " + mclInput);
//		cmds.add(0, "/usr/local/bin/mcl");
//		ProcessBuilder pb = new ProcessBuilder(cmds);
//		Process p = pb.start();
//		int x = p.waitFor();
//		System.out.println("process int: " + x);
//
//		// parse mcl output
//		TreeSet<Cluster> clusters = new TreeSet<Cluster>();
//
//		// this outputFile is where mcl wrote its output
//		List<String> lines = IOUtil.readLines(fullOutputFilePath);
//		int lineNumber = 0;
//		int m = 0;
//		// TreeSet<Integer> missingSet = new TreeSet<Integer>();
//
//		// each line of the output is a cluster
//		for (String line : lines) {
//			String[] indices = line.split("\t");
//			Cluster cluster = new Cluster();
//
//			// each tab-separated index is one regex
//			for (String index : indices) {
//				int indexValue = Integer.parseInt(index);
//
//				RegexProjectSet rps = lookup.get(indexValue);
//				if (rps == null) {
//					// happens when the abc file has some index not in
//					// the lookup - 232 values from 13597 to 13910
//					// these are unique patterns were added back
//					// with only edges to themselves to help
//					// to understand a missing 2.5% of projects,
//					// as documented in the git comments on page:
//					// https://github.com/softwarekitty/tour_de_source/commit/020651fca048452df4569e636aebc8e42f9a6153
//					// System.out.println("missing rps at: " + indexValue +
//					// " in cluster: " + cluster.thisClusterID);
//					// missingSet.add(indexValue);
//					m++;
//
//				} else {
//					boolean added = cluster.add(rps);
//					if (!added) {
//						System.out.println(
//								"indexValue: " + indexValue + " failure to add: " + DumpUtil.dumpRegex(0, 1, rps)
//										+ " problem with: " + Arrays.toString(indices) + "on line: " + lineNumber);
//						// System.out.println("cluster: "+cluster.getContent());
//						// waitNsecsOrContinue(12);
//					}
//				}
//			}
//			if (cluster.isEmpty()) {
//				// these clusters are dummy clusters of size 1 to deal with
//				// an accounting issue - pay them no mind
//				// System.out.println("missing cluster "+m+" on: "+ lineNumber);
//			} else {
//				clusters.add(cluster);
//			}
//			lineNumber++;
//		}
//		// System.out.println("nMissing: " + missingSet.size());
//		// System.out.println("missing set: " + missingSet.toString());
//		// System.exit(0);
//		return clusters;
//	}
//}

//private static String getCSV(TreeSet<RegexProjectSet> value, HashMap<String, Integer> patternIndexMap) {
//	StringBuilder sb = new StringBuilder();
//	for (RegexProjectSet y : value) {
//		sb.append(patternIndexMap.get(y.getPattern()));
//		sb.append(",");
//	}
//	sb.deleteCharAt(sb.lastIndexOf(","));
//	return sb.toString();
//}

// builds the original projectIDPatternIDMultiMap
//// public static void main(String[] args) throws ClassNotFoundException,
//// IllegalArgumentException, SQLException, QuoteRuleException,
//// PythonParsingException, IOException {
//// HashMap<String, Integer> patternIndexMap =
// BehavioralCategories.getPatternIndexMap();
//// TreeSet<RegexProjectSet> corpus = reloadCorpus();
////
//// // now building a reloadable file that maps projectIDs to their
//// // patterns' javaIDs
//// StringBuilder sb = new StringBuilder();
//// File dumpWithIndices = new
// File(BehavioralCategories.homePath,"projectIDPatternIDMultiMap.txt");
//// HashMap<Integer, TreeSet<RegexProjectSet>> initial
// =initializeProjectPatternMM(connectionString,patternIndexMap);
////
//// StringBuilder contents = new StringBuilder();
//// for (Entry<Integer, TreeSet<RegexProjectSet>> e : initial.entrySet()) {
//// contents.append(e.getKey().toString() + "\t" +
//// getCSV(e.getValue(),patternIndexMap) + "\n");
//// }
//// IOUtil.createAndWrite(dumpWithIndices, contents.toString());
//// HashMap<Integer, TreeSet<RegexProjectSet>> reloaded =
// reloadProjectPatternMM(corpus);
//// System.out.println(reloaded.equals(initial));
//// }
