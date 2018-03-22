package weka.agent;

import java.io.File;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

public class WekaAgentCli extends Thread {

  /*  
    public static final String OUTPUT = "-p 0";
    
    //public static final String CLASSIFIER = "weka.classifiers.misc.InputMappedClassifier -I -trim -W weka.classifiers.functions.NeuralNetwork -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.2 -dh 0.5 -iw 0 ";
    public static final String CLASSIFIER = "weka.classifiers.misc.InputMappedClassifier -I -trim -W weka.classifiers.functions.NeuralNetwork ";
    public static final String WEKACOMMAND = "java {classifier} -t {train} -T {test} {output}";
    public static final String LOOKUP_PATH = "c:/var/tmp/output/";
    public static final int POOLING_INTERVAL = 5000;
    public static final String ALGO="CNN";
    public static final String OUT_SUFFIX="_TESTED_SMALL";
    public static final String LAST_SYMBOL="XOM";
    */
    public static final ArffFilter arrFilter = new ArffFilter();

    public CommandListener listener ;

    public void setListener(CommandListener listener) {
        this.listener=listener;
    }

    private WekaAgentCliProperties properties=WekaAgentCliProperties.getInstance();
    
    public void run() {

        
        
        //Wait for 10 secs
        try {
            
            Thread.currentThread().join(properties.getPoolingInterval());

            while (!interrupted()) {
                File file = new File(properties.getLookupPath());

                String[] files = file.list(arrFilter);
                System.out.print(".");

                if (files != null && files.length > 0) {

                    File next = new File(properties.getLookupPath() + files[0]);

                    String command = getCommand(next);

                    command += " > " + properties.getLookupPath() + next.getName() +".output";
                    
                    //This call will block until the execution finishes
                    if ( listener.commandEvent(command,next)) {
                        renameFiles(next);
                        if (next.getName().startsWith(properties.getLastSymbol()))
                        {
                            //Create a end of processing file
                            PrintWriter eop= new PrintWriter(new File( properties.getLookupPath() +"EOP.TXT"));
                            eop.write(properties.getLastSymbol());
                            eop.close();
                            
                        }
                    }

                }

                try {
                    Thread.currentThread().join(properties.getPoolingInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e1) {
            JOptionPane.showMessageDialog(null, e1.getMessage());
            e1.printStackTrace();
        }

    }

    public File renameFiles(File next) {

        File newTrain = new File(next.getAbsolutePath().replace(ArffFilter.TRAIN, ArffFilter.TRAIN + "_"));
        
        newTrain.delete();
        
        next.renameTo(newTrain);

        //File test = new File(next.getAbsolutePath().replace(ArffFilter.TRAIN, ArffFilter.TEST));

        //test.renameTo(new File(test.getAbsolutePath().replace(ArffFilter.TEST, ArffFilter.TEST + "_")));

        return newTrain;

    }
    public String getCommand(File trainFile) {

        String command = properties.getWekaCommand().replace("{classifier}", properties.getClassifier());
        command = command.replace("{train}", trainFile.getAbsolutePath());
        command = command.replace("{output}",properties.getOutput());
        File testFile = new File(trainFile.getAbsolutePath().replace("TRAIN", "TEST"));
        command = command.replace("{test}", testFile.getAbsolutePath());
        return command;
    }
    
    
    public static  File getPredictionFile(File outFile)
    {
        
        
        final String SUFFIX_TO_STRIP="_TRAIN.arff.output";
        
        String suffix= "_" +WekaAgentCliProperties.getInstance().getAlgo()+WekaAgentCliProperties.getInstance().getOutputSuffix();
        
        String prefix = outFile.getAbsolutePath() .replace(SUFFIX_TO_STRIP, "");
        File predFile = new File (prefix + suffix+".CSV");
        
        return predFile;
        
    }
   
}
