package test.core.categories;

import java.util.List;
import java.util.TreeSet;

import main.core.RegexProjectSet;

public class CategoryTestUtil {
	public static RegexProjectSet determineHeaviest(List<RegexProjectSet> regexList) {
		RegexProjectSet actualHeaviest = null;
		int heaviestWeight = Integer.MIN_VALUE;
		for (int i = 0; i < regexList.size(); i++) {
			if (actualHeaviest == null) {
				actualHeaviest = regexList.get(0);
				heaviestWeight = actualHeaviest.getNProjects();
			} else {
				RegexProjectSet current = regexList.get(i);
				int currentWeight = current.getNProjects();
				if (currentWeight > heaviestWeight) {
					heaviestWeight = currentWeight;
					actualHeaviest = current;
				}
			}
		}
		return actualHeaviest;
	}

	public static RegexProjectSet determineShortest(List<RegexProjectSet> regexList) {
		RegexProjectSet actualShortest = null;
		int minLength = Integer.MAX_VALUE;
		for (int i = 0; i < regexList.size(); i++) {
			if (actualShortest == null) {
				actualShortest = regexList.get(0);
				minLength = actualShortest.getPattern().length();
			} else {
				RegexProjectSet current = regexList.get(i);
				int currentLength = current.getPattern().length();
				if (currentLength < minLength) {
					minLength = currentLength;
					actualShortest = current;
				}
			}
		}
		return actualShortest;
	}

	public static TreeSet<Integer> combinePIDs(List<TreeSet<Integer>> treeSetList) {
		TreeSet<Integer> combined = new TreeSet<Integer>();
		for (TreeSet<Integer> member : treeSetList) {
			combined.addAll(member);
		}
		return combined;
	}

}
