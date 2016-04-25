package step1;

/**
 * plain old java object, used to help build the feature table
 * 
 * @author cc
 *
 */
public class FeatureDetail implements Comparable<FeatureDetail> {
	private final int featureID;
	private final int nProjects;
	private final int nFiles;
	private final int nPresent;
	private final int max;
	private final int nTokens;

	public FeatureDetail(int featureID, int nFiles, int nPresent, int nProjects, int max, int nTokens) {
		this.featureID = featureID;
		this.nProjects = nProjects;
		this.nFiles = nFiles;
		this.nPresent = nPresent;
		this.max = max;
		this.nTokens = nTokens;
	}

	public int getNProjectsHavingFeature() {
		return nProjects;
	}

	@Override
	public int compareTo(FeatureDetail other) {
		if (this.nProjects > other.nProjects) {
			return -1;
		} else if (this.nProjects < other.nProjects) {
			return 1;
		} else if (this.featureID > other.featureID) {
			return -1;
		} else if (this.featureID < other.featureID) {
			return 1;
		} else {
			return 0;
		}
	}

	public int getID() {
		return featureID;
	}

	public int getnFiles() {
		return nFiles;
	}

	public int getnPresent() {
		return nPresent;
	}

	public int getMax() {
		return max;
	}

	public int getnTokens() {
		return nTokens;
	}

	@Override
	public String toString() {
		return "FeatureDetail [featureID=" + featureID + ", nProjects=" + nProjects + ", nFiles=" + nFiles
				+ ", nPresent=" + nPresent + ", max=" + max + ", nTokens=" + nTokens + "]";
	}
}
