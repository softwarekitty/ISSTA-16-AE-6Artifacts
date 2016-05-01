package test.core.features;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import main.core.features.FeatureCount;
import main.core.features.FeatureDictionary;

public final class FeatureCountTest {
	private static FeatureDictionary fd;
	
	@Before
	public void setup(){
		fd = new FeatureDictionary();
	}
	
	@Test
	public void test_canCreateWithCorrectSizeArray() {
		
		// create feature count with all set to 1
		int[] counts = new int[fd.getSize()];
		Arrays.fill(counts, 1);
		FeatureCount fc = new FeatureCount(counts,fd);
		assertNotNull(fc);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_cannotCreateWithSmallArray() {
		
		// create feature count with all set to 1, but array is too small
		int[] counts = new int[fd.getSize()-1];
		Arrays.fill(counts, 1);
		FeatureCount fc = new FeatureCount(counts,fd);	
		assertNull(fc);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_cannotCreateWithLargeArray() {
		
		// create feature count with all set to 1, but array is too large
		int[] counts = new int[fd.getSize()+1];
		Arrays.fill(counts, 1);
		FeatureCount fc = new FeatureCount(counts,fd);	
		assertNull(fc);
	}
	
	@Test
	public void test_cannotModifyArray() {
		
		// create feature count with all set to 1
		int[] counts = new int[fd.getSize()];
		Arrays.fill(counts, 1);
		FeatureCount fc = new FeatureCount(counts,fd);
		
		// now try to modify the array, setting all to 0
		int[] shouldBeACopy = fc.getFeatureCountArray();
		Arrays.fill(shouldBeACopy, 0);
		assertFalse(Arrays.equals(shouldBeACopy, fc.getFeatureCountArray()));		
	}
}
