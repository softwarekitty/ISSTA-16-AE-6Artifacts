package test.core.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Random;

import org.junit.Test;

import main.core.features.FeatureCount;
import main.core.features.FeatureCountFactory;
import main.core.features.FeatureDictionary;

public final class FeatureCountFactoryTest {
	FeatureDictionary fd = new FeatureDictionary();
	Random gen = new Random(Integer.MAX_VALUE);

	@Test
	public void test_init_emptyMap() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount(new HashMap<Integer, Integer>());
		assertNotNull(fc);
	}

	@Test
	public void test_init_eachKeyOne_and_allKeysOne() {
		HashMap<Integer, Integer> allKeysOne = new HashMap<Integer, Integer>();
		for (int i = 0; i < fd.getSize(); i++) {
			HashMap<Integer, Integer> eachKeyOne = new HashMap<Integer, Integer>();
			eachKeyOne.put(i, 1);
			allKeysOne.put(i, 1);
			FeatureCount fc_each = FeatureCountFactory.getFeatureCount(eachKeyOne);
			assertNotNull(fc_each);
			int[] fc_each_values = fc_each.getFeatureCountArray();

			// check that the fc has i set to 1
			assertEquals(fc_each_values[i], 1);

			// and that the rest are zero
			for (int j = 0; j < fd.getSize(); j++) {
				if (j != i) {
					assertEquals(fc_each_values[j], 0);
				}
			}
		}
		FeatureCount fcAll = FeatureCountFactory.getFeatureCount(allKeysOne);
		int[] fc_all_values = fcAll.getFeatureCountArray();
		for (int j = 0; j < fd.getSize(); j++) {
			assertEquals(fc_all_values[j], 1);
		}
	}

	@Test
	public void test_init_eachKeyPos_and_allKeysPos() {
		HashMap<Integer, Integer> allKeysPos = new HashMap<Integer, Integer>();
		int[] allValues = new int[fd.getSize()];

		for (int i = 0; i < fd.getSize(); i++) {
			int eachPos = gen.nextInt(Integer.MAX_VALUE);
			int allPos = gen.nextInt(Integer.MAX_VALUE);
			allValues[i] = allPos;

			HashMap<Integer, Integer> eachKeyPos = new HashMap<Integer, Integer>();
			eachKeyPos.put(i, eachPos);
			allKeysPos.put(i, allPos);
			FeatureCount fc_each = FeatureCountFactory.getFeatureCount(eachKeyPos);
			assertNotNull(fc_each);
			int[] fc_each_values = fc_each.getFeatureCountArray();

			// check that the fc has i set to whatever positive int
			assertEquals(fc_each_values[i], eachPos);

			// and that the rest are zero
			for (int j = 0; j < fd.getSize(); j++) {
				if (j != i) {
					assertEquals(fc_each_values[j], 0);
				}
			}
		}
		FeatureCount fcAll = FeatureCountFactory.getFeatureCount(allKeysPos);
		int[] fc_all_values = fcAll.getFeatureCountArray();
		for (int j = 0; j < fd.getSize(); j++) {
			assertEquals(fc_all_values[j], allValues[j]);
		}
	}

	@Test
	public void test_init_TooManyKeysOK() {
		int putEverywhere = 1;
		HashMap<Integer, Integer> oneTooManyMap = new HashMap<Integer, Integer>();
		for (int i = 0; i <= fd.getSize(); i++) {
			oneTooManyMap.put(i, putEverywhere);
		}
		FeatureCount fc = FeatureCountFactory.getFeatureCount(oneTooManyMap);
		assertNotNull(fc);
		int[] fc_values = fc.getFeatureCountArray();
		for (int j = 0; j < fd.getSize(); j++) {
			assertEquals(fc_values[j], putEverywhere);
		}
	}

	@Test
	public void test_init_fromList() {

		// this would be the same as the raw pattern: [a\d]
		FeatureCount fc = FeatureCountFactory.getFeatureCount(FeatureDictionary.I_META_LITERAL,
				FeatureDictionary.I_CC_DECIMAL, FeatureDictionary.I_META_CC);
		int[] fc_values = fc.getFeatureCountArray();
		assertEquals(fc_values[FeatureDictionary.I_META_LITERAL], 1);
		assertEquals(fc_values[FeatureDictionary.I_CC_DECIMAL], 1);
		assertEquals(fc_values[FeatureDictionary.I_META_CC], 1);
	}
	
	//////// tests below here rely on functionality tested above /////////

	@Test
	public void test_rawPattern_LIT() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("abcd");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,4);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_CG() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("(ab)cd");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,4);
		controlMap.put(FeatureDictionary.I_META_CAPTURING_GROUP,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}

	@Test
	public void test_rawPattern_ADD() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("a+bcd");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,4);
		controlMap.put(FeatureDictionary.I_REP_ADDITIONAL,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_KLE() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("a*bc*d");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,4);
		controlMap.put(FeatureDictionary.I_REP_KLEENISH,2);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_ANY() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("....");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_DOT_ANY,4);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_CCC() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("a[bc]d");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,4);
		controlMap.put(FeatureDictionary.I_META_CC,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_RNG() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("[a-c]d");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,3);
		controlMap.put(FeatureDictionary.I_META_CC,1);
		controlMap.put(FeatureDictionary.I_CC_RANGE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_STR() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("^abc");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,3);
		controlMap.put(FeatureDictionary.I_POS_START_ANCHOR,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_END() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("abc$");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,3);
		controlMap.put(FeatureDictionary.I_POS_END_ANCHOR,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	/// fhix hre
	
	@Test
	public void test_rawPattern_NCCC() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("[^ab]c");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,3);
		controlMap.put(FeatureDictionary.I_META_NCC,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_WSP() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("\\s\\s\\s");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_CC_WHITESPACE,3);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_OR() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("a|b");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,2);
		controlMap.put(FeatureDictionary.I_META_OR,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_DEC() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("\\d\\.\\d");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_CC_DECIMAL,2);
		controlMap.put(FeatureDictionary.I_META_LITERAL,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_WRD() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("\\w\\w");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_CC_WORD,2);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_QST() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("1?");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,1);
		controlMap.put(FeatureDictionary.I_REP_QUESTIONABLE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_LZY() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("\\w+?");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_CC_WORD,1);
		controlMap.put(FeatureDictionary.I_REP_ADDITIONAL,1);
		controlMap.put(FeatureDictionary.I_REP_LAZY,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_NCG() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("(?:axb)");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,3);
		controlMap.put(FeatureDictionary.I_LOOK_NON_CAPTURE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_PNG() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("(?P<name>\\s)");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,1);
		controlMap.put(FeatureDictionary.I_CC_WHITESPACE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_SNG() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("z{8}");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,1);
		controlMap.put(FeatureDictionary.I_REP_SINGLEEXACTLY,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_NWSP() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("\\S");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_CC_NWHITESPACE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_DBB() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("z{3,8}");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,1);
		controlMap.put(FeatureDictionary.I_REP_DOUBLEBOUNDED,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_NLKA() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("a(?!zy)");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,3);
		controlMap.put(FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_WNW() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("\\babc\\b");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,3);
		controlMap.put(FeatureDictionary.I_POS_WORD,2);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_NWRD() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("\\W");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_CC_NWORD,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_LWB() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("ab{3,}");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,2);
		controlMap.put(FeatureDictionary.I_REP_LOWERBOUNDED,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_LKA() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("a(?=bc)");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,3);
		controlMap.put(FeatureDictionary.I_LOOK_AHEAD,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_OPT() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("(?i)CaSe");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,4);
		controlMap.put(FeatureDictionary.I_XTRA_OPTIONS,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_NLKB() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("(?<!x)z");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,2);
		controlMap.put(FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_LKB() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("(?<=a)z");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,2);
		controlMap.put(FeatureDictionary.I_LOOK_BEHIND,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_ENDZ() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("x\\Z");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,1);
		controlMap.put(FeatureDictionary.I_XTRA_END_SUBJECTLINE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_BKR() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("(a+)b\\1");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,2);
		controlMap.put(FeatureDictionary.I_REP_ADDITIONAL,1);
		controlMap.put(FeatureDictionary.I_META_CAPTURING_GROUP,1);
		controlMap.put(FeatureDictionary.I_META_NUMBERED_BACKREFERENCE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_NDEC() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("\\D\\D\\D\\D");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_CC_NDECIMAL,4);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_BKRN() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("(?P<name>x+)y(?P=name)");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,2);
		controlMap.put(FeatureDictionary.I_REP_ADDITIONAL,1);
		controlMap.put(FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,1);
		controlMap.put(FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_VWSP() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("xy\\v");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,2);
		controlMap.put(FeatureDictionary.I_XTRA_VERTICAL_WHITESPACE,1);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
	@Test
	public void test_rawPattern_NWNW() {
		FeatureCount fc = FeatureCountFactory.getFeatureCount("\\BinnerWord\\B");
		HashMap<Integer,Integer> controlMap = new HashMap<Integer,Integer>();
		controlMap.put(FeatureDictionary.I_META_LITERAL,9);
		controlMap.put(FeatureDictionary.I_POS_NONWORD,2);
		FeatureCount control = FeatureCountFactory.getFeatureCount(controlMap);
		assertEquals(control,fc);
	}
	
}
