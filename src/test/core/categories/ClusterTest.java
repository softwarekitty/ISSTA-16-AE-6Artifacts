package test.core.categories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import main.core.RegexProjectSet;
import main.core.categories.Cluster;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;

public final class ClusterTest {
	static RegexProjectSet regex1;
	static List<RegexProjectSet> c7List;
	static List<RegexProjectSet> c12List;
	static List<TreeSet<Integer>> c7PIDs;
	static List<TreeSet<Integer>> c12PIDs;

	@BeforeClass
	public static void setup() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		regex1 = CategoryTestFixtures.getCluster12_RegexProjectSets().get(0);
		c7List = CategoryTestFixtures.getCluster7_RegexProjectSets();
		c12List = CategoryTestFixtures.getCluster12_RegexProjectSets();
		c7PIDs = CategoryTestFixtures.getC7PIDs();
		c12PIDs = CategoryTestFixtures.getC12PIDs();

	}

	@Test
	public void test_init() {
		Cluster c = new Cluster();
		assertNotNull(c);
	}

	@Test
	public void test_getHeaviest_empty() {
		Cluster c = new Cluster();
		RegexProjectSet null_rps = c.getHeaviest();
		assertNull(null_rps);
	}

	@Test
	public void test_getHeaviest_hasOne() {
		Cluster c = new Cluster();
		c.add(regex1);
		RegexProjectSet one_rep = c.getHeaviest();
		assertEquals(one_rep, regex1);
	}

	@Test
	public void test_getHeaviest_hasC7() {
		Cluster c = new Cluster();
		c.addAll(c7List);
		RegexProjectSet rep = c.getHeaviest();
		RegexProjectSet actualHeaviest = CategoryTestUtil.determineHeaviest(c7List);
		assertNotNull(actualHeaviest);
		assertEquals(rep, actualHeaviest);
	}

	@Test
	public void test_getHeaviest_hasC12() {
		Cluster c = new Cluster();
		c.addAll(c12List);
		RegexProjectSet rep = c.getHeaviest();
		RegexProjectSet actualHeaviest = CategoryTestUtil.determineHeaviest(c12List);
		assertNotNull(actualHeaviest);
		assertEquals(rep, actualHeaviest);
	}

	@Test
	public void test_geShortest_empty() {
		Cluster c = new Cluster();
		RegexProjectSet null_rps = c.getShorty();
		assertNull(null_rps);
	}

	@Test
	public void test_getShortest_hasOne() {
		Cluster c = new Cluster();
		c.add(regex1);
		RegexProjectSet one_rep = c.getShorty();
		assertEquals(one_rep, regex1);
	}

	@Test
	public void test_getShortest_hasC7() {
		Cluster c = new Cluster();
		c.addAll(c7List);
		RegexProjectSet rep = c.getShorty();
		RegexProjectSet actualShortest = CategoryTestUtil.determineShortest(c7List);
		assertNotNull(actualShortest);
		assertEquals(rep, actualShortest);
	}

	@Test
	public void test_getShortest_hasC12() {
		Cluster c = new Cluster();
		c.addAll(c12List);
		RegexProjectSet rep = c.getShorty();
		RegexProjectSet actualShortest = CategoryTestUtil.determineShortest(c12List);
		assertNotNull(actualShortest);
		assertEquals(rep, actualShortest);
	}

	@Test
	public void test_getProjectIDs_empty() {
		Cluster c = new Cluster();
		TreeSet<Integer> empty_PIDs = c.getAllProjectIDs();
		TreeSet<Integer> newTreeSet = new TreeSet<Integer>();
		assertEquals(empty_PIDs, newTreeSet);
	}

	@Test
	public void test_getProjectIDs_hasOne() {
		Cluster c = new Cluster();
		c.add(regex1);
		TreeSet<Integer> regex1_PIDs = regex1.getProjectIDSet();
		TreeSet<Integer> combined = c.getAllProjectIDs();
		assertEquals(combined, regex1_PIDs);
	}

	@Test
	public void test_getProjectIDs_hasC7() {
		Cluster c = new Cluster();
		c.addAll(c7List);
		TreeSet<Integer> actual_c7PIDs_combined = CategoryTestUtil.combinePIDs(c7PIDs);
		TreeSet<Integer> combined = c.getAllProjectIDs();
		assertEquals(actual_c7PIDs_combined, combined);
	}

	@Test
	public void test_getProjectIDs_hasC12() {
		Cluster c = new Cluster();
		c.addAll(c12List);
		TreeSet<Integer> actual_c12PIDs_combined = CategoryTestUtil.combinePIDs(c12PIDs);
		TreeSet<Integer> combined = c.getAllProjectIDs();
		assertEquals(actual_c12PIDs_combined, combined);
	}

	@Test
	public void test_getNProjects_empty() {
		Cluster c = new Cluster();
		assertEquals(c.getNProjects(), 0);
	}

	@Test
	public void test_getNProjects_hasOne() {
		Cluster c = new Cluster();
		c.add(regex1);
		assertEquals(c.getNProjects(), regex1.getNProjects());
	}

	@Test
	public void test_getNProjects_hasC7() {
		Cluster c = new Cluster();
		c.addAll(c7List);
		TreeSet<Integer> actual_c7PIDs_combined = CategoryTestUtil.combinePIDs(c7PIDs);
		assertEquals(c.getNProjects(), actual_c7PIDs_combined.size());
	}

	@Test
	public void test_getNProjects_hasC12() {
		Cluster c = new Cluster();
		c.addAll(c12List);
		TreeSet<Integer> actual_c12PIDs_combined = CategoryTestUtil.combinePIDs(c12PIDs);
		assertEquals(c.getNProjects(), actual_c12PIDs_combined.size());
	}

	@Test
	public void test_getNPatterns_empty() {
		Cluster c = new Cluster();
		assertEquals(c.getNPatterns(), 0);
	}

	@Test
	public void test_getNPatterns_hasOne() {
		Cluster c = new Cluster();
		c.add(regex1);
		assertEquals(c.getNPatterns(), 1);
	}

	@Test
	public void test_getNPatterns_hasC7() {
		Cluster c = new Cluster();
		c.addAll(c7List);
		assertEquals(c.getNPatterns(), c7List.size());
	}

	@Test
	public void test_getNPatterns_hasC12() {
		Cluster c = new Cluster();
		c.addAll(c12List);
		assertEquals(c.getNPatterns(), c12List.size());
	}
}