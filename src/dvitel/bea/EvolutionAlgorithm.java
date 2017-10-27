package dvitel.bea;

import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import dvitel.bea.utils.Logger;
import dvitel.bea.utils.Pair;
import dvitel.bea.utils.Param;
import dvitel.bea.utils.StreamEx;
//import dvitel.bea.utils.Logger;

public abstract class EvolutionAlgorithm {	
		
	private int numGenerations;
	private boolean printGenerationOnEachStep;
	private int recordEveryNth;
	public EvolutionAlgorithm(int numGenerations, boolean printGenerationOnEachStep, int recordEveryNth) {
		this.numGenerations = numGenerations;
		this.printGenerationOnEachStep = printGenerationOnEachStep;
		this.recordEveryNth = recordEveryNth;
	}
	public abstract Stream<Genotype> select(Population population, Selection selection);
	public abstract Stream<Genotype> breed(Stream<Genotype> selected, Crossover crossover);
	public abstract Stream<Genotype> mutate(Stream<Genotype> selected, Mutation mutation);
	public abstract Population replace(Population population, Selection selection, Stream<Genotype> withSelected);
	
	public Population run(Population initial, 
			Selection selection, Crossover crossover, Mutation mutation, 
			Selection replacement, Logger logger, Logger dataLogger)
	{
		return 
			StreamEx.indexed(
				Stream.iterate(initial, p -> 
					{
						StreamSupport.stream(p.spliterator(), true)
							.forEach(g -> g.clearTempTag());
						Stream<Genotype> selected = 
							select(p, selection)
								.map(s -> s.clone().addTempTag(g -> "[" + g.toString() + "] selected"));
								//.peek(g -> Logger.info("SELECTED: " + g.toString()));
						Stream<Genotype> breeded = 
								breed(selected, crossover);
									//.map(s -> s.addTempTag(g -> "[" + g.toString() + "] breed result"));
						Stream<Genotype> mutated = mutate(breeded, mutation);									
						return replace(p, replacement, mutated);
					}))
				.peek(p -> {
					if (printGenerationOnEachStep)
						logger.info("GEN: " + p.first + "\r\n" + p.second.toString());
				})
				.peek(p -> {
					if (p.first % recordEveryNth == 0)
						dataLogger.data(p.first, p.second.getBest().getWeight(p.second.fitness));
				})
				.limit(numGenerations)
				.reduce((p, l) -> l)
				.map(o -> o.second)
				.orElse(initial);
	}	
	
	public static class SteadyState extends EvolutionAlgorithm {
		private int selectCount;
		public SteadyState(int numGenerations, int selectCount, boolean printGenerationOnEachStep, int recordEveryNth) {
			super(numGenerations, printGenerationOnEachStep, recordEveryNth);
			this.selectCount = selectCount;
		}
		@Override
		public Stream<Genotype> breed(Stream<Genotype> genotypes, Crossover crossover){
			return
				StreamEx.pairwise(genotypes)
			    	.map(parents -> crossover.apply(parents.first, parents.second))
			    	.flatMap(offsprings -> 
			    			Stream.<Genotype>builder()
			    				.add(offsprings.first)
			    				.add(offsprings.second)
			    				.build());						    	
		}
		
		@Override
		public Stream<Genotype> mutate(Stream<Genotype> slctd, Mutation mutation) {
			return 
				slctd
					.map(g -> Pair.create(g.toString(), mutation.apply(g)))
					.map(p -> 
						p.second
							.addTempTag("[" + p.second.toString() + "] mutated from [" + p.first + "]"));			
		}		
		
		@Override
		public Stream<Genotype> select(Population population, Selection selection) {
			return Stream.generate(() -> selection.apply(population))
					.limit(selectCount).unordered();
		}
		
		@Override
		public Population replace(final Population population, Selection selection, Stream<Genotype> withReplace) {
			return 
				withReplace
					.sequential()
					.collect(Collector.of(
							() -> population,
							(p, g) -> p.replace(selection.apply(p), g),
							(p1, p2) -> p2));			
		}		
		
		public static EvolutionAlgorithm create(@Param(name = "numGenerations") int numGenerations, 
				@Param(name = "selectCount") int selectCount, 
				@Param(name = "printGen") boolean printGen,
				@Param(name = "recordEveryNth") int recordEveryNth) {
			return new SteadyState(numGenerations, selectCount, printGen, recordEveryNth);
		}
		
	}
	
}
