package com.rapidminer.operator.preprocessing.processcontrol;

import java.io.File;
import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.PortPairExtender;
//import com.rapidminer.operator.text.WordList;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

public class WaitForFile extends Operator {

        public static final String PARAMETER_FILE = "file";

        private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());

        
        public WaitForFile(OperatorDescription description) {
                super(description);
                
                dummyPorts.start();
                
                getTransformer().addRule(dummyPorts.makePassThroughRule());
                
                
        }

        public void doWork() throws OperatorException {

            String expected = getParameterAsString(PARAMETER_FILE);
            
            File expectedFile = new File(expected);
            System.out.println("Waiting for expected file :" +expectedFile.getAbsolutePath());
            while (!expectedFile.exists())
            {
                try {
                    Thread.currentThread().join(1000);
                } catch (InterruptedException e) {
             
                    new OperatorException(e.getMessage(),e);
                }
            }
            
            dummyPorts.passDataThrough();
                
        }

        @Override
        public List<ParameterType> getParameterTypes() {
                List<ParameterType> types = super.getParameterTypes();
                types.add(new ParameterTypeFile(PARAMETER_FILE, "Expected file", "fil", false));

                return types;
        }

}
