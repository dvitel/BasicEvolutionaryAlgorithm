package dvitel.bea.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

/*Load handlers of logger from config file */
public final class Logger {
	private java.util.logging.Logger logger;
	public Logger() {		
	}
	public Logger(String name, boolean withConsole, String logPath)
	{
		logger = java.util.logging.Logger.getLogger(name);
		logger.setUseParentHandlers(false);
		SimpleFormatter formatter = new SimpleFormatter() {
			@Override 
			public String format(LogRecord record) {
				return record.getMessage() + "\r\n";
			}
		};
		Arrays.stream(logger.getHandlers()).forEach(logger::removeHandler);
		if (withConsole)
		{
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(formatter);
			logger.addHandler(consoleHandler);
		}
		try {
			FileHandler fileHandler = new FileHandler(logPath);
			fileHandler.setFormatter(formatter);
			logger.addHandler(fileHandler);
		} catch (IOException e) {
			//silently ignore
			logger.severe("Could not log to " + logPath);
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.exit(1);
		}
		logger.setLevel(Level.ALL);
	}
	//public static Logger getInstance() {
	//	if (instance == null) instance = new Logger();
	//	return instance;
	//}
	public void err(String msg) {
		if (logger != null)
		logger.severe(msg);
	}
	public void warn(String msg) {
		if (logger != null)
		logger.warning(msg);
	}
	public void info(String msg) {
		if (logger != null)
		logger.info(msg);
	}	
	public void exn(Throwable e) {
		if (logger != null)
		logger.log(Level.SEVERE, e.getMessage(), e);
	}
	private TreeMap<Integer, List<Double>> experimentResults = new TreeMap<Integer, List<Double>>();
	public void data(int generation, double value) {
		List<Double> lst = experimentResults.getOrDefault(generation, new ArrayList<>());
		lst.add(value);
		experimentResults.put(generation, lst);
	}	
	public void flushData() {
		logger.info(
			experimentResults.keySet().stream()
				.map(i -> i + "," + experimentResults.get(i).stream().map(Objects::toString).collect(Collectors.joining(",")))
				.collect(Collectors.joining("\r\n")));
	}
}
