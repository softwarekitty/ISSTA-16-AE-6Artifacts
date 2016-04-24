package analysisUtil;

import java.util.TreeSet;

public class ClusterMemberSpec extends TreeSet<Integer>{
	private static final long serialVersionUID = -4943115767979183054L;
	private String name;
	public ClusterMemberSpec(String name, int[] memberIDs){
		this.name = name;
		for(int id : memberIDs){
			this.add(id);
		}
	}
	public String getName(){
		return name;
	}
}
