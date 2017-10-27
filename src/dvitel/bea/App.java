package dvitel.bea;

import dvitel.bea.utils.Config;

public class App {
	public static void main2(String[] args) {
		Config config = Config.getDefault();
		Workbench w = Workbench.create();
		w.runChildConfigs(config);
	}	
	
	public static void main(String[] args) throws ClassNotFoundException {
		Config config = Config.getDefault();
		Workbench w = Workbench.create();
		w.runConfig(config);
		
		//Class<?> cl = int.class;
		//System.out.println(cl.getCanonicalName());
		//System.out.println(dvitel.bea.BigIntGenotype.Fitness.class.getName());
//		CharGenotype g = new CharGenotype("1234567890", "1234567890".split(""));
//		
//		Stream<Pair<Integer, Integer>> s = 
//			StreamEx.window2(IntStream.range(0, 20).boxed());
//		
//		s.forEach(pair -> System.out.println(pair + " " + g.construct(Stream.of(g.get(pair.first, pair.second)))));
//		System.out.println("----------------");
		
		
		//System.out.println("test".substring(5,4));
//		BigInteger v = BigInteger.valueOf(2);
//		System.out.println(v.toString(2));
//		BigIntGenotype g = BigIntGenotype.fromString("1000011110");
//		List<Gene> gns = g.get(0, 4);
//		for (Gene gn: gns) {
//			System.out.println(gn.toString());
//		}
		//System.out.println(g.toString());
	}
}
