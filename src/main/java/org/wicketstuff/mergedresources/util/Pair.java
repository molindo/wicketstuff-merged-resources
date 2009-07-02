package org.wicketstuff.mergedresources.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Pair<A, B> implements Serializable {

	private static final long serialVersionUID = 1L;

	private A _first;

	private B _second;

	/**
	 * Contructs a pair holding two null values.
	 */
	public Pair() {
		_first = null;
		_second = null;
	}

	/**
	 * Contructs a pair holding the given objects.
	 */
	public Pair(final A a, final B b) {
		_first = a;
		_second = b;
	}

	/**
	 * Contructs a pair holding the objects of the given pair.
	 */
	public Pair(final Pair<A, B> p) {
		_first = p.getFirst();
		_second = p.getSecond();
	}

	public Pair(final Entry<A, B> e) {
		_first = e.getKey();
		_second = e.getValue();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (_first == null ? 0 : _first.hashCode());
		result = prime * result + (_second == null ? 0 : _second.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Pair)) {
			return false;
		}
		final Pair other = (Pair) obj;
		if (_first == null) {
			if (other._first != null) {
				return false;
			}
		} else if (!_first.equals(other._first)) {
			return false;
		}
		if (_second == null) {
			if (other._second != null) {
				return false;
			}
		} else if (!_second.equals(other._second)) {
			return false;
		}
		return true;
	}

	public A getFirst() {
		return _first;
	}

	public void setFirst(final A first) {
		_first = first;
	}

	public B getSecond() {
		return _second;
	}

	public void setSecond(final B second) {
		_second = second;
	}

	// alias for getSecond, inspired by Map.Entry
	public B getValue() {
		return getSecond();
	}

	// alias for getFirst, inspired by Map.Entry
	public A getKey() {
		return getFirst();
	}

	@Override
	public String toString() {
		return "['" + getKey() + "' = '" + getValue() + "']";
	}

	public static <K, V> HashMap<K, V> toHashMap(final List<Pair<K, V>> pairs) {
		final HashMap<K, V> map = new HashMap<K, V>();
		for (final Pair<K, V> pair : pairs) {
			map.put(pair.getKey(), pair.getValue());
		}
		return map;
	}

}
