package main.core.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.runtime.tree.CommonTree;

import main.parse.pcre.PCRE;
import main.parse.pcre.PCREParser;

public class FeatureCountFactory {
	private static FeatureDictionary featureDictionary = new FeatureDictionary();

	/**
	 * 
	 * @param rawPattern
	 *            The raw (unescaped and unquoted) pattern to be parsed and have
	 *            its features counted.
	 * @return a FeatureCount object representing the number of each feature in
	 *         the raw pattern.
	 * @throws AlienFeatureException
	 *             If the tree contains a feature outside of the studied feature
	 *             set.
	 */
	public static FeatureCount getFeatureCount(String rawPattern) throws AlienFeatureException {
		return getFeatureCount(rawPatternToIndexCountMap(rawPattern));
	}

	/**
	 * creates a FeatureCount from a Map<Integer,Integer>
	 * 
	 * @param indexCountMap
	 *            maps indexes to count values. keys outside of the correct
	 *            range are ignored.
	 */
	public static FeatureCount getFeatureCount(Map<Integer, Integer> indexCountMap) {
		int correctLength = featureDictionary.getSize();
		int[] array = new int[correctLength];
		Set<Entry<Integer, Integer>> entries = indexCountMap.entrySet();

		// ignores keys outside of the correct range
		for (Entry<Integer, Integer> entry : entries) {
			int proposedIndex = entry.getKey();
			if (proposedIndex >= 0 && proposedIndex < correctLength) {
				array[entry.getKey()] = entry.getValue();
			}
		}
		return new FeatureCount(array, featureDictionary);
	}

	/**
	 * creates a FeatureCount from an array of Integers representing key
	 * indices, where the value for each key is set to one.
	 * 
	 * @param keyList
	 */
	public static FeatureCount getFeatureCount(Integer... keyList) {
		HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
		for (Integer key : keyList) {
			counts.put(key, 1);
		}
		return getFeatureCount(counts);
	}

	/*
	 * This is the important code for counting features. It walks the parse tree
	 * created by the PCRE parser and counts how many of each feature is
	 * present, creating a Map<Integer,Integer> that is used to create the
	 * FeatureCount.
	 * 
	 * This contains feature analysis logic that is tightly coupled with the
	 * PCRE parser interfaces (CommonTree), and with the FeatureDictionary
	 * class, which is tightly coupled with the decisions of what features to
	 * include in the analysis. Changing any of those and wanting to be able to
	 * use this class in a frequency analysis may require changes here.
	 * 
	 * @param rawPattern Escaped, unquoted pattern used to build a parse tree.
	 * 
	 * @return A map going from feature indexes to the number of times they
	 * appear
	 * 
	 * @throws AlienFeatureException
	 */
	@SuppressWarnings("unchecked")
	public static Map<Integer, Integer> rawPatternToIndexCountMap(String rawPattern) throws AlienFeatureException {
		CommonTree treeRoot = new PCRE(rawPattern).getCommonTree();
		HashMap<Integer, Integer> indexCountMap = new HashMap<Integer, Integer>();

		List<CommonTree> firstStack = new ArrayList<CommonTree>();
		firstStack.add(treeRoot);

		// stack used for DFS
		List<List<CommonTree>> childListStack = new ArrayList<List<CommonTree>>();
		childListStack.add(firstStack);

		while (!childListStack.isEmpty()) {

			List<CommonTree> childStack = childListStack.get(childListStack.size() - 1);

			if (childStack.isEmpty()) {
				childListStack.remove(childListStack.size() - 1);
			} else {
				CommonTree subTree = childStack.remove(0);
				
				// this is the visitor call
				incrementCount(subTree, indexCountMap, rawPattern);
				if (subTree.getChildCount() > 0) {
					childListStack.add(new ArrayList<CommonTree>((List<CommonTree>) subTree.getChildren()));
				}
			}
		}
		return indexCountMap;
	}

	/**
	 * this method looks at a tree node, and if that node is a feature node, the
	 * count is adjusted to reflect the presence of this feature to get this to
	 * work, I added Repetition names to the PCREParser
	 * 
	 * @param tree
	 *            The current tree node being checked for feature status and
	 *            type.
	 * @param indexCountMap
	 *            The map tracking how many of each feature have been observed.
	 * @param rawPattern
	 *            The raw pattern being parsed, used only for debugging
	 *            messages.
	 * @throws AlienFeatureException
	 */
	private static void incrementCount(CommonTree tree, Map<Integer, Integer> indexCountMap, String rawPattern)
			throws AlienFeatureException {

		// these tree nodes are branches of the parse tree, not leaves
		List<String> ignoreList = Arrays.asList("", "QUANTIFIER", "NUMBER", "GREEDY", "ALTERNATIVE", "ELEMENT", "NAME",
				"OPTION", "SET", "UNSET");

		// repetitions use the same name as the FeatureDictionary, so just use
		// those
		String tokenName = "";
		if (tree.getType() == PCREParser.REPETITION_TYPE) {
			tokenName = tree.getText();

			// otherwise, look up the tokenName
		} else {
			tokenName = PCREParser.tokenNames[tree.getType()];
		}
		int featureIndex = featureDictionary.getIndex(tokenName);

		// it is some valid feature index that maps directly
		if (featureIndex >= 0) {

			// increment or put one if it is not already there
			Integer previousValue = indexCountMap.get(featureIndex);
			if (previousValue == null) {
				indexCountMap.put(featureIndex, 1);
			} else {
				indexCountMap.put(featureIndex, previousValue + 1);
			}

			// or it is not supported by the analysis
		} else if (!ignoreList.contains(tokenName)) {
			throw new AlienFeatureException("found unsupported feature: " + PCREParser.tokenNames[tree.getType()]
					+ " in rawPattern: " + rawPattern, PCREParser.tokenNames[tree.getType()]);
		}
	}
}
