package dvitel.bea;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
public class TreeGenotype extends Genotype {
	public static class Operation {
		private Function<List<Object>, Object> call;
		private TreeMap<Integer, Object> bindings;
		private int arity;
		private String name;
		private Operation(Function<List<Object>, Object> call, int arity, String name, 
					TreeMap<Integer, Object> bindings) {			
			this.call = call;
			this.arity = arity;
			this.bindings = bindings;
			this.name = name;
		}
		public Operation(Function<List<Object>, Object> call, int arity, String name) {
			this(call, arity, name, new TreeMap<>());
		}
		public Operation bind(Operation other, int position) {
			if (position < 0 || position >= arity) return this;
			bindings.put(position, other);
			int newArity = arity - 1 + other.arity;
			return new Operation(call, newArity, name);
		}
		public Operation bind(Object argument, int position) {
			if (position < 0 || position >= arity) return this;
			int newArity = arity - 1;
			return new Operation(call, newArity, name);
		}
		public Object call(List<Object> freeArgs) {
			List<Object> safeFreeArgs = freeArgs.subList(0, arity);
			ArrayList<Supplier<Object>> allArgs = new ArrayList<Supplier<Object>>();
			int currentIndex = 0;
			int prevBindingIndex = 0;
			for (Integer bindingIndex: bindings.keySet()) {
				allArgs.addAll(safeFreeArgs.subList(prevBindingIndex, bindingIndex));
				prevBindingIndex = bindingIndex;
				if (bindings.get(bindingIndex) instanceof Operation) {
					Operation op = (Operation)bindings.get(bindingIndex);
					allArgs.add(op.call(safeFreeArgs.subList(currentIndex, currentIndex + op.arity)));
					currentIndex = currentIndex + op.arity;
				} else {
					allArgs.add(bindings.get(bindingIndex));
				}
			}				
			return call.apply(allArgs);
		}
		@Override 
		public String toString() {
			return "[" + name + ":\r\n" + IntStream.range(0, arity).mapToObj(p -> {
				if (bindedOperations.containsKey(p)) {
					return "\t" + p + ": " + bindedOperations.get(p).toString();
				} else {
					return "\t" + p + ": X";
				}
			}).collect(Collectors.joining("\r\n"));
		}
	}
	private Operation tree;
	private List<Function<List<Object>, Object>> domain;
	public TreeGenotype(Operation tree, List<Function<List<Object>, Object>> domain) {
		this.tree = tree;
		this.domain = domain;
	}
	@Override
	public Genotype clone() {
		return new TreeGenotype(tree, domain);
	}
	@Override
	public Genotype mutateGene(int index) {
		return null;
	}
	@Override
	public int length() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public Gene get(int index) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<Gene> get(int from, int toExclusive) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Genotype construct(Stream<List<Gene>> segments) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Gene createRandomGene() {
		// TODO Auto-generated method stub
		return null;
	}
}*/
