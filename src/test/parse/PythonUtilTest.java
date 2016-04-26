package test.parse;

import org.junit.Test;

import main.parse.PythonParsingException;
import main.parse.PythonUtil;

public final class PythonUtilTest {

	@Test
	public void test_emptySinglequotes() throws PythonParsingException{
		PythonUtil.validatePythonRegex("''");
	}
	
	@Test
	public void test_emptyDoublequotes() throws PythonParsingException{
		PythonUtil.validatePythonRegex("\"\"");
	}
	
	@Test
	public void test_literals() throws PythonParsingException{
		PythonUtil.validatePythonRegex("'abc'");
	}
	
	@Test
	public void test_RNG() throws PythonParsingException{
		PythonUtil.validatePythonRegex("'[0-9]'");
	}
	
	@Test
	public void test_ADD() throws PythonParsingException{
		PythonUtil.validatePythonRegex("'a+b+'");
	}
	
	@Test
	public void test_CG() throws PythonParsingException{
		PythonUtil.validatePythonRegex("'a(b)c'");
	}
	
	@Test
	public void test_anchors() throws PythonParsingException{
		PythonUtil.validatePythonRegex("'^abc$'");
	}
	
	@Test
	public void test_repetitions() throws PythonParsingException{
		PythonUtil.validatePythonRegex("'a+b*c{1}d{2,3}e{4,}f?g+?'");
	}
	
	@Test
	public void test_elements() throws PythonParsingException{
		PythonUtil.validatePythonRegex("'(a|b)[f]\\\\1'");
	}
	
	@Test
	public void test_validEscapes() throws PythonParsingException{
		PythonUtil.validatePythonRegex("'\\\\^\\\\v\\\\n\\\\t\\\\f\\\\b\\\\B\\\\d\\\\D\\\\w\\\\W\\\\s\\\\S\\\\{\\\\}\\\\[\\\\]\\\\(\\\\)\\\\$\\\\\\\\'");
	}
	
	@Test(expected = PythonParsingException.class)
	public void test_invalidEscapes() throws PythonParsingException{
		PythonUtil.validatePythonRegex("'\\^\\v\\n\\t\\f\\b\\B\\d\\D\\w\\W\\s\\S\\{\\}\\[\\]\\(\\)\\$\\\\'");
	}
}
