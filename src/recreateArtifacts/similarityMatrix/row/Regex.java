package recreateArtifacts.similarityMatrix.row;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
	private static Pattern lCurly = Pattern.compile("(?<!\\\\)\\{(?=\\D)|(?<!\\\\)\\{$");
	private Pattern regex;
	
	public Regex(String pattern) {
		// handle differences in Java syntax
		if(pattern.equals("([\\13\\14])")){
			pattern = "([\\013\\014])";
		}else if(pattern.equals("([^[]+)\\[([^]]+)\\]")){
			pattern = "([^\\[]+)\\[([^]]+)\\]";
		}else if(pattern.equals("[$><;[{~`|&()]")){
			pattern = "[$><;\\[{~`|&()]";
		}else if(pattern.equals("[,*\")([\\]]")){
			pattern = "[,*\")(\\[\\]]";
		}else if(pattern.equals("[[({] | []}),;:]")){
			pattern = "[\\[\\(\\{] | [\\]\\}\\),;:]";
		}else if(pattern.equals("[[*?]")){
			pattern = "[\\[*?]";
		}else if(pattern.equals("[\\000-\\037\\177-\\377]")){
			pattern = "[\\000-\\037\\x7F-\\xFF]";
		}else if(pattern.equals("[\\t-\\r -/:-@[-`{-~]")){
			pattern = "[\\t-\\r -/:-@\\[-`\\{-~]";
		}else if(pattern.equals("[\\t\\b\\r\\f#]")){
			pattern = "[\\t\\010\\r\\f#]";
		}else if(pattern.equals("[\\t\\n #$%&'()*;<=>?[{|}~]|^$")){
			pattern = "[\\t\\n #$%&'()*;<=>?\\[\\{|}~]|^$";
		}else if(pattern.equals("[^A-Za-z0-9\\-_.~!*'();:@&=+$,/?%#[\\]]")){
			pattern = "[^A-Za-z0-9\\-_.~!*'();:@&=+$,/?%#\\[\\]]";
		}else if(pattern.equals("[^[]+\\[(\\d+/[A-Za-z]+/\\d+):[^\\d]*")){
			pattern = "[^\\[]+\\[(\\d+/[A-Za-z]+/\\d+):[^\\d]*";
		}else if(pattern.equals("\\\\([.(){}[\\]\"\\\\])")){
			pattern = "\\\\([.()\\{}\\[\\]\"\\\\])";
		}else if(pattern.equals("\\s*([a-zA-Z_][-:.a-zA-Z_0-9]*)[$]?(\\s*=\\s*(\\\\'[^\\\\']*\\\\'|\"[^\"]*\"|[][\\-a-zA-Z0-9./,:;+*%?!&$\\(\\)_#=~\\\\'\"@]*))?")){
			pattern = "\\s*([a-zA-Z_][-:.a-zA-Z_0-9]*)[$]?(\\s*=\\s*(\\\\'[^\\\\']*\\\\'|\"[^\"]*\"|[\\]\\[\\-a-zA-Z0-9./,:;+*%?!&$\\(\\)_#=~\\\\'\"@]*))?";
		}
		Matcher m = lCurly.matcher(pattern);
		// in tests, it seems okay to add a forward-slash within a CCC
		regex = Pattern.compile(m.replaceAll("\\\\{"));
	}

	public boolean match(String matchingString) {
		InterruptibleCharSequence seq = new InterruptibleCharSequence(
				matchingString.subSequence(0, matchingString.length()));
		Matcher matcher = regex.matcher(seq);
		boolean found = matcher.find();
//		System.out.println("pattern: "+regex.pattern()+" seq: "+seq+" found: "+found);
		return found;
	}

	// cite:
	// http://stackoverflow.com/questions/910740/cancelling-a-long-running-regex-match
	/**
	 * CharSequence that noticed thread interrupts -- as might be necessary to
	 * recover from a loose regex on unexpected challenging input.
	 * 
	 * @author gojomo
	 */
	class InterruptibleCharSequence implements CharSequence {
		CharSequence inner;
		// public long counter = 0;

		public InterruptibleCharSequence(CharSequence inner) {
			super();
			this.inner = inner;
		}

		public char charAt(int index) {
			if (Thread.interrupted()) { // clears flag if set
				throw new RuntimeException(new InterruptedException());
			}
			// counter++;
			return inner.charAt(index);
		}

		public int length() {
			return inner.length();
		}

		public CharSequence subSequence(int start, int end) {
			return new InterruptibleCharSequence(inner.subSequence(start, end));
		}

		@Override
		public String toString() {
			return inner.toString();
		}
	}

}
