package biweekly.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/*
 Copyright (c) 2013-2023, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A multimap that uses {@link ArrayList} objects to store its values. The
 * internal {@link Map} implementation is a {@link LinkedHashMap}.
 * @author Michael Angstadt
 * @param <K> the key
 * @param <V> the value
 */
public class ListMultimap<K, V> implements Iterable<Map.Entry<K, List<V>>> {
	private final Map<K, List<V>> map;

	/**
	 * Creates an empty multimap.
	 */
	public ListMultimap() {
		this(new LinkedHashMap<K, List<V>>());
	}

	/**
	 * Creates an empty multimap.
	 * @param initialCapacity the initial capacity of the underlying map.
	 */
	public ListMultimap(int initialCapacity) {
		this(new LinkedHashMap<K, List<V>>(initialCapacity));
	}

	/**
	 * Creates a copy of an existing multimap.
	 * @param orig the multimap to copy from
	 */
	public ListMultimap(ListMultimap<K, V> orig) {
		this(copy(orig.map));
	}

	private static <K, V> Map<K, List<V>> copy(Map<K, List<V>> orig) {
		Map<K, List<V>> map = new LinkedHashMap<K, List<V>>(orig.size());
		for (Map.Entry<K, List<V>> entry : orig.entrySet()) {
			List<V> values = new ArrayList<V>(entry.getValue());
			map.put(entry.getKey(), values);
		}
		return map;
	}

	/**
	 * <p>
	 * Creates a new multimap backed by the given map. Changes made to the given
	 * map will effect the multimap and vice versa.
	 * </p>
	 * <p>
	 * To avoid problems, it is highly recommended that the given map NOT be
	 * modified by anything other than this {@link ListMultimap} class after
	 * being passed into this constructor.
	 * </p>
	 * @param map the backing map
	 */
	public ListMultimap(Map<K, List<V>> map) {
		this.map = map;
	}

	/**
	 * Adds a value to the multimap.
	 * @param key the key
	 * @param value the value to add
	 */
	public void put(K key, V value) {
		key = sanitizeKey(key);
		List<V> list = map.get(key);
		if (list == null) {
			list = new ArrayList<V>();
			map.put(key, list);
		}
		list.add(value);
	}

	/**
	 * Adds multiple values to the multimap.
	 * @param key the key
	 * @param values the values to add
	 */
	public void putAll(K key, Collection<? extends V> values) {
		if (values.isEmpty()) {
			return;
		}

		key = sanitizeKey(key);
		List<V> list = map.get(key);
		if (list == null) {
			list = new ArrayList<V>();
			map.put(key, list);
		}
		list.addAll(values);
	}

	/**
	 * Gets the values associated with the key. Changes to the returned list
	 * will update the underlying multimap, and vice versa.
	 * @param key the key
	 * @return the list of values or empty list if the key doesn't exist
	 */
	public List<V> get(K key) {
		key = sanitizeKey(key);
		List<V> value = map.get(key);
		if (value == null) {
			value = new ArrayList<V>(0);
		}
		return new WrappedList(key, value, null);
	}

	/**
	 * Gets the first value that's associated with a key.
	 * @param key the key
	 * @return the first value or null if the key doesn't exist
	 */
	public V first(K key) {
		key = sanitizeKey(key);
		List<V> values = map.get(key);

		/*
		 * The list can be null, but never empty. Empty lists are removed from
		 * the map.
		 */
		return (values == null) ? null : values.get(0);
	}

	/**
	 * Determines whether the given key exists.
	 * @param key the key
	 * @return true if the key exists, false if not
	 */
	public boolean containsKey(K key) {
		key = sanitizeKey(key);
		return map.containsKey(key);
	}

	/**
	 * Removes a particular value.
	 * @param key the key
	 * @param value the value to remove
	 * @return true if the multimap contained the value, false if not
	 */
	public boolean remove(K key, V value) {
		key = sanitizeKey(key);
		List<V> values = map.get(key);
		if (values == null) {
			return false;
		}

		boolean success = values.remove(value);
		if (values.isEmpty()) {
			map.remove(key);
		}
		return success;
	}

	/**
	 * Removes all the values associated with a key
	 * @param key the key to remove
	 * @return the removed values or an empty list if the key doesn't exist
	 * (this list is immutable)
	 */
	public List<V> removeAll(K key) {
		key = sanitizeKey(key);
		List<V> removed = map.remove(key);
		if (removed == null) {
			return Collections.emptyList();
		}

		List<V> unmodifiableCopy = Collections.unmodifiableList(new ArrayList<V>(removed));
		removed.clear();
		return unmodifiableCopy;
	}

	/**
	 * Replaces all values with the given value.
	 * @param key the key
	 * @param value the value with which to replace all existing values, or null
	 * to remove all values
	 * @return the values that were replaced (this list is immutable)
	 */
	public List<V> replace(K key, V value) {
		List<V> replaced = removeAll(key);
		if (value != null) {
			put(key, value);
		}
		return replaced;
	}

	/**
	 * Replaces all values with the given values.
	 * @param key the key
	 * @param values the values with which to replace all existing values
	 * @return the values that were replaced (this list is immutable)
	 */
	public List<V> replace(K key, Collection<? extends V> values) {
		List<V> replaced = removeAll(key);
		putAll(key, values);
		return replaced;
	}

	/**
	 * Clears all entries from the multimap.
	 */
	public void clear() {
		//clear each collection to make previously returned lists empty
		for (List<V> value : map.values()) {
			value.clear();
		}
		map.clear();
	}

	/**
	 * Gets all the keys in the multimap.
	 * @return the keys (this set is immutable)
	 */
	public Set<K> keySet() {
		return Collections.unmodifiableSet(map.keySet());
	}

	/**
	 * Gets all the values in the multimap.
	 * @return the values (this list is immutable)
	 */
	public List<V> values() {
		List<V> list = new ArrayList<V>();
		for (List<V> value : map.values()) {
			list.addAll(value);
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Determines if the multimap is empty or not.
	 * @return true if it's empty, false if not
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Gets the number of values in the map.
	 * @return the number of values
	 */
	public int size() {
		int size = 0;
		for (List<V> value : map.values()) {
			size += value.size();
		}
		return size;
	}

	/**
	 * Gets an immutable view of the underlying {@link Map} object.
	 * @return an immutable map
	 */
	public Map<K, List<V>> asMap() {
		Map<K, List<V>> view = new LinkedHashMap<K, List<V>>(map.size());
		for (Map.Entry<K, List<V>> entry : map.entrySet()) {
			K key = entry.getKey();
			List<V> value = entry.getValue();
			view.put(key, Collections.unmodifiableList(value));
		}
		return Collections.unmodifiableMap(view);
	}

	/**
	 * Gets the {@link Map} that backs this multimap. This method is here for
	 * performances reasons. The returned map should NOT be modified by anything
	 * other than the {@link ListMultimap} object that owns it.
	 * @return the map
	 */
	public Map<K, List<V>> getMap() {
		return map;
	}

	/**
	 * Modifies a given key before it is used to interact with the internal map.
	 * This method is meant to be overridden by child classes if necessary.
	 * @param key the key
	 * @return the modified key (by default, the key is returned as-is)
	 */
	protected K sanitizeKey(K key) {
		return key;
	}

	/**
	 * Gets an iterator for iterating over the entries in the map. This iterator
	 * iterates over an immutable view of the map.
	 * @return the iterator
	 */
	//@Override
	public Iterator<Map.Entry<K, List<V>>> iterator() {
		final Iterator<Map.Entry<K, List<V>>> it = map.entrySet().iterator();
		return new Iterator<Map.Entry<K, List<V>>>() {
			public boolean hasNext() {
				return it.hasNext();
			}

			public Entry<K, List<V>> next() {
				final Entry<K, List<V>> next = it.next();
				return new Entry<K, List<V>>() {
					public K getKey() {
						return next.getKey();
					}

					public List<V> getValue() {
						return Collections.unmodifiableList(next.getValue());
					}

					public List<V> setValue(List<V> value) {
						throw new UnsupportedOperationException();
					}
				};
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		ListMultimap<?, ?> other = (ListMultimap<?, ?>) obj;
		return map.equals(other.map);
	}

	/**
	 * Note: This class is a modified version of the
	 * "AbstractMapBasedMultimap.WrappedList" class from the
	 * <a href="https://github.com/google/guava">Guava</a>.
	 * 
	 * <p>
	 * Collection decorator that stays in sync with the multimap values for a
	 * key. There are two kinds of wrapped collections: full and subcollections.
	 * Both have a delegate pointing to the underlying collection class.
	 *
	 * <p>
	 * Full collections, identified by a null ancestor field, contain all
	 * multimap values for a given key. Its delegate is a value in the
	 * multimap's underlying {@link Map} whenever the delegate is non-empty. The
	 * {@code refreshIfEmpty}, {@code removeIfEmpty}, and {@code addToMap}
	 * methods ensure that the {@code WrappedList} and map remain consistent.
	 *
	 * <p>
	 * A subcollection, such as a sublist, contains some of the values for a
	 * given key. Its ancestor field points to the full wrapped collection with
	 * all values for the key. The subcollection {@code refreshIfEmpty},
	 * {@code removeIfEmpty}, and {@code addToMap} methods call the
	 * corresponding methods of the full wrapped collection.
	 */
	private class WrappedList extends AbstractCollection<V> implements List<V> {
		final K key;
		List<V> delegate;
		final WrappedList ancestor;
		final List<V> ancestorDelegate;

		WrappedList(K key, List<V> delegate, WrappedList ancestor) {
			this.key = key;
			this.delegate = delegate;
			this.ancestor = ancestor;
			this.ancestorDelegate = (ancestor == null) ? null : ancestor.getDelegate();
		}

		public boolean addAll(int index, Collection<? extends V> collection) {
			if (collection.isEmpty()) {
				return false;
			}
			int oldSize = size(); // calls refreshIfEmpty
			boolean changed = getDelegate().addAll(index, collection);
			if (changed && oldSize == 0) {
				addToMap();
			}
			return changed;
		}

		public V get(int index) {
			refreshIfEmpty();
			return getDelegate().get(index);
		}

		public V set(int index, V element) {
			refreshIfEmpty();
			return getDelegate().set(index, element);
		}

		public void add(int index, V element) {
			refreshIfEmpty();
			boolean wasEmpty = getDelegate().isEmpty();
			getDelegate().add(index, element);
			if (wasEmpty) {
				addToMap();
			}
		}

		public V remove(int index) {
			refreshIfEmpty();
			V value = getDelegate().remove(index);
			removeIfEmpty();
			return value;
		}

		public int indexOf(Object o) {
			refreshIfEmpty();
			return getDelegate().indexOf(o);
		}

		public int lastIndexOf(Object o) {
			refreshIfEmpty();
			return getDelegate().lastIndexOf(o);
		}

		public ListIterator<V> listIterator() {
			refreshIfEmpty();
			return new WrappedListIterator();
		}

		public ListIterator<V> listIterator(int index) {
			refreshIfEmpty();
			return new WrappedListIterator(index);
		}

		public List<V> subList(int fromIndex, int toIndex) {
			refreshIfEmpty();
			return new WrappedList(getKey(), getDelegate().subList(fromIndex, toIndex), (getAncestor() == null) ? this : getAncestor());
		}

		/**
		 * If the delegate collection is empty, but the multimap has values for
		 * the key, replace the delegate with the new collection for the key.
		 *
		 * <p>
		 * For a subcollection, refresh its ancestor and validate that the
		 * ancestor delegate hasn't changed.
		 */
		void refreshIfEmpty() {
			if (ancestor != null) {
				ancestor.refreshIfEmpty();
				if (ancestor.getDelegate() != ancestorDelegate) {
					throw new ConcurrentModificationException();
				}
			} else if (delegate.isEmpty()) {
				List<V> newDelegate = map.get(key);
				if (newDelegate != null) {
					delegate = newDelegate;
				}
			}
		}

		/**
		 * If collection is empty, remove it from
		 * {@code AbstractMapBasedMultimap.this.map}. For subcollections, check
		 * whether the ancestor collection is empty.
		 */
		void removeIfEmpty() {
			if (ancestor != null) {
				ancestor.removeIfEmpty();
			} else if (delegate.isEmpty()) {
				map.remove(key);
			}
		}

		K getKey() {
			return key;
		}

		/**
		 * Add the delegate to the map. Other {@code WrappedCollection} methods
		 * should call this method after adding elements to a previously empty
		 * collection.
		 *
		 * <p>
		 * Subcollection add the ancestor's delegate instead.
		 */
		void addToMap() {
			if (ancestor != null) {
				ancestor.addToMap();
			} else {
				map.put(key, delegate);
			}
		}

		@Override
		public int size() {
			refreshIfEmpty();
			return delegate.size();
		}

		@Override
		public boolean equals(Object object) {
			if (object == this) {
				return true;
			}
			refreshIfEmpty();
			return delegate.equals(object);
		}

		@Override
		public int hashCode() {
			refreshIfEmpty();
			return delegate.hashCode();
		}

		@Override
		public String toString() {
			refreshIfEmpty();
			return delegate.toString();
		}

		List<V> getDelegate() {
			return delegate;
		}

		@Override
		public Iterator<V> iterator() {
			refreshIfEmpty();
			return new WrappedListIterator();
		}

		@Override
		public boolean add(V value) {
			refreshIfEmpty();
			boolean wasEmpty = delegate.isEmpty();
			boolean changed = delegate.add(value);
			if (changed && wasEmpty) {
				addToMap();
			}
			return changed;
		}

		WrappedList getAncestor() {
			return ancestor;
		}

		// The following methods are provided for better performance.

		@Override
		public boolean addAll(Collection<? extends V> collection) {
			if (collection.isEmpty()) {
				return false;
			}
			int oldSize = size(); // calls refreshIfEmpty
			boolean changed = delegate.addAll(collection);
			if (changed && oldSize == 0) {
				addToMap();
			}
			return changed;
		}

		@Override
		public boolean contains(Object o) {
			refreshIfEmpty();
			return delegate.contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			refreshIfEmpty();
			return delegate.containsAll(c);
		}

		@Override
		public void clear() {
			int oldSize = size(); // calls refreshIfEmpty
			if (oldSize == 0) {
				return;
			}
			delegate.clear();
			removeIfEmpty(); // maybe shouldn't be removed if this is a sublist
		}

		@Override
		public boolean remove(Object o) {
			refreshIfEmpty();
			boolean changed = delegate.remove(o);
			if (changed) {
				removeIfEmpty();
			}
			return changed;
		}

		@Override
		public boolean removeAll(Collection<?> collection) {
			if (collection.isEmpty()) {
				return false;
			}
			refreshIfEmpty();
			boolean changed = delegate.removeAll(collection);
			if (changed) {
				removeIfEmpty();
			}
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			refreshIfEmpty();
			boolean changed = delegate.retainAll(c);
			if (changed) {
				removeIfEmpty();
			}
			return changed;
		}

		/** ListIterator decorator. */
		private class WrappedListIterator implements ListIterator<V> {
			final ListIterator<V> delegateIterator;
			final List<V> originalDelegate = delegate;

			WrappedListIterator() {
				delegateIterator = delegate.listIterator();
			}

			public WrappedListIterator(int index) {
				delegateIterator = delegate.listIterator(index);
			}

			public boolean hasPrevious() {
				return getDelegateIterator().hasPrevious();
			}

			public V previous() {
				return getDelegateIterator().previous();
			}

			public int nextIndex() {
				return getDelegateIterator().nextIndex();
			}

			public int previousIndex() {
				return getDelegateIterator().previousIndex();
			}

			public void set(V value) {
				getDelegateIterator().set(value);
			}

			public void add(V value) {
				boolean wasEmpty = isEmpty();
				getDelegateIterator().add(value);
				if (wasEmpty) {
					addToMap();
				}
			}

			/**
			 * If the delegate changed since the iterator was created, the
			 * iterator is no longer valid.
			 */
			void validateIterator() {
				refreshIfEmpty();
				if (delegate != originalDelegate) {
					throw new ConcurrentModificationException();
				}
			}

			public boolean hasNext() {
				validateIterator();
				return delegateIterator.hasNext();
			}

			public V next() {
				validateIterator();
				return delegateIterator.next();
			}

			public void remove() {
				delegateIterator.remove();
				removeIfEmpty();
			}

			ListIterator<V> getDelegateIterator() {
				validateIterator();
				return delegateIterator;
			}
		}
	}
}
