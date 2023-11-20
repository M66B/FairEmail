// CopyrightGoogle Inc. All rights reserved.

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

package biweekly.util.com.google.ical.util;

import java.util.Collection;

/**
 * Static methods for creating the standard set of {@link Predicate} objects.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class Predicates {
	private static final Predicate<?> ALWAYS_TRUE = new AlwaysTruePredicate<Object>();
	private static final Predicate<?> ALWAYS_FALSE = new AlwaysFalsePredicate<Object>();

	/**
	 * Returns a predicate that always evaluates to true.
	 * @param <T> the input type
	 * @return the predicate
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysTrue() {
		return (Predicate<T>) ALWAYS_TRUE;
	}

	/**
	 * Returns a predicate that always evaluates to false.
	 * @param <T> the input type
	 * @return the predicate
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysFalse() {
		return (Predicate<T>) ALWAYS_FALSE;
	}

	/**
	 * Returns a predicate that evaluates to true iff the given predicate
	 * evaluates to false.
	 * @param predicate the predicate to evaluate
	 * @param <T> the input type
	 * @return the resultant predicate
	 */
	public static <T> Predicate<T> not(Predicate<? super T> predicate) {
		return new NotPredicate<T>(predicate);
	}

	/**
	 * Returns a predicate that evaluates to true iff each of its components
	 * evaluates to true. The components are evaluated in order, and evaluation
	 * will be "short-circuited" as soon as the answer is determined.
	 * @param components the predicates to evaluate
	 * @param <T> the input type
	 * @return the resultant predicate
	 */
	public static <T> Predicate<T> and(Predicate<? super T>... components) {
		components = components.clone();
		int n = components.length;
		for (int i = 0; i < n; ++i) {
			Predicate<? super T> p = components[i];
			if (p == ALWAYS_FALSE) {
				return alwaysFalse();
			}
			if (p == ALWAYS_TRUE) {
				components[i] = components[n - 1];
				--i;
				--n;
			}
		}
		if (n == 0) {
			return alwaysTrue();
		}
		if (n != components.length) {
			@SuppressWarnings("unchecked")
			Predicate<? super T>[] newComponents = new Predicate[n];
			System.arraycopy(newComponents, 0, components, 0, n);
			components = newComponents;
		}
		return new AndPredicate<T>(components);
	}

	/**
	 * Returns a predicate that evaluates to true iff each of its components
	 * evaluates to true. The components are evaluated in order, and evaluation
	 * will be "short-circuited" as soon as the answer is determined.
	 * @param components the predicates to evaluate
	 * @param <T> the input type
	 * @return the resultant predicate
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> and(Collection<Predicate<? super T>> components) {
		return and(components.toArray(new Predicate[0]));
	}

	/**
	 * Returns a predicate that evaluates to true iff any one of its components
	 * evaluates to true. The components are evaluated in order, and evaluation
	 * will be "short-circuited" as soon as the answer is determined.
	 * @param components the predicates to evaluate
	 * @param <T> the input type
	 * @return the resultant predicate
	 */
	public static <T> Predicate<T> or(Predicate<? super T>... components) {
		components = components.clone();
		int n = components.length;
		for (int i = 0; i < n; ++i) {
			Predicate<? super T> p = components[i];
			if (p == ALWAYS_TRUE) {
				return alwaysTrue();
			}
			if (p == ALWAYS_FALSE) {
				components[i] = components[n - 1];
				--i;
				--n;
			}
		}
		if (n == 0) {
			return alwaysFalse();
		}
		if (n != components.length) {
			@SuppressWarnings("unchecked")
			Predicate<? super T>[] newComponents = new Predicate[n];
			System.arraycopy(newComponents, 0, components, 0, n);
			components = newComponents;
		}
		return new OrPredicate<T>(components);
	}

	private static class AlwaysTruePredicate<T> implements Predicate<T> {
		private static final long serialVersionUID = 8759914710239461322L;

		public boolean apply(T t) {
			return true;
		}

		@Override
		public String toString() {
			return "true";
		}
	}

	private static class AlwaysFalsePredicate<T> implements Predicate<T> {
		private static final long serialVersionUID = -565481022115659695L;

		public boolean apply(T t) {
			return false;
		}

		@Override
		public String toString() {
			return "false";
		}
	}

	private static class NotPredicate<T> implements Predicate<T> {
		private static final long serialVersionUID = -5113445916422049953L;
		private final Predicate<? super T> predicate;

		private NotPredicate(Predicate<? super T> predicate) {
			this.predicate = predicate;
		}

		public boolean apply(T t) {
			return !predicate.apply(t);
		}
	}

	private static class AndPredicate<T> implements Predicate<T> {
		private static final long serialVersionUID = 1022358602593297546L;
		private final Predicate<? super T>[] components;

		private AndPredicate(Predicate<? super T>... components) {
			this.components = components;
		}

		public boolean apply(T t) {
			for (Predicate<? super T> predicate : components) {
				if (!predicate.apply(t)) {
					return false;
				}
			}
			return true;
		}
	}

	private static class OrPredicate<T> implements Predicate<T> {
		private static final long serialVersionUID = -7942366790698074803L;
		private final Predicate<? super T>[] components;

		private OrPredicate(Predicate<? super T>... components) {
			this.components = components;
		}

		public boolean apply(T t) {
			for (Predicate<? super T> predicate : components) {
				if (predicate.apply(t)) {
					return true;
				}
			}
			return false;
		}
	}

	private Predicates() {
		//uninstantiable
	}
}
