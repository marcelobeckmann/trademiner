/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package weka.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutputProcessor {
    
    public static final String START_PREDICTION="=== Predictions on test data ===";
    

    private static final int ID=0;
    private static final int LABEL=1;
    private static final int PREDICTION=2;
    private static final int PROBABILITY=3;
    
    private static final String COLUMN_SEPARATOR=";"; 
    
    public OutputProcessor()
    {}
    
    
    
    public void processPredictions(File output, File outFile) throws IOException
    {
        List<String[]> predictions= loadPredictions(output);
        //TODO PUT HEADER
        PrintWriter pw = null;
        long lastId=0;
        if (!outFile.exists())
        {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(outFile, false)));
            pw.write("id;label;prediction;probability\n");
        }
        else
        {
            lastId=this.retrieveLastId(outFile);
            pw = new PrintWriter(new BufferedWriter(new FileWriter(outFile, true)));
        }

        //TODO PUT A END PROCESSING CHECK
        for (String[] row:predictions)
        {
            
            row[ID]= String.valueOf(++lastId);
            
            String line  = Arrays.toString(row);
            line = line.replace("[", "");
            line = line.replace("]", "");
            line = line.replace(" ","");
            line =  line.replace(",",COLUMN_SEPARATOR);
            pw.write(line);
            pw.write('\n');
            
            pw.flush();
        }
        pw.close();
                
    }
    
    public long retrieveLastId(File file) throws IOException
    {
        BufferedReader r = new BufferedReader(new FileReader(file));
        
        String line;
        //Go to the end of file
        while ((line=r.readLine())!=null);
        String row[]= line.split(";");
        
        long lastId= -1;
        try {
            lastId= Long.parseLong(row[ID]);
            
        }
        catch (Exception e)
        {}
        return lastId;
        
        
    }
    
    
    /**
     * Gets the output, with the prediction and less characters
     * @return
     * @throws IOException 
     */
    private List<String[]> loadPredictions(File output) throws IOException
    {
        List<String[]> predictions = new ArrayList<String[]>();
        
        StringBuilder sb= new StringBuilder();
        boolean preditionStarted=false;
        BufferedReader r = new BufferedReader(new FileReader(output));
        String line;
        
        while ( (line = r.readLine())!=null)
        {
            if (!preditionStarted &&  line.contains(START_PREDICTION))
            {
                preditionStarted=true;
                continue;
            }
            else if (preditionStarted)
            {
                if (!line.contains(":"))
                {
                    continue;
                }
                line = line.replace("+", "");
                line= line.trim();
                
                for (int i=10;i>3;i--) {
                    line = line.replace(spaces(i)," ");
                }
                String [] array=line.split(" ");
                
                array[LABEL] = array[LABEL].replaceAll(".*:", "");
                array[PREDICTION]=array[PREDICTION].replaceAll(".*:", "");;
                predictions.add(array);
            }
        }
        
        
        return predictions;
    }

    private String spaces(int n)
    {
        StringBuilder sb= new StringBuilder();
        for (int i=0;i<n;i++)
        {
            sb.append(' ');
            
        }

        return sb.toString();   
    }
    
    public static void main(String args[]) throws IOException{
        

                
        File outFile = new File("C:/var/tmp/output/BA_1_20150903_k21_TRAIN.arff.output");
        File predFile =new WekaAgentCli().getPredictionFile(outFile);
        
        OutputProcessor proc= new OutputProcessor();
        
        proc.processPredictions(outFile,predFile);
        
    }
}
