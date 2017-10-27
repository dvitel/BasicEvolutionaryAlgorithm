package dvitel.bea;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dvitel.bea.utils.Param;
import dvitel.bea.utils.StreamEx;

//create simple alias
@FunctionalInterface
public interface Mutation extends Function<Genotype,Genotype>  {

	public static Genotype PerGene(@Param(name = "rate") double rate, Genotype genotype) {
		Random rand = new Random();
		return 
			StreamEx.fold(genotype, 
				IntStream.range(0, genotype.length())
					.filter(i -> rand.nextDouble() <= rate), 
				(a, i) -> a.mutateGene(i));		
	}
	public static Genotype PerGeneWithLength(@Param(name = "rate") double rate, 
			@Param(name = "minLength") int minLength, 
			@Param(name = "maxLength") int maxLength, Genotype genotype) {
		Random rand = new Random();		
		Genotype mutated = PerGene(rate, genotype);
		if (rand.nextDouble() <= rate) {
			int newLength = rand.nextInt(maxLength - minLength) + minLength;
			mutated = 
				(newLength > mutated.length()) ?
					mutated.construct(
						Stream.<List<Gene>>builder()
							.add(mutated.get(0, mutated.length()))
							.add(
								IntStream.range(mutated.length(), newLength)
									.mapToObj(i -> genotype.createRandomGene()).filter(g -> g != null)
										.collect(Collectors.toList()))
							.build())
				: 
					mutated.construct(Stream.of(mutated.get(0, newLength)));							
		}
		return mutated;
	}	
}
