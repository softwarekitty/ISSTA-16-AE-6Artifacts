package main.core.categories;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * a TreeSet where attempts to remove an element throw an exception.
 * 
 * This may be breaking the Collections contract, in spirit at least.
 * 
 * It is handy for implementing sets without complexity of removal - it's a
 * temporary measure with nice properties.
 * 
 * @author cc
 *
 * @param <E>
 */
public class UnremovableTreeSet<E> extends TreeSet<E> {
	private static final long serialVersionUID = -1378317793938877526L;

	public UnremovableTreeSet() {
		super();
	}

	public UnremovableTreeSet(Collection<? extends E> c) {
		super(c);
	}

	public UnremovableTreeSet(Comparator<? super E> comparator) {
		super(comparator);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("clear not allowed");
	}

	@Override
	public Iterator<E> iterator() {
		Iterator<E> defaultIterator = super.iterator();
		return new UnremovableIterator(defaultIterator);
	}

	@Override
	public Iterator<E> descendingIterator() {
		Iterator<E> defaultIterator = super.descendingIterator();
		return new UnremovableIterator(defaultIterator);
	}

	@Override
	public E pollFirst() {
		throw new UnsupportedOperationException("removing by pollFirst not allowed");
	}

	@Override
	public E pollLast() {
		throw new UnsupportedOperationException("removing by pollLast not allowed");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("removing not allowed");
	}

	class UnremovableIterator implements Iterator<E> {
		private Iterator<E> givenIterator;

		public UnremovableIterator(Iterator<E> givenIterator) {
			this.givenIterator = givenIterator;
		}

		@Override
		public boolean hasNext() {
			return givenIterator.hasNext();
		}

		@Override
		public E next() {
			return givenIterator.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("removing by iterator not allowed");
		}

	}
}
