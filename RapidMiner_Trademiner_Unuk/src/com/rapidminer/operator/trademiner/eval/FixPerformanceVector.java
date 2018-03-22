package com.rapidminer.operator.trademiner.eval;

import java.util.Arrays;
import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.BinaryClassificationPerformance;
import com.rapidminer.operator.performance.BinaryClassificationPerformanceFixed;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.math.Averagable;


public class FixPerformanceVector extends Operator  {

    private InputPort performanceInput = getInputPorts().createPort("performance");
    private OutputPort performanceOutput = getOutputPorts().createPort("performance");

    
    public FixPerformanceVector(OperatorDescription description) {
        super(description);
    }

    @Override
    public void doWork() throws OperatorException {
        PerformanceVector inputPerformance = performanceInput.getDataOrNull(PerformanceVector.class);
            
      List<String> names = Arrays.asList(BinaryClassificationPerformance.NAMES);
         
        
       for (int i=0;i<inputPerformance.size();i++) {
    	   BinaryClassificationPerformance avg =(BinaryClassificationPerformance) inputPerformance.getAveragable(i);
    	   
    		if ( Double.isNaN(avg.getFitness()))
			{
    			int type = names.indexOf(avg.getName());
    			//Let's put some poor performance, this 4 is assuming f_measure
    			
    			Averagable newAvg= null;
    			if (avg instanceof BinaryClassificationPerformanceFixed) {
    				newAvg= new BinaryClassificationPerformanceFixed(type,new double [][] {new double[] {1,99},new double[]{99,1}  });
    			}
    			else
    			{
    				newAvg= new BinaryClassificationPerformance(type,new double [][] {new double[] {1,99},new double[]{99,1}  });
    	    				
    			}
				inputPerformance.removeAveragable(avg);
				inputPerformance.addAveragable(newAvg);
				
			}
			
			
		} 
       
        
        performanceOutput.deliver(inputPerformance);

        
    }


    
}

