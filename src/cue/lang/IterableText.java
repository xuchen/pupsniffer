package cue.lang;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;

abstract class IterableText implements Iterator<String>, Iterable<String>
{
	protected boolean hasNext;
	protected Matcher m;

	public Iterator<String> iterator()
	{
		return this;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	public String next()
	{
		if (!hasNext)
		{
			throw new NoSuchElementException();
		}
		final String s = m.group();
		hasNext = m.find();
		return s;
	}

	public boolean hasNext()
	{
		return hasNext;
	}
}
