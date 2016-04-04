package metric;

import java.util.HashMap;

public class CountMap extends HashMap<Integer,Integer>{
	private static final long serialVersionUID = 5181725662840430900L;

	public CountMap(){
		super();
	}
	
	public void increment(int index){
		Integer previousValue = get(index);
		if(previousValue==null){
			put(index,1);
		}else{
			put(index,previousValue+1);
		}
	}
}
