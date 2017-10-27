package dvitel.bea;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dvitel.bea.utils.Pair;
import dvitel.bea.utils.Param;
import dvitel.bea.utils.StreamEx;

@FunctionalInterface
public interface Crossover
	extends BiFunction<Genotype, Genotype, Pair<Genotype, Genotype>> {
	
	public static Stream<List<Gene>> splitGenotype(Genotype genotype, Stream<Integer> splitPoints) {
		return 
			StreamEx.window2(
				Stream.concat(Stream.of(0), 
					Stream.concat(splitPoints, Stream.of(genotype.length()))))
			.map(p -> 
				genotype.get(p.first, p.second));
	}

	public static Pair<Genotype, Genotype> Default(@Param(name = "points") int points, @Param(name = "rate") double rate, Genotype parent1, Genotype parent2) {	
		if (Objects.equals(parent1, parent2) || parent1.length() == 0 || parent2.length() == 0) {
			return Pair.create(parent1.addTempTag("[" + parent1.toString() + "][" + parent2.toString() + "] Crossover skipped"), 
				parent2.addTempTag("[" + parent1.toString() + "][" + parent2.toString() + "] Crossover skipped"));
		}
		Random rand = new Random();
		double chanceToBreed = rand.nextDouble();
		if (chanceToBreed < rate) {			
			int maxIndex = Math.max(parent1.length(), parent2.length());
			List<Integer> splitPoints = 
				StreamEx.shuffle(IntStream.range(0, maxIndex).boxed().collect(Collectors.toList()))
					.limit(points)					
					.collect(Collectors.toList());
			List<List<Gene>> parent1Segments = 
				splitGenotype(parent1, splitPoints.stream())
					//.peek(p -> Logger.info("SP: " + p.toString()))
					.collect(Collectors.toList());
			List<List<Gene>> parent2Segments = 
				splitGenotype(parent2, splitPoints.stream())
					//.peek(p -> Logger.info("SP: " + p.toString()))
					.collect(Collectors.toList());			
			Stream<Pair<List<Gene>, List<Gene>>> breededPairs = 
				StreamEx.zip(
					parent1Segments.stream(), 
					parent2Segments.stream());
			Stream<Pair<List<Gene>, List<Gene>>> genesSwapped =
				StreamEx.indexed(breededPairs)
					.map(indexedGeneSeries ->  
						{
							if (indexedGeneSeries.first % 2 == 1) {
								List<Gene> tmp = indexedGeneSeries.second.first;
								indexedGeneSeries.second.first = indexedGeneSeries.second.second;
								indexedGeneSeries.second.second = tmp;
							}
							return indexedGeneSeries.second;
						});
			Pair<List<List<Gene>>, List<List<Gene>>> offspringSegments = 
				StreamEx.unzip(genesSwapped)
				.map((f, s) -> 
					Pair.create(f.collect(Collectors.toList()), s.collect(Collectors.toList()))
				);
			List<String> crossoverLog = 
				Stream.<Pair<List<List<Gene>>, String>>builder()
					.add(Pair.create(parent2Segments, "parent 2"))
					.add(Pair.create(parent1Segments, "parent 1"))					
					.add(Pair.create(offspringSegments.second, "child 2"))
					.add(Pair.create(offspringSegments.first, "child 1"))
					.build()
					.map(segments ->
						segments
							.<Pair<String, String>>map((s, desc) -> 
								Pair.create(desc,
									s.stream()
										.map(segment ->
											segment.stream().map(g -> g.toString())
												.collect(Collectors.joining()))
										.collect(Collectors.joining("| |"))
										)))
					.map(p -> "[" + p.second + "] " + p.first)
					.collect(Collectors.toList());				
			Genotype offspring1 = 
				parent1.construct(offspringSegments.first.stream());
			crossoverLog.stream().forEach(offspring1::addTempTag);
			Genotype offspring2 = 
				parent2.construct(offspringSegments.second.stream());
			crossoverLog.stream().forEach(offspring2::addTempTag);			
			return Pair.create(offspring1, offspring2);
		} else {
			String reasonString = "[" + parent1.toString() + "][" + parent2.toString() + "] Crossover skipped";
			return Pair.create(parent1.addTempTag(reasonString), parent2.addTempTag(reasonString));
		}
	}	
	
	public static Pair<Genotype, Genotype> Default2(@Param(name = "points") int points, @Param(name = "rate") double rate, Genotype parent1, Genotype parent2) {	
		if (Objects.equals(parent1, parent2) || parent1.length() == 0 || parent2.length() == 0) {
			return Pair.create(parent1.addTempTag("[" + parent1.toString() + "][" + parent2.toString() + "] Crossover skipped"), 
				parent2.addTempTag("[" + parent1.toString() + "][" + parent2.toString() + "] Crossover skipped"));
		}
		Random rand = new Random();
		double chanceToBreed = rand.nextDouble();
		if (chanceToBreed < rate) {			
			List<Integer> splitPoints1 = 
				StreamEx.shuffle(IntStream.range(0, parent1.length()).boxed().collect(Collectors.toList()))
					.limit(points)					
					.collect(Collectors.toList());
			List<Integer> splitPoints2 = 
				StreamEx.shuffle(IntStream.range(0, parent2.length()).boxed().collect(Collectors.toList()))
					.limit(points)					
					.collect(Collectors.toList());			
			List<List<Gene>> parent1Segments = 
				splitGenotype(parent1, splitPoints1.stream())
					//.peek(p -> Logger.info("SP: " + p.toString()))
					.collect(Collectors.toList());
			List<List<Gene>> parent2Segments = 
				splitGenotype(parent2, splitPoints2.stream())
					//.peek(p -> Logger.info("SP: " + p.toString()))
					.collect(Collectors.toList());			
			Stream<Pair<List<Gene>, List<Gene>>> breededPairs = 
				StreamEx.zip(
					parent1Segments.stream(), 
					parent2Segments.stream());
			Stream<Pair<List<Gene>, List<Gene>>> genesSwapped =
				StreamEx.indexed(breededPairs)
					.map(indexedGeneSeries ->  
						{
							if (indexedGeneSeries.first % 2 == 1) {
								List<Gene> tmp = indexedGeneSeries.second.first;
								indexedGeneSeries.second.first = indexedGeneSeries.second.second;
								indexedGeneSeries.second.second = tmp;
							}
							return indexedGeneSeries.second;
						});
			Pair<List<List<Gene>>, List<List<Gene>>> offspringSegments = 
				StreamEx.unzip(genesSwapped)
				.map((f, s) -> 
					Pair.create(f.collect(Collectors.toList()), s.collect(Collectors.toList()))
				);
			List<String> crossoverLog = 
				Stream.<Pair<List<List<Gene>>, String>>builder()
					.add(Pair.create(parent2Segments, "parent 2"))
					.add(Pair.create(parent1Segments, "parent 1"))					
					.add(Pair.create(offspringSegments.second, "child 2"))
					.add(Pair.create(offspringSegments.first, "child 1"))
					.build()
					.map(segments ->
						segments
							.<Pair<String, String>>map((s, desc) -> 
								Pair.create(desc,
									s.stream()
										.map(segment ->
											segment.stream().map(g -> g.toString())
												.collect(Collectors.joining()))
										.collect(Collectors.joining("| |"))
										)))
					.map(p -> "[" + p.second + "] " + p.first)
					.collect(Collectors.toList());				
			Genotype offspring1 = 
				parent1.construct(offspringSegments.first.stream());
			crossoverLog.stream().forEach(offspring1::addTempTag);
			Genotype offspring2 = 
				parent2.construct(offspringSegments.second.stream());
			crossoverLog.stream().forEach(offspring2::addTempTag);			
			return Pair.create(offspring1, offspring2);
		} else {
			String reasonString = "[" + parent1.toString() + "][" + parent2.toString() + "] Crossover skipped";
			return Pair.create(parent1.addTempTag(reasonString), parent2.addTempTag(reasonString));
		}
	}	
	
}
