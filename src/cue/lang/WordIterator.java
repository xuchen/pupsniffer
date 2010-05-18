/*
   Copyright 2009 IBM Corp

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package cue.lang;

import java.util.regex.Pattern;

/**
 *
 * @author Jonathan Feinberg <jdf@us.ibm.com>
 *
 */
public class WordIterator extends IterableText
{
	private static final String LETTER = "[@+\\p{javaLetter}\\p{javaDigit}]";
	/*
	 * Unicode Character 'PRIME' (U+2032)
	 * http://www.fileformat.info/info/unicode/char/2032/index.htm
	 * Unicode Character 'NO-BREAK SPACE' (U+00A0)
	 * http://www.fileformat.info/info/unicode/char/00a0/index.htm
	 * Unicode Character 'ZERO WIDTH NON-JOINER' (U+200C)
	 * http://www.fileformat.info/info/unicode/char/200c/index.htm
	 * Unicode Character 'ZERO WIDTH JOINER' (U+200D)
	 * http://www.fileformat.info/info/unicode/char/200d/index.htm
	 * [@+\p{javaLetter}\p{javaDigit}]+([-.:/'’\p{M}\u2032\u00A0\u200C\u200D~]+[@+\\p{javaLetter}\\p{javaDigit}]+)*
	 */
	private static final String JOINER = "[-.:/'’\\p{M}\\u2032\\u00A0\\u200C\\u200D~]";
	private static final Pattern WORD = Pattern.compile(LETTER + "+(" + JOINER + "+"
			+ LETTER + "+)*");


	public WordIterator(final String text)
	{
		this.m = WORD.matcher(text == null ? "" : text);
		hasNext = m.find();
	}

}
