package step1;

import org.json.JSONException;

import analysisUtil.Config;

public class Step1 {

	public static void main(String[] args) throws JSONException {
		Config ac = new Config();
		System.out.println(ac.getInputFilename());
	}

}
