package create_table;

import java.io.File;
import java.sql.SQLException;
import java.util.TreeSet;

import build_corpus.CorpusUtil;
import build_corpus.RegexProjectSet;
import c.C;
import c.IOUtil;
import exceptions.PythonParsingException;
import exceptions.QuoteRuleException;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException, SQLException, QuoteRuleException, PythonParsingException {
		TreeSet<RegexProjectSet> corpus = CorpusUtil.initializeCorpus(C.connectionString);
		String table = FeatureStatsTable.featureStats(corpus, C.connectionString);
		File tableFile = new File(C.outputPath,"featureStats.tex");
		IOUtil.createAndWrite(tableFile, table);
	}

}
