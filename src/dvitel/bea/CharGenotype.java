package dvitel.bea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dvitel.bea.Gene.StringGene;
import dvitel.bea.utils.Param;

public class CharGenotype extends Genotype {
	private String genes;
	private String[] domain;
	
	public CharGenotype(String init, String[] domain, Hashtable<Object, Object> tags) {
		super(tags);
		this.domain = domain;
		this.genes = init;
	}
	public CharGenotype(String init, String[] domain) {
		this(init, domain, new Hashtable<>());
	}
	@Override
	public Genotype mutateGene(int index) {
		if (index >= length()) return this;
		Random r = new Random();
		int mutationIndex = r.nextInt(domain.length);
		String resultChar = domain[mutationIndex];
		StringBuilder b = new StringBuilder();
		b.append(genes.subSequence(0, index));
		b.append(resultChar);
		b.append(genes.subSequence(index+1, genes.length()));
		return new CharGenotype(b.toString(), domain, tags);
	}

	@Override
	public int length() {
		return genes.length();
	}

	@Override
	public Gene get(int index) {
		return new StringGene(genes.substring(index, index+1), domain);
	}
	
	@Override
	public String toString() {
		return genes;
	}
	
	@Override
	public List<Gene> get(int from, int toExclusive) {
		from = Math.max(Math.min(from, length()), 0);
		toExclusive = Math.min(toExclusive, length());
		if (from == toExclusive) return Collections.emptyList();
		return 
			genes.chars().mapToObj(ch -> new String(Character.toChars(ch)))
				.skip(from)
				.limit(toExclusive - from)
				.map(str -> new StringGene(str, domain))
				.collect(Collectors.toList());
	}

	@Override
	public Genotype construct(Stream<List<Gene>> segments) {
		String result = 
			segments.flatMap(segment -> segment.stream())
				.map(g -> g.toString()).collect(Collectors.joining());
		return new CharGenotype(result, domain, tags);			
	}
	
	@Override 
	public boolean equals(Object other) {
		if (other == null || !(other instanceof CharGenotype)) return false;
		String otherGenes = ((CharGenotype)other).genes;
		return Objects.equals(otherGenes, genes);
	}
	@Override 
	public int hashCode() {
		return genes.hashCode();
	}		
	
	public static final class Fitness {
		public static double SimilarTo(@Param(name="targetString") String toCompareWith, Genotype g) {
			String genes = g.toString();
			int minLength = Math.min(toCompareWith.length(), genes.length());
			double diff = 0;
			for (int i = 0; i < minLength; i++) {
				diff = diff - Math.abs((int)genes.charAt(i)- (int)toCompareWith.charAt(i));
			}
			for (int i = minLength; i < toCompareWith.length(); i++) {
				diff = diff - (int)toCompareWith.charAt(i);
			}
			for (int i = minLength; i < genes.length(); i++) {
				diff = diff - (int)genes.charAt(i);
			}				
			return diff;
		}
	}
	
	public static List<Genotype> createRandom(@Param(name = "count") int count, @Param(name = "minVectorSize") int minVectorSize, @Param(name = "maxVectorSize") int maxVectorSize, @Param(name = "domain") String domain) {
		ArrayList<Genotype> genes = new ArrayList<>();
		String[] domainLst = domain.split("");
		Random rnd = new Random();
		for (int i = 0; i < count; i++) {
			int selectedSize = rnd.nextInt(maxVectorSize - minVectorSize + 1) + minVectorSize;
			List<String> lst = new ArrayList<String>();
			for (int j = 0; j < selectedSize; j++) {
				lst.add(domainLst[rnd.nextInt(domainLst.length)]);
			}
			Genotype g = new CharGenotype(String.join("", lst), domainLst);
			genes.add(g);
		}
		return genes;
	}
	@Override
	public Genotype clone() {
		return new CharGenotype(genes, domain, tags);
	}	
	@Override
	public Gene createRandomGene() {
		return new StringGene(domain[(new Random()).nextInt(domain.length)], domain);
	}	
}
