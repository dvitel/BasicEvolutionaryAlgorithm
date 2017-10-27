package dvitel.bea;

import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

public abstract class Genotype {
	protected Hashtable<Object, Object> tags;
	private enum Tag { TEMP };
	public Genotype addTag(Object name, Object value) {
		tags.put(name, value);
		return this;
	}
	public Object getTag(Object name) {
		return tags.get(name);
	}	
	@SuppressWarnings("unchecked")
	public <T> T getTagOrDefault(Object name, Supplier<T> spl) {
		Object o = tags.get(name);
		if (o == null)
		{
			T r = spl.get();
			tags.put(name, r);
			return r;
		}
		return (T)o; 
	}		
	public Genotype addTempTag(String value, boolean noIdent) {
		try (Formatter f = new Formatter()) {
			tags.put(Tag.TEMP, f.format(noIdent ? "%s%s\r\n%s" : "%12s %s\r\n%s", "", value, tags.getOrDefault(Tag.TEMP, "")).out().toString());
		}
		return this;
	}
	public Genotype addTempTag(String value) {
		return addTempTag(value, false);
	}
	public Genotype addTempTag(Function<Genotype, String> value) {
		return addTempTag(value.apply(this));
	}	
	public String getTempTag() {
		return (String)tags.getOrDefault(Tag.TEMP, "");		
	}
	protected Genotype clearTempTag() {
		tags.remove(Tag.TEMP);
		return this;
	}		
	protected void clearTag(Class<?> cls) {
		tags.keySet().removeIf(cls::isInstance);
	}	
	public double getWeight(Fitness fitness) {
		return this.getTagOrDefault(fitness, () -> fitness.applyAsDouble(this));
	}	
	protected Genotype(Hashtable<Object, Object> tags) {
		this.tags = new Hashtable<>(tags);
		clearTag(Fitness.class);
	}
	protected Genotype() { 
		this(new Hashtable<>());
	}
	public abstract Genotype clone();
	public abstract Genotype mutateGene(int index);
	public abstract int length();
	public abstract Gene get(int index);
	public abstract List<Gene> get(int from, int toExclusive);
	public abstract Genotype construct(Stream<List<Gene>> segments);
	public abstract Gene createRandomGene(); //TODO: think - move to gene class	
	//public abstract Genotype replaceGenes(int from, int toExclusive, List<Gene> other);	
	
	public interface Fitness extends ToDoubleFunction<Genotype>{}	
	
}
