package recreateArtifacts.similarityMatrix;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.io.IOUtil;
import recreateArtifacts.similarityMatrix.threading.Regex;

public class RegexGroup {
	private final HashMap<Integer,Regex> regexMap;
	private final List<Integer> keyList;
	
	public RegexGroup(String filteredCorpusPath) throws IOException{
		// create regexMap and keyList
		regexMap = new HashMap<Integer, Regex>();

		Pattern numberFinder = Pattern.compile("(\\d+)\\t(.*)");
		List<String> lines = IOUtil.readLines(filteredCorpusPath);
		for (String line : lines) {
			Matcher lineMatcher = numberFinder.matcher(line);
			if (lineMatcher.find()) {
				int index = Integer.parseInt(lineMatcher.group(1));
				String pattern = lineMatcher.group(2);
				regexMap.put(index, new Regex(pattern));
			}
		}
		keyList = new LinkedList<Integer>();
		keyList.addAll(regexMap.keySet());
		Collections.sort(keyList);
	}

	public HashMap<Integer, Regex> getRegexMap() {
		return regexMap;
	}

	public List<Integer> getKeyList() {
		return keyList;
	}
	
	public int size(){
		return keyList.size();
	}
}
