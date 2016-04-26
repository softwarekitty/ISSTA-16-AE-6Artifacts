package test.core.categories;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import main.core.RegexProjectSet;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;

public class CategoryTestFixtures {
	
	public static List<TreeSet<Integer>> getC12PIDs(){
		List<TreeSet<Integer>> c12PIDs = new LinkedList<TreeSet<Integer>>();
		TreeSet<Integer> PIDs0 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs1 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs2 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs3 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs4 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs5 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs6 = new TreeSet<Integer>();
		PIDs0.addAll(Arrays.asList(306,322,464,467,505,534,1300,1404,1466));
		PIDs1.addAll(Arrays.asList(12,412,759,852,961,1087,1221,1511));
		PIDs2.addAll(Arrays.asList(171,241,1249,1317,1381,1501));
		PIDs3.addAll(Arrays.asList(322,467,505,534,1216,1466));
		PIDs4.addAll(Arrays.asList(139,292,1219,1501));
		PIDs5.addAll(Arrays.asList(232,1338));
		PIDs6.addAll(Arrays.asList(103,130));
		c12PIDs.add(PIDs0);
		c12PIDs.add(PIDs1);
		c12PIDs.add(PIDs2);
		c12PIDs.add(PIDs3);
		c12PIDs.add(PIDs4);
		c12PIDs.add(PIDs5);
		c12PIDs.add(PIDs6);
		return c12PIDs;
	}
	
	public static List<TreeSet<Integer>> getC7PIDs(){
		List<TreeSet<Integer>> c7PIDs = new LinkedList<TreeSet<Integer>>();
		TreeSet<Integer> PIDs0 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs1 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs2 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs3 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs4 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs5 = new TreeSet<Integer>();
		TreeSet<Integer> PIDs6 = new TreeSet<Integer>();
		PIDs0.addAll(Arrays.asList(308,322,389,467,505,534,743,1216,1466));
		PIDs1.addAll(Arrays.asList(356,531,654,832,846));
		PIDs2.addAll(Arrays.asList(96,252,564,1059));
		PIDs3.addAll(Arrays.asList(1004,1074,1075,1495));
		PIDs4.addAll(Arrays.asList(1127,1380,1526));
		PIDs5.addAll(Arrays.asList(645,1007,1301));
		PIDs6.addAll(Arrays.asList(1317,1501));
		c7PIDs.add(PIDs0);
		c7PIDs.add(PIDs1);
		c7PIDs.add(PIDs2);
		c7PIDs.add(PIDs3);
		c7PIDs.add(PIDs4);
		c7PIDs.add(PIDs5);
		c7PIDs.add(PIDs6);
		return c7PIDs;
	}
	
	public static LinkedList<RegexProjectSet> getCluster12_RegexProjectSets() throws IllegalArgumentException, QuoteRuleException, PythonParsingException{
		List<TreeSet<Integer>> c12PIDs = getC12PIDs();
		LinkedList<RegexProjectSet> c12_rps = new LinkedList<RegexProjectSet>();
		c12_rps.add(new RegexProjectSet("'\\\\s*([^: ]*)\\\\s*:(.*)'",c12PIDs.get(0)));
		c12_rps.add(new RegexProjectSet("':+'",c12PIDs.get(1)));
		c12_rps.add(new RegexProjectSet("'(:)'",c12PIDs.get(1)));
		c12_rps.add(new RegexProjectSet("'(:+)'",c12PIDs.get(1)));
		c12_rps.add(new RegexProjectSet("'(:)(:*)'",c12PIDs.get(1)));
		c12_rps.add(new RegexProjectSet("'^([^:]*): *(.*)'",c12PIDs.get(1)));
		c12_rps.add(new RegexProjectSet("'[:]'",c12PIDs.get(2)));
		c12_rps.add(new RegexProjectSet("'([^:]+):(.*)'",c12PIDs.get(3)));
		c12_rps.add(new RegexProjectSet("'\\\\s*:\\\\s*'",c12PIDs.get(4)));
		c12_rps.add(new RegexProjectSet("'\\\\:'",c12PIDs.get(5)));
		c12_rps.add(new RegexProjectSet("'^([^:]*):[^:]*$'",c12PIDs.get(6)));
		c12_rps.add(new RegexProjectSet("'^[^:]*:([^:]*)$'",c12PIDs.get(6)));
		return c12_rps;
	}
	
	public static LinkedList<RegexProjectSet> getCluster7_RegexProjectSets() throws IllegalArgumentException, QuoteRuleException, PythonParsingException{
		List<TreeSet<Integer>> c7PIDs = getC7PIDs();
		LinkedList<RegexProjectSet> c7_rps = new LinkedList<RegexProjectSet>();
		c7_rps.add(new RegexProjectSet("'(\\\\d+\\\\.\\\\d+)(.*)'",c7PIDs.get(0)));
		c7_rps.add(new RegexProjectSet("'(\\\\d+\\\\.\\\\d+)'",c7PIDs.get(1)));
		c7_rps.add(new RegexProjectSet("'\\\\d+\\\\.\\\\d+'",c7PIDs.get(2)));
		c7_rps.add(new RegexProjectSet("'(\\\\d+)\\\\.(\\\\d+)(\\\\.(\\\\d+))?(b(\\\\d+))?'",c7PIDs.get(3)));
		c7_rps.add(new RegexProjectSet("'[0-9]+\\\\.[0-9]+'",c7PIDs.get(4)));
		c7_rps.add(new RegexProjectSet("'(\\\\d+)\\\\.(\\\\d+)'",c7PIDs.get(5)));
		c7_rps.add(new RegexProjectSet("'(grubby version)?(\\\\s)?(\\\\d+)\\\\.(\\\\d+)(.*)'",c7PIDs.get(6)));
		return c7_rps;
	}

}
