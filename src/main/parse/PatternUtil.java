package main.parse;

//TODO - TEST ME, PLEEASE
public class PatternUtil {

	public static String getUnescaped(String unquotedPattern) {
		return unquotedPattern.replaceAll("\\\\\\\\", "\\\\");
	}

	public static String getUnquotedPythonPattern(String pat) throws QuoteRuleException {

		// python can do: u'...', ur'...', r'...'
		String removedUR = pat.startsWith("ur") ? pat.substring(2) : pat;
		if (removedUR.length() == 0) {
			throw new QuoteRuleException("the pattern: " + pat + " does not conform to the expected quotation rules");
		}
		String removedPrefix = (removedUR.startsWith("u") || removedUR.startsWith("r")) ? removedUR.substring(1)
				: removedUR;
		
		// a single char, or empty string cannot have balanced delimiters
		if (removedPrefix.length() < 2) {
			throw new QuoteRuleException("the pattern: " + pat + " does not conform to the expected quotation rules");
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
}
