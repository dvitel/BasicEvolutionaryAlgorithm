package dvitel.bea;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dvitel.bea.Gene.BitGene;
import dvitel.bea.utils.Param;

public class ListGenotype extends Genotype {
	private ArrayList<Gene> genes;
	@Override 
	public boolean equals(Object other) {
		if (other == null || !(other instanceof ListGenotype)) return false;
		ArrayList<Gene> otherGenes = ((ListGenotype)other).genes;
		if (otherGenes.size() != genes.size()) return false;
		for (int i = 0; i < genes.size(); i++) {
			if (!Objects.equals(genes.get(i), otherGenes.get(i))) return false;
		}
		return true;
	}
	@Override 
	public int hashCode() {
		return Objects.hashCode(genes.toArray(new Gene[]{}));
	}
	
	public ListGenotype(List<Gene> genes) {
		this(genes, new Hashtable<>());
	}
	public ListGenotype(List<Gene> genes, Hashtable<Object, Object> tags) {
		super(tags);
		this.genes = new ArrayList<>(genes); 
	}
	@Override
	public Genotype mutateGene(int index) {
		ArrayList<Gene> newGenes = new ArrayList<>(genes);
		newGenes.set(index, genes.get(index).mutate());
		return new ListGenotype(newGenes, tags); //statefull
	}

	@Override
	public int length() {
		return genes.size();
	}

	@Override
	public Gene get(int index) {
		return genes.get(index);
	}

	@Override
	public Genotype construct(Stream<List<Gene>> segments) {
		List<Gene> genes = 
			segments.flatMap(segment -> segment.stream()).collect(Collectors.toList());
		return new ListGenotype(genes, tags);
	}		
	
	@Override 
	public String toString() {
		List<String> s = genes.stream().map(g -> g.toString()).collect(Collectors.toList());
		return String.join("", s);
	}
	@Override
	public List<Gene> get(int from, int toExclusive) {
		return genes.subList(Math.min(length(), from), Math.min(length(), toExclusive));
	}	
	
	public static final class BitFitness {
		/*fast OneMax*/
		public static double OneMax(ListGenotype genotype) { 
			double count = 
				genotype.genes.stream().filter(g -> g instanceof BitGene)
					.mapToDouble(g -> ((BitGene)g).isOne() ? 1.0 : 0.0).sum();
			return count / genotype.length();
		}		
	}	
	
	public static List<Genotype> createRandomWithBitGene(@Param(name = "count") int count, @Param(name = "vectorSize") int vectorSize) {
		ArrayList<Genotype> l = new ArrayList<>();
		Random rnd = new Random();
		for (int i = 0; i < count; i++) {
			ArrayList<Gene> g = new ArrayList<Gene>();
			for (int j = 0; j < vectorSize; j++) {
				g.add(rnd.nextBoolean() ? BitGene.ONE : BitGene.ZERO);
			}
			l.add(new ListGenotype(g));
		}
		return l;
	}
	@Override
	public Genotype clone() {
		return new ListGenotype(genes, tags);
	}
	@Override
	public Gene createRandomGene() {
		if (genes.size() > 0) return genes.get(0).mutate();
		else return null; 
	}	
}	