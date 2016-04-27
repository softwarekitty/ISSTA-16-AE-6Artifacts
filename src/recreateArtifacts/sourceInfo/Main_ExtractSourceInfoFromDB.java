package recreateArtifacts.sourceInfo;

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

import main.io.IOUtil;
import recreateArtifacts.PathUtil;

public class Main_ExtractSourceInfoFromDB {

	public static void main(String[] args) throws IOException, JSONException, ClassNotFoundException, SQLException {
		File projectInfoDump = new File(PathUtil.getPathSource()+"output/projectInfo.tsv");
		IOUtil.createAndWrite(projectInfoDump, getInfoFileContent());
	}

	private static String getInfoFileContent() throws SQLException, ClassNotFoundException, JSONException {
		
		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(PathUtil.getConnectionString());
		c.setAutoCommit(false);
		stmt = c.createStatement();

		HashMap<Integer, SourceInfo> projectInfoMap = new HashMap<Integer, SourceInfo>();
		String query = "select uniqueSourceID, sourceJSON from RegexCitationMerged;";

		// these are all the distinct patterns with weight
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			
			int projectID = rs.getInt("uniqueSourceID");
			String sourceJSON = rs.getString("sourceJSON");
			
			SourceInfo info = projectInfoMap.get(projectID);
			if(info==null){
				info = new SourceInfo();
			}
			info.update(sourceJSON);
			projectInfoMap.put(projectID,info);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("INTERNAL_ID\t\t\t\t\tCLONE_URL\t\t\t\t\tLAST_COMMIT_SHA\t\t\t\t\tGITHUB_REPO_ID\n");
		for(Entry<Integer, SourceInfo> infoEntry : projectInfoMap.entrySet()){
			sb.append(infoEntry.getValue().dumpTSV(infoEntry.getKey()));
			sb.append("\n");
		}

		rs.close();
		stmt.close();
		c.close();
		return sb.toString();
	}
}

