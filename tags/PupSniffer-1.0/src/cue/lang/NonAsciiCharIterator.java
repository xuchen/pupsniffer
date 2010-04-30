/**
 * Iterate character by character (but no spaces).
 * This is a naive segmentation for languages
 * without spaces, such as Chinese.
 */
package cue.lang;

import java.util.regex.Pattern;

/**
 * @author Xuchen Yao
 *
 */
public class NonAsciiCharIterator extends IterableText {

	// no space and ASCII
	private static final Pattern WORD = Pattern.compile("[^\\s\\p{ASCII}]");

	/**
	 *
	 */
	public NonAsciiCharIterator(final String text) {
		this.m = WORD.matcher(text == null ? "" : text);
		hasNext = m.find();
	}


}
