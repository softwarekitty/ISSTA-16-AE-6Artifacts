package test.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import main.core.RegexProjectSet;
import main.io.LoadUtil;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;

public final class LoadUtilTest {
	
	static RegexProjectSet regexOne;
	static String regexOneString = "'\\\\s*([^: ]*)\\\\s*:(.*)'";
	static String regexOneCSV = "306,322,464,467,505,534,1300,1404,1466";

	@BeforeClass
	public static void setup() throws IllegalArgumentException, QuoteRuleException, PythonParsingException{
		TreeSet<Integer> PIDs0 = new TreeSet<Integer>();
		PIDs0.addAll(CSV_To_IntList(regexOneCSV));
		regexOne = new RegexProjectSet(regexOneString,PIDs0);
	}

	@Test
	public void test_loadRegexProjectSetInput_oneRegex() throws IllegalArgumentException, IOException, QuoteRuleException, PythonParsingException {
		LinkedList<String> lines = new LinkedList<String>();
		lines.add(regexOneString+"\t"+regexOneCSV);
		TreeSet<RegexProjectSet> loaded = LoadUtil.loadRegexProjectSetInput(lines);
		assertEquals(loaded.size(),1);
		RegexProjectSet first = loaded.first();
		assertEquals(first,regexOne);
	}
	
	private static List<Integer> CSV_To_IntList(String csv){
		List<Integer> ints = new LinkedList<Integer>();
		String[] tokens = csv.split(",");
		for(String s : tokens){
			ints.add(Integer.parseInt(s));
		}
		return ints;
	}
}
