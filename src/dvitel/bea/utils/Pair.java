package dvitel.bea.utils;

import java.util.Objects;
import java.util.function.BiFunction;

/*
 * Java does not have tuple as native type
 */
public class Pair<K, V> { 
	public K first;
	public V second;
	public static <K1, V1> Pair<K1, V1> create(K1 first, V1 second) {
		Pair<K1, V1> p = new Pair<K1, V1>();
		p.first = first;
		p.second = second;
		return p;		
	}
	public <T> T map(BiFunction<K,V,T> f) { return f.apply(first, second); }
	@Override 
	public String toString() {
		return "(" + Objects.toString(first) + ", " + Objects.toString(second) + ")";
	}
}
