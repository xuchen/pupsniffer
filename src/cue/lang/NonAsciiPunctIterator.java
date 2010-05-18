/**
 * This class iterates through punctuations that are not encoded
 * in ASCII code.
 */
package cue.lang;

import java.util.regex.Pattern;

/**
 * @author Xuchen Yao
 *
 */
public class NonAsciiPunctIterator extends IterableText {

	/*
	 * It's not any unicode letter, unicode digits or spaces.
	 * ([^\p{javaLetterOrDigit}^\p{Punct}|\s])
	 */
	private static final Pattern WORD = Pattern.compile("[^\\p{javaLetterOrDigit}|\\p{Punct}|\\s]");

	public NonAsciiPunctIterator(final String text) {
		this.m = WORD.matcher(text == null ? "" : text);
		hasNext = m.find();
	}

}
