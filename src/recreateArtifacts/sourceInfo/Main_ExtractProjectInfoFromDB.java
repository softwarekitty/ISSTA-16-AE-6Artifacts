package recreateArtifacts.miningDataSources;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;

import main.io.Config;
import main.io.IOUtil;

public class Main_ExtractProjectInfoFromDB {
	
	private static String getPathToDump(){
		return "src/recreateArtifacts/miningDataSources/";
	}

	public static void main(String[] args) throws IOException, JSONException, ClassNotFoundException, SQLException {
		Config config = new Config();
		File projectInfoDump = new File(config.homePath,getPathToDump()+"projectInfo.txt");
		IOUtil.createAndWrite(projectInfoDump, getInfoFileContent(config));
	}

	private static String getInfoFileContent(Config config) throws SQLException, ClassNotFoundException, JSONException {
		
		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(getConnectionString(config));
		c.setAutoCommit(false);
		stmt = c.createStatement();

		HashMap<Integer, CommitSecsSHA_cloneUrl> projectInfoMap = new HashMap<Integer, CommitSecsSHA_cloneUrl>();
		String query = "select uniqueSourceID, sourceJSON from RegexCitationMerged;";

		// these are all the distinct patterns with weight
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			
			int projectID = rs.getInt("uniqueSourceID");
			String sourceJSON = rs.getString("sourceJSON");
			
			CommitSecsSHA_cloneUrl info = projectInfoMap.get(projectID);
			if(info==null){
				info = new CommitSecsSHA_cloneUrl();
			}
			info.update(sourceJSON);
			projectInfoMap.put(projectID,info);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("INTERNAL_ID\t\t\t\t\tCLONE_URL\t\t\t\t\tLAST_COMMIT_SHA\t\t\t\t\tGITHUB_REPO_ID\n");
		for(Entry<Integer, CommitSecsSHA_cloneUrl> infoEntry : projectInfoMap.entrySet()){
			sb.append(infoEntry.getValue().dumpTSV(infoEntry.getKey()));
			sb.append("\n");
		}

		rs.close();
		stmt.close();
		c.close();
		return sb.toString();
	}
	
	private static String getConnectionString(Config config) {
		String pathToDb = new File(Config.homePath, "artifacts/merged_report.db").getPath();
		return "jdbc:sqlite:" + pathToDb;
	}
	


}
