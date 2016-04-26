package test.core.categories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import main.core.RegexProjectSet;
import main.core.categories.Category;
import main.core.categories.Cluster;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;

public final class CategoryTest {

	static Cluster c12;
	static Cluster c7;

	@BeforeClass
	public static void setup() throws IllegalArgumentException, QuoteRuleException, PythonParsingException {
		c12 = new Cluster();
		c12.addAll(CategoryTestFixtures.getCluster12_RegexProjectSets());
		c7 = new Cluster();
		c7.addAll(CategoryTestFixtures.getCluster7_RegexProjectSets());
	}

	@Test
	public void test_init() {
		Category c = new Category();
		assertNotNull(c);
	}

	// assumes that regexes in a cluster are not in other clusters
	// TODO - enforce this assumption somewhere
	@Test
	public void test_categoryTotalPatterns() {
		Category c = new Category();
		assertEquals(c.categoryTotalPatterns(), 0);
		c.add(c12);
		assertEquals(c.categoryTotalPatterns(), c12.getNPatterns());
		c.add(c7);
		assertEquals(c.categoryTotalPatterns(), c12.getNPatterns() + c7.getNPatterns());
	}

	@Test
	public void test_categoryTotalProjects() {
		Category c = new Category();
		assertEquals(c.categoryTotalProjects(), 0);
		c.add(c12);
		assertEquals(c.categoryTotalProjects(), c12.getNProjects());
		c.add(c7);
		assertEquals(c.categoryTotalProjects(),
				CategoryTestUtil.combinePIDs(Arrays.asList(c12.getAllProjectIDs(), c7.getAllProjectIDs())).size());
	}
	
	@Test
	public void test_getRepresentative_false() {
		Category c = new Category();
		List<RegexProjectSet> combined = new LinkedList<RegexProjectSet>();
		
		RegexProjectSet null_rep = c.getRepresentative(false);
		assertNull(null_rep);
		
		c.add(c12);
		combined.addAll(c12);
		RegexProjectSet rep_from_c12 = c.getRepresentative(false);
		RegexProjectSet rep_from_combined1 = CategoryTestUtil.determineShortest(combined);
		assertEquals(rep_from_c12.getPattern().length(), rep_from_combined1.getPattern().length());
		
		c.add(c7);
		combined.addAll(c7);
		RegexProjectSet rep_from_c7 = c.getRepresentative(false);
		RegexProjectSet rep_from_combined2 = CategoryTestUtil.determineShortest(combined);
		assertEquals(rep_from_c7.getPattern().length(), rep_from_combined2.getPattern().length());
	}

	@Test
	public void test_getRepresentative_true() {
		Category c = new Category();
		List<RegexProjectSet> combined = new LinkedList<RegexProjectSet>();
		
		RegexProjectSet null_rep = c.getRepresentative(true);
		assertNull(null_rep);
		
		c.add(c12);
		combined.addAll(c12);
		RegexProjectSet rep_from_c12 = c.getRepresentative(true);
		RegexProjectSet rep_from_combined1 = CategoryTestUtil.determineHeaviest(combined);
		assertEquals(rep_from_c12.getNProjects(), rep_from_combined1.getNProjects());
		
		c.add(c7);
		combined.addAll(c7);
		RegexProjectSet rep_from_c7 = c.getRepresentative(true);
		RegexProjectSet rep_from_combined2 = CategoryTestUtil.determineHeaviest(combined);
		assertEquals(rep_from_c7.getNProjects(), rep_from_combined2.getNProjects());
	}
}
