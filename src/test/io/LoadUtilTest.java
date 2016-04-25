package io;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import core.RegexProjectSet;
import parse.PythonParsingException;
import parse.QuoteRuleException;

public class LoadUtil {

	/**
	 * Loads input files, expecting a quoted, escaped regex on the left, then a
	 * tab, then a comma separated list of integers on the right. This is
	 * fragile, expecting good behavior from the user.
	 * 
	 * @param inputSourcePath
	 *            Full path to the input source
	 * @return A TreeSet<RegexProjectSet> containing the input data from the
	 *         file.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws QuoteRuleException
	 * @throws PythonParsingException
	 */
	public static TreeSet<RegexProjectSet> loadRegexProjectSetInput(String inputSourcePath)
			throws IOException, IllegalArgumentException, QuoteRuleException, PythonParsingException {
		TreeSet<RegexProjectSet> regexProjectSetInput = new TreeSet<RegexProjectSet>();
		List<String> lines = IOUtil.readLines(inputSourcePath);
		for (String line : lines) {
			String[] parts = line.split("\t");
			String[] IDs = parts[1].split(",");
			TreeSet<Integer> IDSet = new TreeSet<Integer>();
			for (String id : IDs) {
				IDSet.add(Integer.parseInt(id));
			}
			regexProjectSetInput.add(new RegexProjectSet(parts[0], IDSet));
		}
		return regexProjectSetInput;
	}

}
