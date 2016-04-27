package recreateArtifacts.sourceInfo;

import org.json.JSONException;
import org.json.JSONObject;

public 	class SourceInfo{
	long commitSecs;
	String sha;
	String cloneUrl;
	String repoID;
	
	public SourceInfo(){
		this.commitSecs = Long.MIN_VALUE;
		this.sha = "INVALID_SHA";
		this.cloneUrl = "INVALID_CLONEURL";
		this.repoID = "INVALID_REPO_ID";
	}
	
	public String dumpTSV(int projectID) {
		return projectID + "\t" + cloneUrl + "\t" + sha + "\t" + repoID;
	}

	public void update(String sourceJSON) throws JSONException{
		JSONObject sourceInfo = new JSONObject(sourceJSON);
		JSONObject data = sourceInfo.getJSONObject("data");
		String currentSha = data.getString("sha");
		String commitS = data.getString("commitS");
		long currentCommitSecs = Long.parseLong(commitS);
		if(currentCommitSecs>commitSecs){
			this.sha = currentSha;
			JSONObject meta = sourceInfo.getJSONObject("meta");
			String currentCloneUrl = meta.getString("clone_url");
			this.cloneUrl = currentCloneUrl;
			this.repoID = meta.getString("repoID");
		}
	}
}
