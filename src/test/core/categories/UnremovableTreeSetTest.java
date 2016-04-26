package test.core.categories;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import main.core.categories.UnremovableTreeSet;

public final class UnremovableTreeSetTest {
	
	private static UnremovableTreeSet<Integer> noRemovesPlease;
	
	@Before
	public void setup(){
		noRemovesPlease = new UnremovableTreeSet<Integer>();
		noRemovesPlease.add(1);
		noRemovesPlease.add(2);
		noRemovesPlease.add(3);
	}

	@Test
	public void test_can_foreach() {
		int sum = 0;
		for (Integer i : noRemovesPlease) {
			sum += i;
		}
		assertEquals(sum,6);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void test_cannot_remove_by_iterator() {
		Iterator<Integer> it = noRemovesPlease.iterator();
		while (it.hasNext()) {
			it.next();

			// should throw here
			it.remove();
		}
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void test_cannot_remove_by_clear() {
		noRemovesPlease.clear();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void test_cannot_remove_by_pollFirst() {
		noRemovesPlease.pollFirst();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void test_cannot_remove_by_pollLast() {
		noRemovesPlease.pollLast();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void test_cannot_remove_by_remove() {
		noRemovesPlease.remove(new Integer(1));
	}
}