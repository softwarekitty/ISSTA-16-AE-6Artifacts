package recreateArtifacts.similarityMatrix.row.cell;

import java.util.concurrent.Callable;

import recreateArtifacts.similarityMatrix.Regex;

public class MatchTask implements Callable<Boolean>{
	private final Regex regex;
	private final String input;
	public MatchTask(Regex regexToUse, String stringToMatch){
		this.regex = regexToUse;
		this.input = stringToMatch;
	}

	@Override
	public Boolean call() throws Exception {
		return new Boolean(regex.match(input));
	}

}
