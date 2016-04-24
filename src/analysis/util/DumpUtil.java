package analysis.util;

import java.io.File;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import analysis.build_corpus.RegexProjectSet;
import analysis.categories.Cluster;

/**
 * helps to prepare human readable data dumps
 * 
 * @author cc
 */
public class DumpUtil {

	public static String getItemLineLatex(HashMap<String, Integer> patternIndexMap, Cluster cluster,
			boolean shouldUseHeaviest) {
		StringBuilder sb = new StringBuilder();
		RegexProjectSet clusterRep = shouldUseHeaviest ? cluster.getHeaviest() : cluster.getShorty();

		Integer index = patternIndexMap.get(clusterRep.getContent());
		sb.append(", " + index + " ");
		sb.append(itemMaker(clusterRep, getClusterDescription(cluster)));
		sb.append("\n");
		return sb.toString();
	}

	private static String itemMaker(RegexProjectSet regex, String description) {
		StringBuilder sb = new StringBuilder();
		sb.append("\\item ");
		sb.append("[" + description + "] ");
		sb.append(DumpUtil.verbatimWrap(regex));
		return sb.toString();
	}

	public static String verbatimWrap(RegexProjectSet regex) {
		char[] charsToUse = { '!', '@', '|', ':' };
		String un = regex.getUnescapedPattern();
		for (int i = 0; i < charsToUse.length; i++) {
			char c = charsToUse[i];
			if (un.indexOf(c) == -1) {
				return "\\cverb" + c + un + c;
			}
		}
		return "\\cverb•" + un + "•";
	}

	public static String getClusterDescription(Cluster cluster) {
		return "(" + cluster.getNProjects() + " \\<" + cluster.size() + "\\>)";
	}

	public static String dumpRegex(int index, int paddedSize, RegexProjectSet regex) {
		return StringUtils.leftPad(String.valueOf(index), paddedSize, "0") + " | "
				+ StringUtils.leftPad(String.valueOf(regex.getRankableValue()), paddedSize, "0") + " | "
				+ regex.getContent() + "\n";
	}
	
	public static String projectCSV(RegexProjectSet regex){
		StringBuilder sb = new StringBuilder();
		TreeSet<Integer> projectIDSet= regex.getProjectIDSet();
		for (Integer pID : projectIDSet) {
			sb.append(pID);
			sb.append(",");
		}
		String allWithExtraComma = sb.toString();
		return allWithExtraComma.substring(0, allWithExtraComma.length() - 1);
	}
	
	public static void dumpClusters(TreeSet<Cluster> behavioralClusters,
			File patternClusterFile, boolean tex) {
		StringBuilder sb = new StringBuilder();
		int i=0;
		for(Cluster cls: behavioralClusters ){
			sb.append("Cluster i: "+i++ +"\n");
			sb.append(getPatternDump(cls,tex));
			sb.append("\n\n\n\n");
		}
		
		IOUtil.createAndWrite(patternClusterFile, sb.toString());
	}
	
	public static String getPatternDump(Cluster cluster, boolean tex) {
		String categoryHeader = tex ? "\\begin{multicols}{1}\n\\begin{description}[noitemsep,topsep=0pt]\n" : "";
		String categoryFooter = tex ? "\\end{description}\n\\end{multicols}\n\n\n\n" : "";
		StringBuilder sb = new StringBuilder();
		sb.append("total projects:" + cluster.getNProjects() + "\n");
		sb.append("total patterns:" + cluster.getNPatterns() + "\n");
		sb.append(categoryHeader);
		for (RegexProjectSet member : cluster) {
			String regexRepresentation = tex ? itemMaker(member, "[" + member.getRankableValue() + "] ") : regexRow(member);
			sb.append(regexRepresentation);
			sb.append("\n");
		}
		sb.append(categoryFooter);
		return sb.toString();
	}

	public static String regexRow(RegexProjectSet regex) {
		return regex.getContent() + "\t" + projectCSV(regex);
	}

}
