package test.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import main.parse.PatternUtil;
import main.parse.QuoteRuleException;

public final class PatternUtilTest {

	@BeforeClass
	public static void setup(){
		// TODO -setup
	}

	@Test
	public void test_getUnescaped_slashPair() {
		String before = "\\\\d";
		String expected = "\\d";
		String actual = PatternUtil.getUnescaped(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnescaped_OneSlash() {
		String before = "\\d";
		String expected = "\\d";
		String actual = PatternUtil.getUnescaped(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_singleQuotes() throws QuoteRuleException {
		String before = "'a'";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_doubleQuotes() throws QuoteRuleException {
		String before = "\"a\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleSingleQuotes() throws QuoteRuleException {
		String before = "'''a'''";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleDoubleQuotes() throws QuoteRuleException {
		String before = "\"\"\"a\"\"\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_singleQuotes_u() throws QuoteRuleException {
		String before = "u'a'";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_doubleQuotes_u() throws QuoteRuleException {
		String before = "u\"a\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleSingleQuotes_u() throws QuoteRuleException {
		String before = "u'''a'''";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleDoubleQuotes_u() throws QuoteRuleException {
		String before = "u\"\"\"a\"\"\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_singleQuotes_ur() throws QuoteRuleException {
		String before = "ur'a'";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_doubleQuotes_ur() throws QuoteRuleException {
		String before = "ur\"a\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleSingleQuotes_ur() throws QuoteRuleException {
		String before = "ur'''a'''";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleDoubleQuotes_ur() throws QuoteRuleException {
		String before = "ur\"\"\"a\"\"\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_singleQuotes_r() throws QuoteRuleException {
		String before = "r'a'";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_doubleQuotes_r() throws QuoteRuleException {
		String before = "r\"a\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleSingleQuotes_r() throws QuoteRuleException {
		String before = "r'''a'''";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleDoubleQuotes_r() throws QuoteRuleException {
		String before = "r\"\"\"a\"\"\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_singleQuotes_innerDouble() throws QuoteRuleException {
		String before = "'abb\"ccc'";
		String expected = "abb\"ccc";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_singleQuotes_innerSingle() throws QuoteRuleException {
		String before = "'abb\\'ccc'";
		String expected = "abb\\'ccc";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_1char() throws QuoteRuleException {
		String before = "a";
		String expected = "";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_0char() throws QuoteRuleException {
		String before = "";
		String expected = "";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_noQuotes_7chars() throws QuoteRuleException {
		String before = "abcdefg";
		String expected = "";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_doubleQuotes_mismatch() throws QuoteRuleException {
		String before = "'a\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleSingleQuotes_mismatch() throws QuoteRuleException {
		String before = "'''a''";
		String expected = "''a'";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_trippleDoubleQuotes_missing() throws QuoteRuleException {
		String before = "\"\"\"a";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_singleQuotes_u_missing() throws QuoteRuleException {
		String before = "u'a";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_doubleQuotes_u_missing() throws QuoteRuleException {
		String before = "ua\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test
	public void test_getUnquoted_trippleSingleQuotes_u_OKmismatch() throws QuoteRuleException {
		String before = "u'''a'";
		String expected = "''a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_trippleDoubleQuotes_u_mismatch() throws QuoteRuleException {
		String before = "u\"\"\"a''";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_singleQuotes_ur_mismatch() throws QuoteRuleException {
		String before = "ur'a\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_doubleQuotes_ur_mismatch() throws QuoteRuleException {
		String before = "ur'a\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_trippleSingleQuotes_ur_mismatch() throws QuoteRuleException {
		String before = "ur'''a\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_trippleDoubleQuotes_ur_mismatch() throws QuoteRuleException {
		String before = "ur'''a\"\"\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_singleQuotes_r_mismatch() throws QuoteRuleException {
		String before = "r'a\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_doubleQuotes_r_mismatch() throws QuoteRuleException {
		String before = "r\"a'";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_trippleSingleQuotes_r_mismatch() throws QuoteRuleException {
		String before = "r'''a\"\"\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_trippleDoubleQuotes_r_mismatch() throws QuoteRuleException {
		String before = "r'''a\"\"\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_singleQuotes_innerDouble_mismatch() throws QuoteRuleException {
		String before = "\"abb\"ccc'";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_getUnquoted_singleQuotes_innerSingle_mismatch() throws QuoteRuleException {
		String before = "'abb\\'ccc\"";
		String expected = "a";
		String actual = PatternUtil.getUnquotedPythonPattern(before);
		assertEquals(expected,actual);
	}
	
}
//	public static void validatePythonRegex(String pattern) throws PythonParsingException {
//		try {
//
//			// make sure the pattern is a valid regex
//			PythonInterpreter interpreter = new PythonInterpreter();
//			interpreter.exec("import re");
//			interpreter.exec("x = re.compile(" + pattern + ")");
//		} catch (Exception e) {
//			throw new PythonParsingException("Failure when trying to compile pattern in Python: " + pattern);
//		}
//
//	}
//
//	public static FeatureCount getFeatureCount(String pattern, String rawPattern) {
//		return new FeatureCount(new PCRE(rawPattern).getCommonTree(), pattern);
//	}
//
//	// // some quick tests done here
//	// public static void main(String[] args) throws QuoteRuleException {
//	// String r = "'^(boot(\\\\.\\\\d+)?$|kernel\\\\.)'";
//	// System.out.println(r);
//	// System.out.println(getUnquotedPythonPattern(r));
//	// }
//
//}
