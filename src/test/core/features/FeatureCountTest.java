package test.core.features;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import main.core.features.FeatureCount;
import main.core.features.FeatureDictionary;

public final class FeatureCountTest {
	
	@Test
	public void test_canCreateWithCorrectSizeArray() {
		
		// create feature count with all set to 1
		FeatureDictionary fd = new FeatureDictionary();
		int[] counts = new int[fd.getSize()];
		FeatureCount fc = new FeatureCount(counts,fd);		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_cannotCreateWithSmallArray() {
		
		// create feature count with all set to 1
		FeatureDictionary fd = new FeatureDictionary();
		int[] counts = new int[fd.getSize()-1];
		FeatureCount fc = new FeatureCount(counts,fd);		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_cannotCreateWithLargeArray() {
		
		// create feature count with all set to 1
		FeatureDictionary fd = new FeatureDictionary();
		int[] counts = new int[fd.getSize()+1];
		FeatureCount fc = new FeatureCount(counts,fd);		
	}
	
	@Test
	public void test_cannotModifyArray() {
		
		// create feature count with all set to 1
		FeatureDictionary fd = new FeatureDictionary();
		int[] counts = new int[fd.getSize()];
		for(int i=0;i<counts.length;i++){
			counts[i] = 1;
		}
		FeatureCount fc = new FeatureCount(counts,fd);
		
		// now try to modify the array, setting all to 0
		int[] shouldBeACopy = fc.getFeatureCountArray();
		for(int i=0;i<counts.length;i++){
			shouldBeACopy[i] = 0;
		}
		assertFalse(Arrays.equals(shouldBeACopy, fc.getFeatureCountArray()));		
	}
}
