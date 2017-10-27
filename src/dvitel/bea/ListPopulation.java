package dvitel.bea;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dvitel.bea.Genotype.Fitness;

public class ListPopulation extends Population {	
	private enum Tag { INDEX }
		
	protected ArrayList<Genotype> genotypes;
	public ListPopulation(Fitness fitness, List<Genotype> population) { 
		super(fitness, population);	
	}
	@Override
	public Iterator<Genotype> iterator() {
		return genotypes.iterator();
	}
	@Override
	public Genotype getBest() {
		int index = genotypes.size() - 1;
		return genotypes.get(index).addTag(Tag.INDEX, index);
	}
	@Override
	public Genotype getWorst() {
		return genotypes.get(0).addTag(Tag.INDEX, 0);
	}
	@Override
	public Genotype getRand() {
		Random rnd = new Random();		
		int index = rnd.nextInt(genotypes.size());
		return genotypes.get(index).addTag(Tag.INDEX, index);
	}

	@Override
	protected void init(List<Genotype> population) {		
		genotypes =
			new ArrayList<Genotype>(
				population.stream()
					.map(g -> g.addTag(fitness, fitness.applyAsDouble(g)))
					.collect(Collectors.toList()));
		genotypes.sort(Comparator.comparingDouble(g -> g.getWeight(fitness)));	
	}
	
	@Override
	public Population replace(Genotype oldOne, Genotype newOne) {
		double newWeight = newOne.getWeight(fitness);
		double oldWeight = oldOne.getWeight(fitness);
		if (oldWeight < newWeight) {			
			int startIndex = oldOne.getTagOrDefault(Tag.INDEX, () -> -1);			
			if (startIndex == -1) {
				oldOne.addTempTag("NO INDEX, Brute force to search genotype");
				startIndex = 
					IntStream.range(0, genotypes.size())
						.filter(i -> Objects.equals(genotypes.get(i), oldOne))
						.findFirst().orElse(-1);				
			}
			if (startIndex == -1) {
				oldOne.addTempTag("NO INDEX, " + "[" + newOne.toString() + "]")
					.addTempTag("-------------------")
					.addTempTag(newOne.getTempTag(), true)
					.addTempTag("-------------------");				
				return this;			
			}
			int i = startIndex + 1;
			for (; i < ListPopulation.this.genotypes.size(); i++) {
				Genotype g = genotypes.get(i);
				if (g.getWeight(fitness) < newWeight) {
					//g.addTempTag("\u2191", i + " -> " + (i-1));
					genotypes.set(i-1, g);
				} else {					
					break;
				}
			}
			//int prevIndex = newOne.getTagOrDefault(Tag.INDEX, () -> -1);
			newOne
				.addTempTag("[" + newOne.toString() + "] replaced [" +  oldOne.toString() + "], " + startIndex + " -> " + (i-1) + ", " + oldWeight + " --> " + newWeight);
			genotypes.set(i-1, newOne);
		} else {
			oldOne.addTempTag("left, " + oldWeight + " > " + newWeight + ", " + "[" + newOne.toString() + "]")
				.addTempTag("-------------------")
				.addTempTag(newOne.getTempTag(), true)
				.addTempTag("-------------------");
		}
		return this;
	}		
	
}