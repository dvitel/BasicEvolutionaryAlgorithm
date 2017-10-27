package dvitel.bea.utils;

import java.io.FileInputStream;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import dvitel.bea.Crossover;
import dvitel.bea.EvolutionAlgorithm;
import dvitel.bea.Genotype;
import dvitel.bea.Genotype.Fitness;
import dvitel.bea.ListGenotype;
import dvitel.bea.ListPopulation;
import dvitel.bea.Mutation;
import dvitel.bea.Population;
import dvitel.bea.Selection;



@SuppressWarnings("unchecked")
public class Config {
	/*
	 * config properties
	 */					
	private Hashtable<String, Hashtable<String, String>> bindings = 
			new Hashtable<String, Hashtable<String, String>>();	
	//private static Class<?> genotypeClass = ListGenotype.class;
	//workbench settings  
	//
	private String name = "";
	private Logger logger = new Logger();
	private Logger dataLogger = new Logger();
	private Fitness fitness = g -> ListGenotype.BitFitness.OneMax((ListGenotype)g);	
	private Supplier<List<Genotype>> genotypeSupplier = () -> ListGenotype.createRandomWithBitGene(100, 500);
	private Supplier<Population> population = () -> new ListPopulation(fitness, genotypeSupplier.get());
	private Selection selection = p -> Selection.Tournament(2, p);
	private Mutation mutation = g -> Mutation.PerGene(0.05, g);
	private Crossover crossover = (g1, g2) -> Crossover.Default(1, 0.8, g1, g2);
	private Selection replacementSelection = p -> Selection.Tournament(2, p);
	private Supplier<EvolutionAlgorithm> algorithm = () -> new EvolutionAlgorithm.SteadyState(5000, 2, false, 10);
	
	private Class<?> loadClass(String name) {
		Class<?> cl = null;
		try {
			cl = Class.forName(name);
		} catch (ClassNotFoundException e) {
			logger.err("[loadClass] Class name: " + name + " - class was not found");
			System.exit(1);
		}		
		return cl;
	}
	
	private Method loadMethod(String name) {
		String[] parts = name.split("::");
		if (parts.length < 2) 
		{
			logger.err("[loadMethod] Method name: " + name + " - is wrong. Method and class should be separated by ::");
			System.exit(1);
		}
		Class<?> cl = loadClass(parts[0]);
		Method found = Arrays.stream(cl.getMethods()).filter(m -> m.getName().equals(parts[1])).findFirst().orElse(null);
		if (found == null) {
			logger.err("[loadMethod] Method name: " + parts[1] + " - was not found in class: " + parts[0]);
			System.exit(1);
		}
		return found;
	}
	
	private Object call(Method m, List<Object> bdg) {
		try {
			return m.invoke(null, bdg.toArray(new Object[] {}));
		} catch (IllegalAccessException | IllegalArgumentException e) {
			logger.err("Method name: " + m.getName() + ", " + m.getClass().getCanonicalName() + " - invocation failed. " + e.getMessage());
			logger.exn(e);
			System.exit(1);
		} catch (InvocationTargetException e) {
			logger.err("Method name: " + m.getName() + ", " + m.getClass().getCanonicalName() + " - invocation failed. " + e.getCause().getMessage() + " " + e.getClass().getName());
			logger.exn(e);
			System.exit(1);
		}			
		return null;
	}	
	
	private Object call(Class<?> m, Object... bdg) {
		try {
			return
				Arrays.stream(m.getConstructors())
					.filter(c ->
						(c.getParameters().length == bdg.length)
						&&
						StreamEx.zip(Arrays.stream(c.getParameters()),
							Arrays.stream(bdg))
							.allMatch(p -> p.first.getType().isInstance(p.second)))
					.findFirst().orElseThrow(() -> new NoSuchMethodException())
					.newInstance(bdg);
		} catch (IllegalAccessException | IllegalArgumentException 
				| InstantiationException | NoSuchMethodException 
				| SecurityException e) {
			logger.err("Constructor name: " + m.getName() + ", " + m.getClass().getCanonicalName() + " - invocation failed. " + e.getMessage());
			logger.exn(e);
			System.exit(1);
		} catch (InvocationTargetException e) {
			logger.err("Constructor name: " + m.getName() + ", " + m.getClass().getCanonicalName() + " - invocation failed. " + e.getCause().getMessage() + " " + e.getClass().getName());
			logger.exn(e);
			System.exit(1);
		}	
		return null;
	}	
	
	private Fitness loadFitness(String name) {
		Method m = loadMethod(name);
		if (m == null) return null;
		ArrayList<Object> bindings = getBinding(m, "Fitness");
		return
			o -> {
				ArrayList<Object> localBindings = new ArrayList<>(bindings);
				localBindings.add(o);
				return (double)call(m, localBindings);
			};
	}
	
	private void addBinding(String routine, String key, String value) {
		Hashtable<String, String> settings = bindings.getOrDefault(routine, new Hashtable<String, String>());
		settings.put(key, value);
		bindings.put(routine, settings);
	}
	
	private static Hashtable<Class<?>, Function<String, ?>> parsers = 
		new Hashtable<Class<?>, Function<String, ?>>();
	
	private ArrayList<Object> getBinding(Executable m, String routine) {
		Hashtable<String, String> binds = bindings.getOrDefault(routine, new Hashtable<String, String>());
		ArrayList<Object> params = new ArrayList<>();
		for (Parameter p: m.getParameters()) {
			Param prm = p.getAnnotation(Param.class);
			if (prm == null) continue;
			String pObj = binds.get(prm.name());
			if (pObj == null) continue;
			Function<String, ?> parser = parsers.get(p.getType());
			if (parser != null) {
				params.add(parser.apply(pObj.trim()));
			} else {
				logger.err("[" + routine + "] Binding error for parameter: " + p.getName());
				System.exit(1);
			}
		}
		return params;
	}
	
	private ArrayList<Pair<String, Consumer<String>>> configFields = 
		new ArrayList<Pair<String, Consumer<String>>>();
	
	static {
		parsers.put(double.class, Double::valueOf);
		parsers.put(float.class, Float::valueOf);
		parsers.put(int.class, Integer::valueOf);
		parsers.put(boolean.class, Boolean::valueOf);
		parsers.put(String.class, str -> str);		
	}
	public Config(String configPath)
	{				
		name = configPath;
		configFields.add(Pair.create("log", 
				rawValue -> {
					logger = new Logger("LOG: " + rawValue, true, rawValue);
				}));		
			configFields.add(Pair.create("data", 
				rawValue -> {
					dataLogger = new Logger("DATA: " + rawValue, false, rawValue);
				}));		
		configFields.add(Pair.create("Fitness", rawValue -> fitness = loadFitness(rawValue)));			
		configFields.add(Pair.create("GenotypeGen", 
			rawValue -> {
				Method m = loadMethod(rawValue);
				List<Object> binding = getBinding(m, "GenotypeGen");
				genotypeSupplier = 
					() -> (List<Genotype>)call(m, binding);
			}));
		configFields.add(Pair.create("Population", rawValue -> 
			{
				Class<?> m = loadClass(rawValue);
				population = 
					() -> (Population)call(m, fitness, genotypeSupplier.get()); 
			}));		
		configFields.add(Pair.create("Selection", 
			rawValue -> {
				Method m = loadMethod(rawValue);
				ArrayList<Object> binding = getBinding(m, "Selection");				
				selection = 
					p -> {
						ArrayList<Object> localBinding = new ArrayList<>(binding);
						localBinding.add(p);
						return (Genotype)call(m, localBinding);
					};
			}));
		configFields.add(Pair.create("Mutation", 
			rawValue -> {
				Method m = loadMethod(rawValue);
				ArrayList<Object> binding = getBinding(m, "Mutation");
				mutation = g -> {
					ArrayList<Object> localBinding = new ArrayList<>(binding);
					localBinding.add(g);					
					return (Genotype)call(m, localBinding);
				};
			}));		
		configFields.add(Pair.create("Crossover", 
			rawValue -> {
				Method m = loadMethod(rawValue);
				ArrayList<Object> binding = getBinding(m, "Crossover");
				crossover = (g1, g2) -> {
					ArrayList<Object> localBinding = new ArrayList<>(binding);
					localBinding.add(g1);
					localBinding.add(g2);
					return (Pair<Genotype, Genotype>)call(m, localBinding);
				};				
			}));
		configFields.add(Pair.create("ReplacementSelection", 
			rawValue -> {
				Method m = loadMethod(rawValue);
				ArrayList<Object> binding = getBinding(m, "ReplacementSelection");				
				replacementSelection = 
					p -> {
						ArrayList<Object> localBinding = new ArrayList<>(binding);
						localBinding.add(p);
						return (Genotype)call(m, localBinding);
					};
			}));	
		configFields.add(Pair.create("Alg", 
			rawValue -> {
				Method m = loadMethod(rawValue);
				ArrayList<Object> bdg = getBinding(m, "Alg");
				algorithm = 
					() -> 
						{
							return (EvolutionAlgorithm)call(m, bdg);
						};
			}));				
		
		Properties props = new Properties();
		Hashtable<String, String> delayedInit = new Hashtable<String, String>();
		try (FileInputStream stream = new FileInputStream(configPath)){
			props.load(stream);
			for (Object propKeyObj: props.keySet()) {
				String propKey = (String)propKeyObj;
				String rawValue = props.getProperty(propKey);
				if (null != rawValue && !("".equals(rawValue)))
					try {	
						String processed = rawValue;
						//Pattern p = Pattern.compile("\\{\\{(.*)\\}\\}");
						//Matcher m = p.matcher(processed);
//						if (m.matches()) {
//							while (m.find()) {
//								String key = m.group(1);
//								processed = processed.replace(m.group(), props.getProperty(key));
//							}
//						}
						String[] propKeyParts = propKey.split(Pattern.quote("."));
						if (propKeyParts.length > 1) {
							addBinding(propKeyParts[0], propKeyParts[1], processed);
						} else {
							delayedInit.put(propKey, processed);
						}
					} 
					catch (NumberFormatException e) {
						logger.err("Could not parse " + propKey + ", value: " + rawValue);	
						System.exit(1);
					}
			}	
			configFields.stream().filter(pair -> delayedInit.containsKey(pair.first))
				.forEach(pair -> pair.second.accept(delayedInit.get(pair.first)));			
		} catch (Exception e) {
			logger.err("Initialization exception: " + e.getMessage());
			System.exit(1);
		}
	}
	
	public Selection getSelection() {
		return selection;
	}
	public Mutation getMutation() {
		return mutation;
	}
	public Crossover getCrossover() {
		return crossover;
	}
	public Selection getReplacementSeleciton() {
		return replacementSelection;
	}
	public Fitness getFitness() {
		return fitness;
	}		
	public Population getEmptyPopulation() {
		return population.get();
	}		
	public EvolutionAlgorithm getAlgorithm() {
		return algorithm.get();
	}
	public List<Genotype> generateGenotypes() {
		return genotypeSupplier.get();
	}
	public Logger getLogger() {
		return logger;
	}
	public Logger getDataLogger() {
		return dataLogger;
	}	
	public static Config getDefault() {
		return new Config("bea.cfg");
	}
	public static Config get(String path) {
		return new Config(path);
	}	
	public Optional<String> getSetting(String category, String key) {
		Hashtable<String, String> s = bindings.get(category);
		if (s == null) return Optional.empty();
		String v = s.get(key);
		if (v == null) return Optional.empty();
		return Optional.of(v);
	}
	public <T> Optional<T> getSetting(String category, String key, Class<T> c) {
		return getSetting(category, key).flatMap(o -> 
			{
				Object res = parsers.get(c).apply(o);
				if (res == null) return Optional.empty();
				else return Optional.of((T)res);
			});
	}
	public <T> Optional<T> getWorkbenchSetting(String key, Class<T> c) {
		return getSetting("Workbench", key, c);
	}	
	public List<Config> getChildConfigs() {
		Hashtable<String, String> configs = bindings.get("Config");
		if (configs == null) return Collections.emptyList();
		return 
			configs.keySet().stream()
				.map(v -> { try { return Pair.create(v, Integer.valueOf(v)); } catch (Exception e) { return Pair.create(v, -1); }})
				.filter(p -> p.second >= 0)
				.map(p -> new Config(configs.get(p.first)))
				.collect(Collectors.toList());
	}	
	public String getName() {
		return name;
	}
}
