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
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.math.Averagable;

/**
 * Replaces an specified measure by G-Mean (Not existing in RM)
 * @author beckmann
 *
 */
public class GMeanPerformance extends Operator  {

	private static final String REPLACE_MEASURE="replace_measure";
	
    private InputPort performanceInput = getInputPorts().createPort("performance");
    private OutputPort performanceOutput = getOutputPorts().createPort("performance");

    
    public GMeanPerformance(OperatorDescription description) {
        super(description);
    }

    @Override
    public void doWork() throws OperatorException {
        PerformanceVector inputPerformance = performanceInput.getDataOrNull(PerformanceVector.class);
            
      List<String> names = Arrays.asList(BinaryClassificationPerformance.NAMES);
      String measureToReplace = this.getParameterAsString(REPLACE_MEASURE);
      	
       for (int i=0;i<inputPerformance.size();i++) {
    	    BinaryClassificationPerformance avg =(BinaryClassificationPerformance) inputPerformance.getAveragable(i);
    		//System.out.println("####### "+ avg.getName());
    		int type = names.indexOf(avg.getName());
		
    		
    		if ( Double.isNaN(avg.getFitness()))
			{
    			//Let's put some poor performance, this 4 is assuming f_measure
    			
    			Averagable newAvg= null;
    			
    			newAvg= new BinaryClassificationPerformance(type,new double [][] {new double[] {1,99},new double[]{99,1}  });
    	    				
    			
				inputPerformance.removeAveragable(avg);
				inputPerformance.addAveragable(newAvg);
				
			}
    		//f-measure
    		else if (avg.getName().equals(measureToReplace))
			{
    			
    			//Let's put some poor performance, this 4 is assuming f_measure
    			Averagable newAvg= null;
    			
    			newAvg= new BinaryClassificationPerformanceFixed(type,avg.getCounter());
    	    	
    			
    			
				inputPerformance.removeAveragable(avg);
				inputPerformance.addAveragable(newAvg);
				
			}
			
			
		} 
       
        
        performanceOutput.deliver(inputPerformance);

        
    }

    
    public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type;
		
		type = new ParameterTypeString(REPLACE_MEASURE,
				"The name of classification measure which its value will be replaced by G-Mean", "f_measure", false);
		types.add(type);
		
		return types;
    }

    
}

