package recreateArtifacts.featureTable;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

import org.json.JSONException;

import main.core.RegexProjectSet;
import main.core.features.FeatureDictionary;
import main.io.IOUtil;
import main.io.LoadUtil;
import main.parse.PythonParsingException;
import main.parse.QuoteRuleException;
import recreateArtifacts.PathUtil;

public class Main_FeatureStatsTableBuilder {
	
	public static void main(String[] args) throws IOException, JSONException, ClassNotFoundException, SQLException, IllegalArgumentException, QuoteRuleException, PythonParsingException{
		TreeSet<RegexProjectSet> corpus = LoadUtil.loadRegexProjectSetInput(IOUtil.readLines(PathUtil.pathToCorpusFile()));
		StringBuilder sb = new StringBuilder();
		sb.append("\\documentclass[12pt]{article}\n");
		sb.append("\\usepackage[margin=0.4in]{geometry}\n");
		sb.append("\\usepackage{tikz}\n");
		sb.append("\\usepackage{booktabs}\n");
		sb.append("\\newcommand{\\yes}{\\tikz\\draw[black,fill=black] (0,0) circle (.5ex);}\n");
		sb.append("\\newcommand{\\no}{\\tikz\\draw[black] (0,0) circle (.5ex);}\n");
		sb.append("\\pagenumbering{gobble}\n");
		sb.append("\\begin{document}\n");
		sb.append(featureStats(corpus,PathUtil.getConnectionString()));
		sb.append("\\end{document}\n");
		IOUtil.createAndWrite(new File(PathUtil.getPathFeature()+"output/featureStats.tex"), sb.toString());
	}

	public static String featureStats(TreeSet<RegexProjectSet> corpus,
			String connectionString) throws ClassNotFoundException,
			SQLException {

		FeatureDictionary fd = new FeatureDictionary();
		int[] totalNProjects = { 0 };
		int[] nProjectsPerFeature = getProjectsPerFeature(corpus, fd, totalNProjects, connectionString);

		int[] totalNFiles = { 0 };
		int[] filesWithFeature = getFilesPerFeature(corpus, fd, totalNFiles, connectionString);

		int nFeatures = fd.getSize();
		int nPatterns = corpus.size();
		int literalTokens = 0;
		int literalPresent = 0;
		int[] presentCounter = new int[nFeatures];
		int[] tokensCounter = new int[nFeatures];
		int[] max = new int[nFeatures];
		double totalWeight = 0;
		int[] featureWeight = new int[nFeatures];

		for (RegexProjectSet wrr : corpus) {
			int[] featureCount = wrr.getFeatures().getFeatureCountArray();
			for (int i = 0; i < nFeatures; i++) {
				int count = featureCount[i];
				if (count > 0) {
					featureWeight[i] += wrr.getNProjects();

					if (max[i] < count) {
						max[i] = count;
						if (i != FeatureDictionary.I_META_LITERAL &&
							count > 100) {
							System.out.println("100? INSPECT THIS (" +
								fd.getCode(i) + "): " + wrr.getPattern());
							// IOUtil.waitNsecsOrContinue(20);
						}
					}
					tokensCounter[i] += count;
					presentCounter[i]++;
					if (i == FeatureDictionary.I_META_LITERAL) {
						literalTokens += count;
						literalPresent++;
					}
				}
			}
			totalWeight += wrr.getNProjects();
		}
		DecimalFormat df = new DecimalFormat("00.0");

		int totalTokens = 0;
		for (int tokens : tokensCounter) {
			totalTokens += tokens;
		}
		int adjustedTokens = totalTokens - literalTokens;

		StringBuilder sb = new StringBuilder();
		String between = " & ";
		sb.append("\\begin{table*}\n\\begin{scriptsize}\n\\begin{center}\n"
			+ "\\caption{How Frequently do Features Appear in Projects, and Which Features are Supported By Four Major Regex Projects? (Table 4 in the paper)}\n"
			+ "\\label{table:featureStats}\n"
			+ "\\begin{tabular}\n{llllcccccccccc}\n");
		sb.append("rank & code & description & example & brics & hampi & Rex & RE2 & nPatterns & \\% patterns & nProjects & \\% projects \\\\ \n\\toprule[0.16em]\n");
		TreeSet<FeatureDetail> sortedFeatures = new TreeSet<FeatureDetail>();
		for (int i = 0; i < nFeatures; i++) {
			if (i == FeatureDictionary.I_META_LITERAL || presentCounter[i] == 0) {
				continue;
			}
			// int featureID, int nFiles, int nPresent, int nProjects, int max,
			// int nTokens)
			sortedFeatures.add(new FeatureDetail(i, filesWithFeature[i], presentCounter[i], nProjectsPerFeature[i], max[i], tokensCounter[i]));
		}

		int rankIndex = 1;
		for (FeatureDetail featureDetail : sortedFeatures) {
			int ID = featureDetail.getID();
			String featureCode = fd.getCode(ID);
			String description = fd.getDescription(ID);
			String verbatimBlock = fd.getVerbatim(ID);

			String nPresent = commafy(presentCounter[ID]);
			String percentPresent = percentify(presentCounter[ID], nPatterns);

			String nTokens = commafy(tokensCounter[ID]);
			String percentTokens = percentify(tokensCounter[ID], adjustedTokens);

			String maxOccurances = commafy(max[ID]);

			String weightInt = commafy(featureDetail.getNProjectsHavingFeature());
			String weightPercent = df.format(100 * (featureDetail.getNProjectsHavingFeature() / totalWeight));

			// System.out.println("filesWithFeature[ID]: "+filesWithFeature[ID]+" totalNFiles[0]: "+totalNFiles[0]+" nProjectsPerFeature[ID]: "+nProjectsPerFeature[ID]+" totalNProjects[0]: "+totalNProjects[0]);

			String nFiles = commafy(filesWithFeature[ID]);
			String percentFiles = percentify(filesWithFeature[ID], totalNFiles[0]);

			String nProjects = commafy(nProjectsPerFeature[ID]);
			String percentProjects = percentify(nProjectsPerFeature[ID], totalNProjects[0]);

			sb.append("" + rankIndex);
			sb.append(between);
			sb.append(featureCode);
			sb.append(between);
			sb.append(description);
			sb.append(between);
			sb.append(verbatimBlock);
			sb.append(between);
			sb.append(projectFeatureInclusion(ID, 0));
			sb.append(between);
			sb.append(projectFeatureInclusion(ID, 1));
			sb.append(between);
			sb.append(projectFeatureInclusion(ID, 2));
			sb.append(between);
			sb.append(projectFeatureInclusion(ID, 3));
			sb.append(between);

			sb.append(nPresent);
			sb.append(between);
			sb.append(percentPresent);
			sb.append(between);

			// sb.append(nTokens);
			// sb.append(between);
			// sb.append(percentTokens);
			// sb.append(between);

			// sb.append(maxOccurances);
			// sb.append(between);

			// sb.append(weightInt);
			// sb.append(between);
			// sb.append(weightPercent);
			// sb.append(between);

			// sb.append(nFiles);
			// sb.append(between);
			// sb.append(percentFiles);
			// sb.append(between);
			sb.append(nProjects);
			sb.append(between);
			sb.append(percentProjects);

			if(rankIndex==8 || rankIndex==27){
				sb.append(" \\\\ \n\\midrule[0.12em]\n");
			}else if(rankIndex < sortedFeatures.size()){
				sb.append(" \\\\ \n\\midrule\n");
			}
			rankIndex++;
		}
		sb.append(" \\\\ \n\\bottomrule[0.13em]\n\\end{tabular}\n"
			+ "\\end{center}\n\\end{scriptsize}\n\\end{table*}\n");
		return sb.toString();
	}

	private static int[] getFilesPerFeature(TreeSet<RegexProjectSet> corpus,
			FeatureDictionary fd, int[] totalNFiles, String connectionString)
			throws ClassNotFoundException, SQLException {
		String filePatternQuery = "select uniqueSourceID || filePath as key, pattern from RegexCitationMerged;";
		return getElementsPerFeature(corpus, fd, totalNFiles, filePatternQuery, connectionString);
	}

	private static int[] getProjectsPerFeature(TreeSet<RegexProjectSet> corpus,
			FeatureDictionary fd, int[] totalNProjects, String connectionString)
			throws ClassNotFoundException, SQLException {
		String projectPatternQuery = "select uniqueSourceID || 'X' as key, pattern from RegexCitationMerged;";
		return getElementsPerFeature(corpus, fd, totalNProjects, projectPatternQuery, connectionString);
	}

	private static int[] getElementsPerFeature(TreeSet<RegexProjectSet> corpus,
			FeatureDictionary fd, int[] totalNElements, String elementQuery,
			String connectionString) throws ClassNotFoundException,
			SQLException {
		HashMap<String, ArrayList<Integer>> corpusMap = getCorpusMap(corpus);
		HashMap<Integer, ArrayList<String>> projectIndexListMap = getIndexListMap(elementQuery, connectionString);
		int nIndices = projectIndexListMap.size();
		totalNElements[0] = nIndices;
		int nFeatures = fd.getSize();
		int[][] matrix = new int[nIndices][nFeatures];
		Collection<Integer> indices = projectIndexListMap.keySet();

		// does not matter if these are iterated in order, but the keys MUST
		// be sequential so that fileMatrix is full, all indices are in bounds
		for (Integer index : indices) {
			ArrayList<String> patternList = projectIndexListMap.get(index);
			// System.out.println("index: "+index +
			// " patternList: "+patternList);
			for (String pattern : patternList) {

				// note how important the iteration order in this list is!
				ArrayList<Integer> featureCount = corpusMap.get(pattern);

				// some patterns are in the database, but not in the corpus -
				// we have to ignore these because they were excluded because
				// they have features that PCRE parser cannot parse, so we have
				// no featureCount for some feature they use (this is rare)
				if (featureCount != null) {
					for (int featureIndex = 0; featureIndex < nFeatures; featureIndex++) {
						// System.out.println("featureIndex: "+featureIndex +
						// " featureCount: "+featureCount);

						int fCount = featureCount.get(featureIndex);
						matrix[index][featureIndex] += fCount;
					}
				} else {
					// System.out.println("pattern not in corpus: " + pattern);
				}

			}
		}
		int[] elementsPerFeature = new int[nFeatures];
		for (int i = 0; i < nIndices; i++) {
			for (int j = 0; j < nFeatures; j++) {
				if (matrix[i][j] != 0) {
					elementsPerFeature[j]++;
				}
			}
		}
		return elementsPerFeature;
	}

	private static HashMap<String, ArrayList<Integer>> getCorpusMap(
			TreeSet<RegexProjectSet> corpus) {
		HashMap<String, ArrayList<Integer>> corpusMap = new HashMap<String, ArrayList<Integer>>();
		for (RegexProjectSet wrr : corpus) {
			int[] fc = wrr.getFeatures().getFeatureCountArray();
			ArrayList<Integer> featureCounts = new ArrayList<Integer>(fc.length);
			for (int fCount : fc) {
				featureCounts.add(fCount);
			}
			corpusMap.put(wrr.getPattern(), featureCounts);
		}
		return corpusMap;
	}

	// important note: Integer keys must be sequential from zero - one for each
	// element
	private static HashMap<Integer, ArrayList<String>> getIndexListMap(
			String query, String connectionString)
			throws ClassNotFoundException, SQLException {
		HashMap<String, ArrayList<String>> keyListMap = new HashMap<String, ArrayList<String>>();

		// this is not a necessity, but guarantees identical results across runs
		TreeSet<String> sortedKeys = new TreeSet<String>();

		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(connectionString);
		c.setAutoCommit(false);
		stmt = c.createStatement();

		// the query needs to return a relation,
		// the first string is a key, second the pattern
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			String key = rs.getString("key");
			String pattern = rs.getString("pattern");
			sortedKeys.add(key);
			ArrayList<String> patternList = keyListMap.get(key);
			if (patternList == null) {
				patternList = new ArrayList<String>();
			}
			patternList.add(pattern);
			keyListMap.put(key, patternList);
		}

		// wind down sql
		rs.close();
		stmt.close();
		c.close();

		HashMap<Integer, ArrayList<String>> indexListMap = new HashMap<Integer, ArrayList<String>>();
		int sequentialIndex = 0;
		for (String elementKey : sortedKeys) {
			ArrayList<String> finalPatternList = keyListMap.get(elementKey);
			indexListMap.put(sequentialIndex++, finalPatternList);
		}
		return indexListMap;
	}

	public static String percentify(double d, double sum) {
		DecimalFormat df = new DecimalFormat("##0.#");
		return df.format(100 * round(d / sum, 3));
	}

	public static String commafy(int n) {
		return NumberFormat.getNumberInstance(Locale.US).format(n);
	}

	public static int intify(String s) {
		int dotIndex = s.indexOf('.');
		String intString = s;
		if (dotIndex > -1) {
			intString = s.substring(0, dotIndex);
		}
		return Integer.parseInt(intString.replaceAll(",", ""));
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private static String projectFeatureInclusion(int ID, int projectIndex) {
		// from:
		// http://www.brics.dk/automaton/doc/index.html?dk/brics/automaton/RegExp.html
		int[] bricsMissingFeatures = { FeatureDictionary.I_REP_LAZY,
				FeatureDictionary.I_CC_DECIMAL,
				FeatureDictionary.I_CC_NDECIMAL,
				FeatureDictionary.I_CC_WHITESPACE,
				FeatureDictionary.I_CC_NWHITESPACE,
				FeatureDictionary.I_CC_WORD, FeatureDictionary.I_CC_NWORD,
				FeatureDictionary.I_LOOK_AHEAD,
				FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,
				FeatureDictionary.I_LOOK_BEHIND,
				FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,
				FeatureDictionary.I_LOOK_NON_CAPTURE,
				FeatureDictionary.I_META_NUMBERED_BACKREFERENCE,
				FeatureDictionary.I_POS_END_ANCHOR,
				FeatureDictionary.I_POS_NONWORD,
				FeatureDictionary.I_POS_START_ANCHOR,
				FeatureDictionary.I_POS_WORD,
				FeatureDictionary.I_XTRA_END_SUBJECTLINE,
				FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
				FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
				FeatureDictionary.I_XTRA_OPTIONS,
				FeatureDictionary.I_XTRA_VERTICAL_WHITESPACE };

		// by inspecting lib/regex-hampi/sampleRegex
		int[] hampiMissingFeatures = { FeatureDictionary.I_LOOK_AHEAD,
				FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,
				FeatureDictionary.I_LOOK_BEHIND,
				FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,
				FeatureDictionary.I_META_NUMBERED_BACKREFERENCE,
				FeatureDictionary.I_POS_NONWORD, FeatureDictionary.I_POS_WORD,
				FeatureDictionary.I_XTRA_END_SUBJECTLINE,
				FeatureDictionary.I_XTRA_VERTICAL_WHITESPACE };

		// by using Rex, these are from PaperWriter
		int[] rexMissingFeatures = { FeatureDictionary.I_REP_LAZY,
				FeatureDictionary.I_LOOK_AHEAD,
				FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,
				FeatureDictionary.I_LOOK_BEHIND,
				FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,
				FeatureDictionary.I_LOOK_NON_CAPTURE,
				FeatureDictionary.I_META_NUMBERED_BACKREFERENCE,
				FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
				FeatureDictionary.I_POS_NONWORD, FeatureDictionary.I_POS_WORD,
				FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
				FeatureDictionary.I_XTRA_OPTIONS,
				FeatureDictionary.I_XTRA_END_SUBJECTLINE };

		// from: https://re2.googlecode.com/hg/doc/syntax.html
		int[] re2MissingFeatures = { FeatureDictionary.I_LOOK_AHEAD,
				FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,
				FeatureDictionary.I_LOOK_BEHIND,
				FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,
				FeatureDictionary.I_META_NUMBERED_BACKREFERENCE,
				FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE, };
		int[][] excluded = { bricsMissingFeatures, hampiMissingFeatures,
				rexMissingFeatures, re2MissingFeatures };

		int[] bricsSortaFeatures = { FeatureDictionary.I_META_CAPTURING_GROUP };
		int[] hampiSortaFeatures = { FeatureDictionary.I_XTRA_OPTIONS,
				FeatureDictionary.I_META_CAPTURING_GROUP,
				FeatureDictionary.I_REP_LAZY };
		int[] rexSortaFeatures = { FeatureDictionary.I_META_CAPTURING_GROUP };
		int[] re2SortaFeatures = { FeatureDictionary.I_META_CAPTURING_GROUP };

		int[] excl = excluded[projectIndex];
		for (int i : excl) {
			if (i == ID) {
				return "\\no";
			}
		}

		int[] mby = excluded[projectIndex];
		for (int j : mby) {
			if (j == ID) {
				return "\\sorta";
			}
		}
		return "\\yes";
	}

}
