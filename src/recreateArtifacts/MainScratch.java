package recreateArtifacts;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import main.core.RegexProjectSet;
import main.io.IOUtil;
import main.io.LoadUtil;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;

public class MainScratch {
	public static void main(String[] args) throws IllegalArgumentException, IOException, QuoteRuleException, PythonParsingException{
		TreeSet<RegexProjectSet> corpusLoadedFromFile = LoadUtil
				.loadRegexProjectSetInput(IOUtil.readLines(PathUtil.pathToCorpusFile()));
		String wholeOrderingFileContents = IOUtil.readFileToString(PathUtil.pathToOriginalOrdering());
		
	}

}
