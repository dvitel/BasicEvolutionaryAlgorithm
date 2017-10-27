package dvitel.bea;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dvitel.bea.Gene.BitGene;
import dvitel.bea.utils.Param;
import dvitel.bea.utils.StreamEx;

public class BigIntGenotype extends Genotype {
	private BigInteger genes; //bits
	private int length;	
	private static class ZeroRandom extends Random {
		private static final long serialVersionUID = 1L;
		@Override
		public int next(int bits) { return 0; }
	}
	@Override 
	public String toString() {
		int bitLength = genes.bitLength();
		int leadingZeroes = length - bitLength;
		String prefix = String.join("", Collections.nCopies(leadingZeroes, "0"));
		return prefix + genes.toString(2);
	}
	@Override 
	public boolean equals(Object other) {
		if (other == null || !(other instanceof BigIntGenotype)) return false;
		//TODO - think of equality of two different implementations of genotypes
		//TODO - impossible to compare implementations - no common sence of equality between genes 
		return this.genes.equals(((BigIntGenotype)other).genes);
	}
	@Override 
	public int hashCode() {
		return genes.hashCode();
	}
	private BigIntGenotype(int length) {
		this(length, new ZeroRandom(), new Hashtable<>());
	}
	private BigIntGenotype(int length, Random rnd) {
		this(length, rnd, new Hashtable<>());
	}	
	private BigIntGenotype(int length, Random rnd, Hashtable<Object, Object> tags) {
		this(new BigInteger(length, rnd), length, tags);
	}
	private BigIntGenotype(BigInteger genes, int length) {
		this(genes, length, new Hashtable<>());
	}
	private BigIntGenotype(BigInteger genes, int length, Hashtable<Object, Object> tags) {
		super(tags);
		this.genes = genes;
		this.length = length;
	}
	@Override
	public Genotype construct(Stream<List<Gene>> segments) {
		List<BitGene> genes = 
			segments.flatMap(segment -> segment.stream())
				.filter(BitGene.class::isInstance)
				.map(BitGene.class::cast).collect(Collectors.toList());
		BigInteger newValue = new BigInteger(genes.size(), new ZeroRandom());	
		class Holder { BigInteger value; }
		BigInteger result = 
			StreamEx.indexed(genes.stream())
				.collect(
					() -> new Holder(){{ value = newValue; }},
					(a, g) -> {a.value = g.second.isOne() ? a.value.setBit(g.first) : a.value.clearBit(g.first); },
					(a1, a2) -> {
						for (int i = 0; i < genes.size(); i++) {
							a1.value = (a1.value.testBit(i) || a2.value.testBit(i)) ? a1.value.setBit(i) : a1.value;
						}
					})
				.value;
		return new BigIntGenotype(result, genes.size(), tags);
	}
	@Override
	public BigIntGenotype mutateGene(int index) {
		if (index < 0 || index >= length) return this;
		return new BigIntGenotype(genes.flipBit(index), length, tags);
	}
	@Override
	public BitGene get(int index) {
		if (index >= length()) return null;
		return genes.testBit(index) ? BitGene.ONE : BitGene.ZERO;
	}

	@Override
	public int length() {
		return this.length;
	}
	public static BigIntGenotype fromString(String str) {
		return new BigIntGenotype(new BigInteger(str, 2), str.length());
	}
	public static List<Genotype> createRandom(@Param(name = "count") int count, @Param(name = "vectorSize") int vectorSize) {
		ArrayList<Genotype> l = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			l.add(new BigIntGenotype(vectorSize, new Random()));
		}
		return l;
	}
	public static List<Genotype> createRandomWithVarSize(@Param(name = "count") int count, @Param(name = "minVectorSize") int minVectorSize, @Param(name = "maxVectorSize") int maxVectorSize) {
		ArrayList<Genotype> l = new ArrayList<>();
		Random rnd = new Random();
		for (int i = 0; i < count; i++) {
			l.add(new BigIntGenotype(rnd.nextInt(maxVectorSize - minVectorSize + 1) + minVectorSize, new Random()));
		}
		return l;
	}		
	public static final class Fitness {
		/*fast OneMax*/
		public static double OneMax(BigIntGenotype genotype) { 
			return ((double)genotype.genes.bitCount()) / genotype.length();
		}	
		
		public static double Sum(BigIntGenotype genotype) { 
			return ((double)genotype.genes.bitCount());
		}			
	}

	@Override
	public List<Gene> get(int from, int toExclusive) {
		return IntStream.range(from, toExclusive).filter(i -> i < length()).mapToObj(i -> get(i)).collect(Collectors.toList());
	}
	@Override
	public Genotype clone() {
		return new BigIntGenotype(genes, length, tags);
	}
	@Override
	public Gene createRandomGene() {
		return (new Random()).nextBoolean() ? BitGene.ONE : BitGene.ZERO;
	}
}	

