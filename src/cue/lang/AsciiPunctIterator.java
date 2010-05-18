/**
 *
 */
package cue.lang;

import java.util.regex.Pattern;

/**
 * @author Xuchen Yao
 *
 */
public class AsciiPunctIterator extends IterableText {

	private static final Pattern WORD = Pattern.compile("\\p{Punct}]");

	public AsciiPunctIterator(final String text) {
		this.m = WORD.matcher(text == null ? "" : text);
		hasNext = m.find();
	}

}
