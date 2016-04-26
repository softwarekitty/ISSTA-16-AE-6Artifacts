package main.core.categories;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import main.core.RegexProjectSet;

/**
 * A cluster is a set of regexes.
 * 
 * A cluster tracks the set of all projects that contain at least one of the
 * regexes it contains.
 * 
 * A cluster also tracks the shortest regex it contains.
 * 
 * For simplicity, at this time removing a regex from a cluster is not allowed,
 * but can be implemented later if needed.
 * 
 * @author cc
 **/
@SuppressWarnings("serial")
public class Cluster extends UnremovableTreeSet<RegexProjectSet> implements Comparable<Cluster> {
	private static int nextClusterID = 0;
	public final int thisClusterID = nextClusterID++;
	private TreeSet<Integer> allProjectIDs;
	private RegexProjectSet shortest = null;

	public Cluster() {
		allProjectIDs = new TreeSet<Integer>();
	}

	/**
	 * gets the heaviest regex in the Cluster
	 * 
	 * @return the heaviest regex or null if empty
	 */
	public RegexProjectSet getHeaviest() {
		if (this.isEmpty()) {
			System.err.println("empty cluster has no heaviest regex");
			return null;
		}
		return this.first();
	}

	/**
	 * gets the shortest regex in the Cluster
	 * 
	 * @return the shortest regex or null if empty
	 */
	public RegexProjectSet getShorty() {
		if (this.isEmpty()) {
			System.err.println("empty cluster has no shortest regex");
			return null;
		}
		return shortest;
	}

	/**
	 * gets a copy of the set of project IDs for projects that contain at least
	 * one regex in this cluster.
	 * 
	 * @return project IDs for this cluster.
	 */
	public TreeSet<Integer> getAllProjectIDs() {
		TreeSet<Integer> defensiveCopy = new TreeSet<Integer>();
		defensiveCopy.addAll(allProjectIDs);
		return defensiveCopy;
	}

	public int getNProjects() {
		return allProjectIDs.size();
	}

	public int getNPatterns() {
		return size();
	}

	// Overridden methods to maintain shortest and allProjectIDs //

	// on each add, update project set and shortest
	@Override
	public boolean add(RegexProjectSet x) {
		boolean addSuccess = super.add(x);
		allProjectIDs.addAll(x.getProjectIDSet());
		if (shortest == null) {
			shortest = x;
		} else if (x.getPattern().length() < shortest.getPattern().length()
				|| (x.getPattern().length() == shortest.getPattern().length()
						&& x.getNProjects() > shortest.getNProjects())) {
			shortest = x;
		}
		return addSuccess;
	}

	// uses the local add function to add all
	@Override
	public boolean addAll(Collection<? extends RegexProjectSet> elements) {
		boolean setIsChanged = false;
		Iterator<? extends RegexProjectSet> it = elements.iterator();
		while (it.hasNext()) {

			// call next() outside of the logical OR, be safe against looping
			RegexProjectSet current = it.next();
			setIsChanged = add(current) || setIsChanged;
		}
		return setIsChanged;
	}

	/*
	 * compares first by number of projects, then by number of patterns, and if
	 * both are the same, then if both are still the same, iterates through both
	 * same-sized sets, comparing each pair returned by the two iterators and
	 * using any non-zero results. If all are zero, then the sets are equal.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Cluster other) {
		if (other.getClass() != this.getClass()) {
			System.err.println("class mismatch");
			return 1;
		}
		Cluster cOther = (Cluster) other;
		int nProjectsThis = this.getNProjects();
		int nProjectsOther = cOther.getNProjects();
		// higher weight is earlier
		if (nProjectsThis > nProjectsOther) {
			return -1;
		} else if (nProjectsThis < nProjectsOther) {
			return 1;
		} else {
			if (this.size() > cOther.size()) {
				return -1;
			} else if (this.size() < cOther.size()) {
				return 1;
			} else {
				Iterator<RegexProjectSet> it1 = this.iterator();
				Iterator<RegexProjectSet> it2 = cOther.iterator();
				while (it1.hasNext()) {
					RegexProjectSet wrr1 = it1.next();
					RegexProjectSet wrr2 = it2.next();
					int ct = wrr1.compareTo(wrr2);
					if (ct != 0) {
						return ct;
					}
				}
				return 0;
			}
		}
	}
}
