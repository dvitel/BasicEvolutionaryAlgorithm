package dvitel.bea;

import java.util.Random;

public interface Gene {
	default Gene mutate() { return this; }		
	
	public enum BitGene implements Gene {
		ZERO, ONE;
		public boolean isZero() { return ZERO == this; }
		public boolean isOne() { return ONE == this; };
		@Override
		public BitGene mutate() { //just flip
			return isOne() ? ZERO : ONE;
		}
		@Override 
		public String toString() {
			return isOne() ? "1" : "0";
		}
	}		
	
	public class StringGene implements Gene {
		private String s;
		private String[] domain;
		public StringGene(String s, String[] domain) { this.s = s; this.domain = domain; }
		@Override
		public String toString() { return s; } 
		@Override 
		public StringGene mutate() {
			Random r = new Random();
			int mutationIndex = r.nextInt(domain.length);
			return new StringGene(domain[mutationIndex], domain);		
		}
	}
}
	
