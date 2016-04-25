package main.core.categories;

import main.core.RegexProjectSet;

/**
 * a Category is a set of clusters.
 * 
 * a Category also tracks all the regexes in all the clusters internally as
 * another Cluster
 * 
 * @author cc
 *
 */
public class Category extends UnremovableTreeSet<Cluster> {
	private static final long serialVersionUID = -217224086597240194L;
	Cluster combinedClusters;

	public Category() {
		super();
		combinedClusters = new Cluster();
	}

	@Override
	public boolean add(Cluster c) {
		boolean added = super.add(c);
		combinedClusters.addAll(c);
		return added;
	}

	public int categoryTotalPatterns() {
		return combinedClusters.getNPatterns();
	}

	public int categoryTotalProjects() {
		return combinedClusters.getNProjects();
	}

	public RegexProjectSet getRepresentative(boolean getHeaviest) {
		return getHeaviest ? combinedClusters.getHeaviest() : combinedClusters.getShorty();
	}
}
