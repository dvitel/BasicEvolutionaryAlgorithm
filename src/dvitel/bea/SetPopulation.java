package dvitel.bea;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import dvitel.bea.Genotype.Fitness;

public class SetPopulation extends Population { 
	SortedSet<Genotype> set;
	
	protected SetPopulation(Fitness fitness, List<Genotype> population) {
		super(fitness, population);		
	}

	@Override
	public Iterator<Genotype> iterator() {
		return set.iterator();
	}

	@Override
	protected void init(List<Genotype> population) {
		set = new TreeSet<>(Comparator.comparing(g -> g.getWeight(fitness)));
		population.stream().forEach(set::add);
	}

	@Override
	public Genotype getBest() {
		return set.last();
	}

	@Override
	public Genotype getWorst() {
		return set.first();
	}

	@Override
	public Genotype getRand() {
		return 
			set.stream()
				.limit((new Random()).nextInt(set.size()))
				.reduce((p, l) -> l)
				.orElse(null);
	}

	@Override
	public Population replace(Genotype oldOne, Genotype newOne) {
		if (oldOne.getWeight(fitness) < newOne.getWeight(fitness)) {
			set.remove(oldOne);
			set.add(newOne);		
		}
		return this;
	}	

}
