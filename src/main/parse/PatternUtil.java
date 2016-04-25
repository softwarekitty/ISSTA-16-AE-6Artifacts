package main.parse;

import org.python.util.PythonInterpreter;

import main.core.features.FeatureCount;
import main.parse.pcre.PCRE;

//TODO - TEST ME, PLEEASE
public class PatternUtil {

	public static String getUnescaped(String unquotedPattern) {
		return unquotedPattern.replaceAll("\\\\\\\\", "\\\\");
	}

	public static String getUnquotedPythonPattern(String pat) throws QuoteRuleException {

		// python can do: u'...', ur'...', r'...'
		String removedUR = pat.startsWith("ur") ? pat.substring(2) : pat;
		if (removedUR.length() == 0) {
			return "";
		}
		String removedPrefix = (removedUR.startsWith("u") || removedUR.startsWith("r")) ? removedUR.substring(1)
				: removedUR;
		if (removedPrefix.length() < 2) {
			return "";
		}
		char firstChar = removedPrefix.charAt(0);
		char lastChar = removedPrefix.charAt(removedPrefix.length() - 1);
		char singleQuote = '\'';
		char doubleQuote = '"';

		if (!(firstChar == singleQuote && lastChar == singleQuote)
				&& !(firstChar == doubleQuote && lastChar == doubleQuote)) {
			throw new QuoteRuleException("the pattern: " + pat + " does not conform to the expected quotation rules");
		}
		String unQuoted = null;
		String trippleSingleQuote = "'''";
		String trippleDoubleQuote = "\"\"\"";
		if (isTripple(removedPrefix, trippleSingleQuote) || isTripple(removedPrefix, trippleDoubleQuote)) {
			unQuoted = removeQuotes(removedPrefix, 3);
		} else {
			unQuoted = removeQuotes(removedPrefix, 1);
		}
		if (unQuoted.length() == 0) {
			return "";
		}
		return unQuoted;
	}

	private static String removeQuotes(String s, int i) {
		return s.substring(i, s.length() - i);
	}

	private static boolean isTripple(String s, String tripple) {
		if (s.length() < 6) {
			return false;
		}
		int threeFromEnd = s.length() - 3;
		return s.startsWith(tripple) && s.substring(threeFromEnd).equals(tripple);
	}

	public static void validatePythonRegex(String pattern) throws PythonParsingException {
		try {

			// make sure the pattern is a valid regex
			PythonInterpreter interpreter = new PythonInterpreter();
			interpreter.exec("import re");
			interpreter.exec("x = re.compile(" + pattern + ")");
		} catch (Exception e) {
			throw new PythonParsingException("Failure when trying to compile pattern in Python: " + pattern);
		}

	}

	public static FeatureCount getFeatureCount(String pattern, String rawPattern) {
		return new FeatureCount(new PCRE(rawPattern).getCommonTree(), pattern);
	}

	// // some quick tests done here
	// public static void main(String[] args) throws QuoteRuleException {
	// String r = "'^(boot(\\\\.\\\\d+)?$|kernel\\\\.)'";
	// System.out.println(r);
	// System.out.println(getUnquotedPythonPattern(r));
	// }

}
