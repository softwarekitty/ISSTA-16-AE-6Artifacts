package core.corpus;

import java.util.TreeSet;

import org.antlr.runtime.tree.CommonTree;
import org.python.util.PythonInterpreter;

import core.metric.FeatureCount;
import exceptions.PythonParsingException;
import exceptions.QuoteRuleException;
import io.util.DumpUtil;
import pcre.PCRE;

/**
 * a RegexProjectSet is a pattern String and a set of project IDs.
 * 
 * The pattern string is checked for correctness in several ways before
 * construction is complete: it must follow Python quoting rules, must not be
 * null or empty, it must compile to a valid Python regex (checked using
 * Jython), must be parsed by PCRE without exceptions, and must contain only
 * features in the studied feature set.
 * 
 * The set of project IDs must not be empty.
 * 
 * @author cc
 *
 */
public final class RegexProjectSet implements Comparable<RegexProjectSet> {
	private final String pattern;
	private final String rawPattern;
	private final FeatureCount features;
	private final TreeSet<Integer> projectIDSet;

	public RegexProjectSet(String pattern, TreeSet<Integer> projectIDSet)
			throws QuoteRuleException, IllegalArgumentException, PythonParsingException {
		if (pattern == null) {
			throw new IllegalArgumentException("pattern cannot be null: " + pattern);
		}
		rawPattern = PatternUtil.getUnescaped(PatternUtil.getUnquotedPythonPattern(pattern));

		if ("".equals(rawPattern)) {
			throw new IllegalArgumentException("pattern cannot be empty: " + pattern);
		} else {
			this.pattern = pattern;
			if (projectIDSet == null || projectIDSet.isEmpty()) {
				throw new IllegalArgumentException("projectIDSet cannot be null or empty for pattern: " + pattern);
			}
			this.projectIDSet = projectIDSet;
			try {

				// make sure the pattern is a valid regex
				PythonInterpreter interpreter = new PythonInterpreter();
				interpreter.exec("import re");
				interpreter.exec("x = re.compile(" + pattern + ")");
			} catch (Exception e) {
				throw new PythonParsingException("Failure when trying to compile pattern in Python: " + pattern);
			}

			// parse into the Commontree to count features
			CommonTree rootTree = new PCRE(rawPattern).getCommonTree();
			this.features = new FeatureCount(rootTree, pattern);
		}
	}

	/**
	 * get the pattern
	 * 
	 * @return the original pattern string, with escaped slashes and whatever
	 *         quoting was used.
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * get the raw pattern
	 * 
	 * @return an unquoted, unescaped version of the pattern
	 */
	public String getRawPattern() {
		return rawPattern;
	}

	/**
	 * get the number of projects that contain this regex
	 * 
	 * @return the number of projects that contain this regex
	 */
	public int getNProjects() {
		return projectIDSet.size();
	}

	/**
	 * get a defensive copy of the set of project IDs for projects containing
	 * this regex
	 * 
	 * @return the set of project IDs for projects containing this regex
	 */
	public TreeSet<Integer> getProjectIDSet() {
		TreeSet<Integer> defensiveCopy = new TreeSet<Integer>();
		defensiveCopy.addAll(projectIDSet);
		return defensiveCopy;
	}

	public FeatureCount getFeatures() {
		return features;
	}

	@Override
	public int compareTo(RegexProjectSet other) {
		// higher weight is earlier
		if (this.getNProjects() > other.getNProjects()) {
			return -1;
		} else if (this.getNProjects() < other.getNProjects()) {
			return 1;
		} else {
			// shorter length is earlier
			if (this.pattern.length() > other.pattern.length()) {
				return 1;
			} else if (this.pattern.length() < other.pattern.length()) {
				return -1;
			} else {

				// larger pattern hashcode is earlier
				if (this.pattern.equals(other.pattern)) {
					return 0;
				} else if (this.hashCode() > other.hashCode()) {
					return -1;
				} else if (this.hashCode() < other.hashCode()) {
					return 1;
				} else {
					return -1;
				}
			}
		}
	}
	
	///// hashcode, toString, equals /////

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		result = prime * result + ((projectIDSet == null) ? 0 : projectIDSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegexProjectSet other = (RegexProjectSet) obj;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.equals(other.pattern))
			return false;
		if (projectIDSet == null) {
			if (other.projectIDSet != null)
				return false;
		} else if (!projectIDSet.equals(other.projectIDSet))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return DumpUtil.regexRow(this);
	}
}
