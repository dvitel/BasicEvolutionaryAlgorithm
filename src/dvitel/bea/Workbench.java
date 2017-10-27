package dvitel.bea;

import java.util.stream.IntStream;

import dvitel.bea.utils.Config;

public class Workbench {
	
	public static Workbench create() {
		return new Workbench();
	}
	
	public Genotype runOnce(Config config) {
		Population result = 
			config.getAlgorithm().run(
					config.getEmptyPopulation(),
					config.getSelection(), //Selection.getFromConfig()
					config.getCrossover(),
					config.getMutation(),
					config.getReplacementSeleciton(),
					config.getLogger(),
					config.getDataLogger());
		return result.getBest();
	}
	
	public void runConfig(Config config) {
		Integer nTimes = config.getWorkbenchSetting("nTimes", int.class).orElse(1);
		Boolean printEveryTest = config.getWorkbenchSetting("printEveryTest", boolean.class).orElse(false);
		if (nTimes == null) nTimes = 1;
		IntStream.range(0, nTimes).forEach(i -> {
			Genotype best = runOnce(config);
			if (printEveryTest)
			{								
				config.getLogger().info("--" + i + "-----" + config.getName() + "\r\n" + best.toString() + "\r\nwith weight: " + best.getWeight(config.getFitness()) + "\r\n---------");
			}
		});
		config.getDataLogger().flushData();
	}
	
	public void runChildConfigs(Config config) {
		config.getChildConfigs()
			.stream()
			.forEach(this::runConfig);
	}	
	
}
