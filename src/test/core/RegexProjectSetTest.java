package test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import main.core.RegexProjectSet;
import main.core.features.AlienFeatureException;
import main.core.features.FeatureCount;
import main.core.features.FeatureDictionary;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;

public final class RegexProjectSetTest {
	private static List<TreeSet<Integer>> pidList;
	private static Random gen;

	@BeforeClass
	public static void setup() {
		gen = new Random(Integer.MAX_VALUE);
		pidList = new LinkedList<TreeSet<Integer>>();
		for (int i = 0; i < 34; i++) {
			int nProjectsToCreate = gen.nextInt(34) + 1;
			TreeSet<Integer> toyProjectIDs = new TreeSet<Integer>();
			while (toyProjectIDs.size() < nProjectsToCreate) {
				toyProjectIDs.add(gen.nextInt(512));
			}
			pidList.add(toyProjectIDs);
		}
	}

	@Test
	public void test_basicRegex1() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("'ab'", pidList.get(0));
		assertNotNull(rps);
		assertEquals("'ab'",rps.getPattern());
		assertEquals(pidList.get(0),rps.getProjectIDSet());
	}
	
	@Test
	public void test_basicRegex2() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("'[0-9]'", pidList.get(1));
		assertNotNull(rps);
		assertEquals("'[0-9]'",rps.getPattern());
		assertEquals(pidList.get(1),rps.getProjectIDSet());
	}
	
	@Test
	public void test_basicRegex3() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("\"(\\\\\\\\+)\"", pidList.get(2));
		assertNotNull(rps);
		assertEquals("\"(\\\\\\\\+)\"",rps.getPattern());
		assertEquals(pidList.get(2),rps.getProjectIDSet());
	}
	
	@Test(expected = PythonParsingException.class)
	public void test_invalidPythonRegex1() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		new RegexProjectSet("'ab('", pidList.get(0));
	}
	
	@Test(expected = PythonParsingException.class)
	public void test_invalidPythonRegex2() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		new RegexProjectSet("'[0-'", pidList.get(0));
	}
	
	@Test(expected = PythonParsingException.class)
	public void test_invalidPythonRegex3() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		new RegexProjectSet("\"\\\\\"", pidList.get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_emptyPattern() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		new RegexProjectSet("", pidList.get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_nullPattern() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		new RegexProjectSet(null, pidList.get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_emptyProjectIDSet() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		TreeSet<Integer> emptyPIDs = new TreeSet<Integer>();
		new RegexProjectSet("'ab'", emptyPIDs);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_nullProjectIDSet() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		new RegexProjectSet("'ab'", null);
	}

	@Test(expected = QuoteRuleException.class)
	public void test_invalidQuotesMissing() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("ab", pidList.get(0));
		assertNull(rps);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_invalidQuotesMismatched1() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("'ab\"", pidList.get(0));
		assertNull(rps);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_invalidQuotesMismatched2() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("\"ab'", pidList.get(0));
		assertNull(rps);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_invalidQuotesMismatched3() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("\"\"\"ab'", pidList.get(0));
		assertNull(rps);
	}
	
	@Test(expected = QuoteRuleException.class)
	public void test_invalidQuotesMismatched4() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("\"ab'''", pidList.get(0));
		assertNull(rps);
	}

	@Test
	public void test_validTrippleQuotes1() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("'''ab'''", pidList.get(0));
		assertNotNull(rps);
	}
	
	@Test
	public void test_validTrippleQuotes2() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("\"\"\"ab\"\"\"", pidList.get(0));
		assertNotNull(rps);
	}

	@Test(expected = AlienFeatureException.class)
	public void test_breaksPCREParser_Unicode() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("'[\\n \\u200b]'", pidList.get(0));
		assertNull(rps);
	}

	@Test(expected = AlienFeatureException.class)
	public void test_unsupportedFeatures() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("'(?P<g1>a)(?P<g2>b)?((?(g2)c|d))'", pidList.get(0));
		assertNull(rps);
	}

	@Test
	public void test_features1() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		Map<Integer,Integer> expectedFeatures = new HashMap<Integer,Integer>();
		expectedFeatures.put(FeatureDictionary.I_CC_DECIMAL, 3);
		expectedFeatures.put(FeatureDictionary.I_REP_ADDITIONAL, 1);
		expectedFeatures.put(FeatureDictionary.I_META_CC, 1);
		expectedFeatures.put(FeatureDictionary.I_META_CAPTURING_GROUP, 2);
		expectedFeatures.put(FeatureDictionary.I_CC_RANGE, 1);
		expectedFeatures.put(FeatureDictionary.I_META_LITERAL, 6);
		expectedFeatures.put(FeatureDictionary.I_REP_KLEENISH, 1);
		FeatureCount expectedFeatureCount = new FeatureCount(expectedFeatures);
		RegexProjectSet rps = new RegexProjectSet("'abc(\\\\d\\\\d(\\\\d+[e-y]*z))'", pidList.get(0));
		assertArrayEquals(expectedFeatureCount.getFeatureCountArray(),rps.getFeatures().getFeatureCountArray());
	}
	
	
	@Test
	public void test_rawPattern() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps = new RegexProjectSet("\"\\\\\\\\abc\\\\d\"", pidList.get(0));
		assertEquals(rps.getRawPattern(),"\\\\abc\\d");
	}
	

	@Test
	public void test_compareTo_equals() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps1 = new RegexProjectSet("\"\\\\\\\\abc\\\\d\"", pidList.get(0));
		RegexProjectSet rps2 = new RegexProjectSet("\"\\\\\\\\abc\\\\d\"", pidList.get(0));
		int i = rps1.compareTo(rps2);
		assertEquals(i,0);
	}
	
	@Test
	public void test_compareTo_sampeProjects_differentPatterns() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps1 = new RegexProjectSet("\"abc\"", pidList.get(0));
		RegexProjectSet rps2 = new RegexProjectSet("\"\\\\\\\\abc\\\\d\"", pidList.get(0));
		int i = rps1.compareTo(rps2);
		assertEquals(i,-1);
	}
	
	@Test
	public void test_compareTo_differentNProjects() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		RegexProjectSet rps1 = new RegexProjectSet("\"abc\"", pidList.get(0));
		RegexProjectSet rps2 = new RegexProjectSet("\"abc\"", pidList.get(1));
		int i = rps1.compareTo(rps2);
		assertEquals(i,-1);
	}

}
