package io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import step5.CategoryMemberSpec;

/**
 * represent the contents of the analysis config file
 * 
 * @author cc
 */
public class Config {

	// access using public fields for immutable values
	public static final String homePath = System.getProperty("user.dir");
	public final String inputFilename;
	public final boolean shouldRankByNPatterns;
	public final boolean shouldCreateOnlyLatexTable;
	public final int nTestStrings;
	public final String testStringDelimiter;
	public final boolean shouldDumpHalfMatrix;
	public final boolean shouldDumpWholeMatrix;
	public final boolean shouldDumpRowFiles;
	public final boolean shouldDumpErrorOutput;
	public final double skipBelow;
	public final double mclI;
	public final double mclP;
	public final double mclK;
	public final boolean shouldKeepMCLOutput;
	public final boolean shouldDumpClusters;
	public final boolean shouldExportProjectIDPatternMM;
	public final boolean shouldUseHeaviestRegexAsClusterRep;
	private List<CategoryMemberSpec> categorySpecs;

	// load the config file into memory for convenience
	public Config() throws IOException, JSONException {

		JSONObject[] stepObjects = new JSONObject[5];
		String text = new String(Files.readAllBytes(Paths.get(CONFIG_FILE_NAME)), StandardCharsets.UTF_8);
		JSONObject configData = new JSONObject(text);
		stepObjects[0] = configData.getJSONObject(STEP_1);
		stepObjects[1] = configData.getJSONObject(STEP_2);
		stepObjects[2] = configData.getJSONObject(STEP_3);
		stepObjects[3] = configData.getJSONObject(STEP_4);
		stepObjects[4] = configData.getJSONObject(STEP_5);

		this.inputFilename = stepObjects[0].getString(INPUT_FILENAME);
		this.shouldRankByNPatterns = stepObjects[0].getBoolean(RANK_BY_NPATTERNS);
		this.shouldCreateOnlyLatexTable = stepObjects[0].getBoolean(CREATE_TABLE_ONLY);

		this.nTestStrings = stepObjects[1].getInt(N_TEST_STRINGS);
		this.testStringDelimiter = stepObjects[1].getString(TEST_STRING_DELIMITER);

		this.shouldDumpHalfMatrix = stepObjects[2].getBoolean(DUMP_HALF_MATRIX);
		this.shouldDumpWholeMatrix = stepObjects[2].getBoolean(DUMP_WHOLE_MATRIX);
		this.shouldDumpRowFiles = stepObjects[2].getBoolean(DUMP_ROW_FILES);
		this.shouldDumpErrorOutput = stepObjects[2].getBoolean(DUMP_ERROR_OUTPUT);
		this.skipBelow = stepObjects[2].getDouble(SKIP_COMPUTING_SIMILARITY_BELOW);

		this.mclI = stepObjects[3].getDouble(MCL_I);
		this.mclP = stepObjects[3].getDouble(MCL_P);
		this.mclK = stepObjects[3].getDouble(MCL_K);
		this.shouldKeepMCLOutput = stepObjects[3].getBoolean(KEEP_MCL_OUTPUT);
		this.shouldDumpClusters = stepObjects[3].getBoolean(DUMP_CLUSTERS_OF_TWO_OR_MORE);

		this.shouldExportProjectIDPatternMM = stepObjects[4].getBoolean(EXPORT_PROJECT_ID_PATTERN_MM);
		this.shouldUseHeaviestRegexAsClusterRep = stepObjects[4].getBoolean(REPRESENT_CLUSTERS_BY_HEAVIEST);

		// expects zero or more cluster specifications
		// with zero or more integers each
		this.categorySpecs = new LinkedList<CategoryMemberSpec>();
		JSONArray categories = stepObjects[4].getJSONArray(CATEGORIZATION_CONTROLLER);
		for (int i = 0; i < categories.length(); i++) {
			JSONObject categorySpec = categories.getJSONObject(i);
			String categoryName = categorySpec.getString(CATEGORY_NAME);
			JSONArray memberArray = categorySpec.getJSONArray(CATEGORY_MEMBERS);

			// only integers in the array of project IDs, please
			int[] categoryMembers = new int[memberArray.length()];
			for (int j = 0; j < memberArray.length(); j++) {
				categoryMembers[j] = memberArray.getInt(j);
			}
			categorySpecs.add(new CategoryMemberSpec(categoryName, categoryMembers));
		}
	}

	public List<CategoryMemberSpec> getClusterSpecs() {
		List<CategoryMemberSpec> defensiveCopy = new LinkedList<CategoryMemberSpec>();
		for (CategoryMemberSpec spec : categorySpecs) {
			CategoryMemberSpec copy = new CategoryMemberSpec(spec.getName());
			copy.addAll(spec);
			defensiveCopy.add(copy);
		}
		return defensiveCopy;
	}

	private static final String CONFIG_FILE_NAME = "analysis_config.json";

	// key names for folders should equal folder names
	public static final String STEP_1 = "step1_featureAnalysis";
	public static final String STEP_2 = "step2_generateTestStrings";
	public static final String STEP_3 = "step3_generateSimilarityMatrix";
	public static final String STEP_4 = "step4_generateClusters";
	public static final String STEP_5 = "step5_categorizeClusters";

	// key names from the config file
	private static final String INPUT_FILENAME = "input_filename";
	private static final String RANK_BY_NPATTERNS = "rank_by_nPatterns___Default_is_rank_by_nProjects";
	private static final String CREATE_TABLE_ONLY = "create_latex_table_only___Default_is_standalone_LaTeX_document";
	private static final String N_TEST_STRINGS = "nTestStrings";
	private static final String TEST_STRING_DELIMITER = "testStringDelimiter";
	private static final String DUMP_HALF_MATRIX = "dump_halfMatrix";
	private static final String DUMP_WHOLE_MATRIX = "dump_wholeMatrix";
	private static final String DUMP_ROW_FILES = "dump_rowFiles";
	private static final String DUMP_ERROR_OUTPUT = "dump_errorOutput";
	private static final String SKIP_COMPUTING_SIMILARITY_BELOW = "for_speed_skip_computing_similarity_below";
	private static final String MCL_I = "mcl_i__inflation";
	private static final String MCL_P = "mcl_p__pruneBelow";
	private static final String MCL_K = "mcl_k__neighborsToExplorePerStep";
	private static final String KEEP_MCL_OUTPUT = "keep_mcl_output";
	private static final String DUMP_CLUSTERS_OF_TWO_OR_MORE = "dump_clusters_of_two_or_more_contents";
	private static final String EXPORT_PROJECT_ID_PATTERN_MM = "for_speed_export_projectIDPatternIDMultiMap";
	private static final String REPRESENT_CLUSTERS_BY_HEAVIEST = "represent_clusters_by_heaviest___Default_is_shortest";
	private static final String CATEGORIZATION_CONTROLLER = "categorization_controller";
	private static final String CATEGORY_NAME = "name";
	private static final String CATEGORY_MEMBERS = "members";
}
