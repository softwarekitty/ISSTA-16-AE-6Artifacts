package step1;

import java.io.IOException;

import org.json.JSONException;

import analysis.config.Config;

public class Step1 {

	public static void main(String[] args) throws JSONException, IOException {
		Config config = new Config();
		System.out.println(config.inputFilename);
		
		//TODO -finish import
//		TreeSet<RegexProjectSet> corpus = CorpusUtil.reloadCorpus();
//		String table = FeatureStatsTable.featureStats(corpus, C.connectionString);
//		File tableFile = new File(C.outputPath,"featureStats.tex");
//		IOUtil.createAndWrite(tableFile, table);
//		System.out.println("Done.");
	}

}
