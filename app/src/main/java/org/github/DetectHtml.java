package org.github;

/**
 * Detect HTML markup in a string
 * This will detect tags or entities
 *
 * @author dbennett455@gmail.com - David H. Bennett
 *
 */

import java.util.regex.Pattern;

public class DetectHtml
{
	// adapted from post by Phil Haack and modified to match better
	public final static String tagStart=
		"\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)\\>";
	public final static String tagEnd=
		"\\</\\w+\\>";
	public final static String tagSelfClosing=
		"\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)/\\>";
	public final static String htmlEntity=
		"&[a-zA-Z][a-zA-Z0-9]+;";
	public final static Pattern htmlPattern=Pattern.compile(
	  "("+tagStart+".*"+tagEnd+")|("+tagSelfClosing+")|("+htmlEntity+")",
	  Pattern.DOTALL
	);

	/**
	 * Will return true if s contains HTML markup tags or entities.
	 *
	 * @param s String to test
	 * @return true if string contains HTML
	 */
	public static boolean isHtml(String s) {
		boolean ret=false;
		if (s != null) {
			ret=htmlPattern.matcher(s).find();
		}
		return ret;
	}

}
