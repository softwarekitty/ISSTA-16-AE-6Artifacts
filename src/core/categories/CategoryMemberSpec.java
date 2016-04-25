package core.categories;

import java.util.TreeSet;

/**
 * represents one category membership specification, where a cluster ID is an
 * integer, and the members of a category are clusters, specified by their
 * cluster ID.
 * 
 * @author cc
 */
public class CategoryMemberSpec extends TreeSet<Integer> {
	private static final long serialVersionUID = -4943115767979183054L;
	private String name;

	public CategoryMemberSpec(String name, int[] memberIDs) {
		super();
		this.name = name;

		for (int id : memberIDs) {
			this.add(id);
		}
	}

	public CategoryMemberSpec(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
