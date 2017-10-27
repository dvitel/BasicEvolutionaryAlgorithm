package dvitel.bea;

import java.util.Formatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import dvitel.bea.Genotype.Fitness;
import dvitel.bea.utils.Pair;
import dvitel.bea.utils.StreamEx;

/* again genotypes are ordered - so we can get at index */
public abstract class Population implements Iterable<Genotype> {	
	protected Fitness fitness;

	protected abstract void init(List<Genotype> population);
	public abstract Genotype getBest();
	public abstract Genotype getWorst();
	public abstract Genotype getRand();
	public abstract Population replace(Genotype oldOne, Genotype newOne);
	protected Population(Fitness fitness, List<Genotype> population) {
		this.fitness = fitness;
		init(population);
	}		
	
	@Override
	public String toString() {
		return 
			"[\r\n" +
				StreamEx.indexed(StreamSupport.stream(this.spliterator(), false))
					.map(g ->{
						try (Formatter f = new Formatter())
						{				
						String tempTag = g.second.getTempTag();
						return Pair.create(g.second, 
							f.format("%3d %8.1f [%s]%s%s", 
								g.first, 
								g.second.getWeight(fitness), 
								g.second.toString(),
								"".equals(tempTag) ? "" : "\r\n",
								tempTag
								).out().toString());
						}
					})
					.map(p -> p.second)
					.collect(Collectors.joining("\r\n"))
			+ "\r\n]";		
	}
	
}