package weka.agent;
import java.io.File;
import java.io.IOException;

public class WekaAgent {

	public static final String WEKAHOME = "c:/Program Files/Weka-3-7/";
	//public static final String CLASSIFIER = "weka.classifiers.trees.J48";
	public static final String OUTPUT="-p 0"; 
	public static final String CLASSIFIER = "weka.classifiers.functions.NeuralNetwork -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.2 -dh 0.5 -iw 0 ";
	public static final String WEKACOMMAND = "java -Xmx3000M -Djava.net.useSystemProxies=true -classpath \".;{jars}\" {classifier} -t {train} -T {test}";
	public static final String PATH = "c:/tmp/";
	public static final  ArffFilter arrFilter =new ArffFilter();
	public static final int POOLING_INTERVAL=1000;
	
//	public static final String jars  = "{wekahome}weka.jar;{wekahome}mtj-1.0-snapshot.jar;{wekahome}NeuralNetwork.jar;{wekahome}opencsv-2.3.jar";
	public static final String jars  = "{wekahome}.;{wekahome}weka.jar";
	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException,
			InterruptedException {

		while (true) {
			File file = new File(PATH );

			String[] files = file.list(arrFilter);
			System.out.print(".");
			

			if (files!=null && files.length > 0) {
				long timestamp = System.currentTimeMillis() / 1000;

				File next = new File(PATH + files[0]);
				

				executeClassifier(next);

				next.renameTo(new File(next.getAbsolutePath() + "." + timestamp));

			}

			Thread.currentThread().join(POOLING_INTERVAL);
		}

	}

	public static void executeClassifier(File next) throws IOException,
			InterruptedException {
		
		
		String command = getCommand(next);
		System.out.println("\nExecuting command " + command);
		
		Process process = Runtime.getRuntime().exec(command);
		OutputReader reader = new OutputReader(process);
		process.waitFor();
		Thread.currentThread().join(5000);
		System.out.println(reader.output);
		System.err.println(reader.err);
	//	reader.interrupt();
	}

	public static String getCommand(File trainFile) {
		
		
		String command = WEKACOMMAND.replace("{jars}",jars.replace("{wekahome}",WEKAHOME));
		
		command = command.replace("{classifier}", CLASSIFIER);
		command = command.replace("{train}", trainFile.getAbsolutePath());
		
		

		File testFile = new File(PATH + trainFile.getName().replace("TRAIN", "TEST"));
		if (!testFile.exists())
		{
			throw new IllegalStateException("File " + testFile.getAbsolutePath()+" does not exist.");
		}
		command = command.replace("{test}", testFile.getAbsolutePath());
		return command;
	}
}
