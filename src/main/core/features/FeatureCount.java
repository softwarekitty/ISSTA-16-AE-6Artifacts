package main.core.features;

import java.util.Arrays;

/**
 * represents the number of each feature present
 * 
 * @author cc
 */
public class FeatureCount {
	private final int[] featureCountArray;

	/**
	 * creates a count of features from an array
	 * 
	 * Unless there is a good reason not to, FeatureCount objects should be
	 * created using the FeatureCountFactory.
	 * 
	 * @param arrayCountingFeatures
	 * 
	 *            an array with length equal to the number of features in the
	 *            featureDictionary. Each array value represents the number of
	 *            features corresponding to the index for that feature in the
	 *            featureDictionary.
	 * 
	 * @param featureDictionary
	 *            the dictionary used to check that the number of features
	 *            (array length) is correct
	 */
	public FeatureCount(int[] arrayCountingFeatures, FeatureDictionary featureDictionary) {
		int correctLength = featureDictionary.getSize();
		int actualLength = arrayCountingFeatures.length;
		if (actualLength != correctLength) {
			throw new IllegalArgumentException(
					"array must have length: " + correctLength + " but has length: " + actualLength);
		}
		for (int i = 0; i < correctLength; i++) {
			int countValue = arrayCountingFeatures[i];
			if (countValue < 0) {
				throw new IllegalArgumentException("feature counts must be positive or zero, but at index: " + i
						+ " the value given is: " + countValue);
			}
		}
		this.featureCountArray = arrayCountingFeatures;
	}

	/**
	 * returns a defensive copy of the featureCountArray
	 * @return a defensive copy of the featureCountArray
	 */
	public int[] getFeatureCountArray() {
		int length = featureCountArray.length;
		int[] copy = new int[length];
		System.arraycopy(featureCountArray, 0, copy, 0, length);
		return copy;
	}

	////////////////// hashcode, string and equals////////////////

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(featureCountArray);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FeatureCount other = (FeatureCount) obj;
		if (!Arrays.equals(featureCountArray, other.featureCountArray))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FeatureCount [featureCountArray=" + Arrays.toString(featureCountArray) + "]";
	}
}
