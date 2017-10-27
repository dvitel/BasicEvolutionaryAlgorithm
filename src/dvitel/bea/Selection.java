package dvitel.bea;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

import dvitel.bea.utils.Param;

@FunctionalInterface
public interface Selection
	extends Function<Population, Genotype> {
	public static Genotype Tournament(@Param(name = "tournamentSize") int tournamentSize, Population population) {
		return 
			Stream.generate(() -> population.getRand())
				.limit(tournamentSize)
				.max(Comparator.comparingDouble(s -> s.getWeight(population.fitness)))
				.orElse(null);
	}	
	public static Genotype Worst(Population population) {
		return population.getWorst();
	}
	public static Genotype Best(Population population) {
		return population.getBest();
	}		
}
