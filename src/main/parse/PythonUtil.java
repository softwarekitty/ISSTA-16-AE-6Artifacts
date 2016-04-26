package main.parse;

import org.python.util.PythonInterpreter;

public class PythonUtil {
	public static void validatePythonRegex(String pattern) throws PythonParsingException {
		try {

			// make sure the pattern is a valid regex
			PythonInterpreter interpreter = new PythonInterpreter();
			interpreter.exec("import re");
			interpreter.exec("x = re.compile(" + pattern + ")");
		} catch (Exception e) {
			throw new PythonParsingException("Failure when trying to compile pattern in Python: " + pattern);
		}

	}
}
