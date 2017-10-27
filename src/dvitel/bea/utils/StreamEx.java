package dvitel.bea.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class StreamEx {
	//https://stackoverflow.com/questions/17640754/zipping-streams-using-jdk8-with-lambda-java-util-stream-streams-zip
	public static<A, B> Stream<Pair<A, B>> zip(Stream<? extends A> a,
            Stream<? extends B> b) {
		Spliterator<? extends A> aSpliterator = Objects.requireNonNull(a).spliterator();
		Spliterator<? extends B> bSpliterator = Objects.requireNonNull(b).spliterator();
		
		// Zipping looses DISTINCT and SORTED characteristics
		int characteristics = 
			aSpliterator.characteristics() 
			& bSpliterator.characteristics() 
			& ~(Spliterator.DISTINCT | Spliterator.SORTED);
		
		long zipSize = 
			((characteristics & Spliterator.SIZED) != 0)
				? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
				: -1;
				
		Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
		Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
		Iterator<Pair<A, B>> cIterator = new Iterator<Pair<A, B>>() {
				@Override
				public boolean hasNext() {
					return aIterator.hasNext() && bIterator.hasNext();
				}
				@Override
				public Pair<A, B> next() {
					return Pair.create(aIterator.next(), bIterator.next());
				}
			};
		Spliterator<Pair<A, B>> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
		return (a.isParallel() || b.isParallel())
		? StreamSupport.stream(split, true)
		: StreamSupport.stream(split, false);
	}

	public static<A, B> A fold(A acc, Iterator<B> stream, BiFunction<A, B, A> func) {
		if (stream.hasNext()) return fold(func.apply(acc, stream.next()), stream, func);
		else return acc;
	}
	
	public static<A, B, S extends BaseStream<B, S>> A fold(A acc, BaseStream<B, S> stream, BiFunction<A, B, A> func) {
		return fold(acc, stream.iterator(), func);
	}
	
	public static<A, B> Pair<Stream<A>, Stream<B>> unzip(Stream<Pair<A, B>> a) {
		Spliterator<Pair<A, B>> aSpliterator = Objects.requireNonNull(a).spliterator();
		
		// Zipping looses DISTINCT and SORTED characteristics
		int characteristics = 
			aSpliterator.characteristics() 
			& ~(Spliterator.DISTINCT | Spliterator.SORTED);
		
		long size = 
			((characteristics & Spliterator.SIZED) != 0)
				? aSpliterator.getExactSizeIfKnown()
				: -1;
				
		Iterator<Pair<A, B>> aIterator = Spliterators.iterator(aSpliterator);
		Queue<A> queue = new LinkedList<A>();
		Queue<B> altQueue = new LinkedList<B>();					
		Iterator<A> bIterator = 
			new Iterator<A>() {
				@Override
				public boolean hasNext() {
					return (queue.size() > 0) || aIterator.hasNext();
				}
				@Override
				public A next() {
					if (queue.size() > 0) return queue.poll();
					else  {
						Pair<A, B> n = aIterator.next();
						altQueue.add(n.second);
						return n.first;
					}
				}
			};
		Iterator<B> cIterator = 
			new Iterator<B>() {
				@Override
				public boolean hasNext() {
					return (altQueue.size() > 0) || aIterator.hasNext();
				}
				@Override
				public B next() {
					if (altQueue.size() > 0) return altQueue.poll();
					else  {
						Pair<A, B> n = aIterator.next();
						queue.add(n.first);
						return n.second;
					}
				}
			};			
		return Pair.create(
				StreamSupport.stream(
					Spliterators.spliterator(bIterator, size, characteristics), a.isParallel()),
				StreamSupport.stream(
					Spliterators.spliterator(cIterator, size, characteristics), a.isParallel()));					
	}		
	
	private interface PartitionIteratorCreator<A> {
		public Iterator<A> create(Predicate<A> predicate, Queue<A> queue, Queue<A> altQueue);
	}
	
	public static<A> Pair<Stream<A>, Stream<A>> partition(
			Stream<? extends A> a,
            Predicate<A> p
			) {
		Objects.requireNonNull(p);
		Spliterator<? extends A> aSpliterator = Objects.requireNonNull(a).spliterator();
		
		int characteristics = aSpliterator.characteristics();	
		long size = 
			((characteristics & Spliterator.SIZED) != 0)
				? aSpliterator.getExactSizeIfKnown()
				: -1;
				
		Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
		PartitionIteratorCreator<A> b =
			(predicate, queue, altQueue) ->
				new Iterator<A>() {				
						@Override
						public boolean hasNext() {
							if (queue.size() > 0) return true;
							while (aIterator.hasNext()) {
								A element = null;
								if (predicate.test(element = aIterator.next())) {
									queue.add(element);
									return true;
								}
								else 
									altQueue.add(element);
							}
							return false;
						}
						@Override
						public A next() {
							A result = queue.poll();
							if (result == null) 
							{
								if (hasNext()) {
									result = queue.poll();
								} else {
									throw new NoSuchElementException();
								}
							}
							return result;
						}
					};	
		Queue<A> queue = new LinkedList<A>();
		Queue<A> altQueue = new LinkedList<A>();					
		return 
			Pair.create(
				StreamSupport.stream(
					Spliterators.spliterator(
						b.create(p, queue, altQueue), size, characteristics), a.isParallel()), 
				StreamSupport.stream(
						Spliterators.spliterator(
							b.create(p.negate(), altQueue, queue), size, characteristics), a.isParallel()));
	}	
	
	public static <T> Stream<T> shuffle(List<T> data){
		Set<Integer> availableIndexes = new HashSet<Integer>();
		Random rand = new Random();
		IntStream.range(0, data.size()).forEach(availableIndexes::add);
		return 		
			IntStream.range(0, data.size())
				.map(i -> availableIndexes.toArray(new Integer[] {})[rand.nextInt(availableIndexes.size())])	
				.peek(availableIndexes::remove)
				.mapToObj(data::get);
	}
	
	public static <T> Stream<Pair<Integer, T>> indexed(Stream<T> data) {
		return zip(IntStream.iterate(0, i -> i + 1).boxed(), data);
	}
	
	public static <T> Stream<Pair<T, T>> pairwise(Stream<T> data){
		return 
			partition(indexed(data), p -> p.first % 2 == 0)
				.map((f, s) -> zip(f, s))
				.map(p -> Pair.create(p.first.second, p.second.second));
	}	
	
	public static <T> Stream<Pair<T, T>> window2(Stream<? extends T> a) {
		Spliterator<? extends T> aSpliterator = Objects.requireNonNull(a).spliterator();
		
		int characteristics = aSpliterator.characteristics();	
		long size = 
			((characteristics & Spliterator.SIZED) != 0)
				? aSpliterator.getExactSizeIfKnown()
				: -1;
				
		Iterator<T> aIterator = Spliterators.iterator(aSpliterator);
		Iterator<Pair<T, T>> b =
			new Iterator<Pair<T, T>>() {		
					T fetched = null;
					@Override
					public boolean hasNext() {
						return aIterator.hasNext();
					}
					@Override
					public Pair<T, T> next() {
						if (fetched == null)
						{
							T first = aIterator.next();
							if (aIterator.hasNext()) 
							{
								fetched = aIterator.next();
								return Pair.create(first, fetched);
							} else {
								fetched = first; 
								return Pair.create(fetched, null);
							}
						} else {
							T prev = fetched;
							fetched = aIterator.next();
							return Pair.create(prev, fetched);
						}
					}
				};	
		return 
			StreamSupport.stream(
				Spliterators.spliterator(
					b, size > 1 ? size - 1 : size, characteristics), false);
	}	
}
