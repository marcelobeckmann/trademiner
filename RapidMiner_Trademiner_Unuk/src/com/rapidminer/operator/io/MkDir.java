package com.rapidminer.operator.io;

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

public class MkDir extends Operator {

        public static final String PARAMETER_DIR = "directory";

        private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());

        
        public MkDir(OperatorDescription description) {
                super(description);
                dummyPorts.start();
                getTransformer().addRule(dummyPorts.makePassThroughRule());
                
        }

        public void doWork() throws OperatorException {

            String dir = getParameterAsString(PARAMETER_DIR);
            
            
            File file = new File(dir);
            if (!file.exists())
            {
            	file.mkdirs();
            	
            }
            
            dummyPorts.passDataThrough();
                
        }

        @Override
        public List<ParameterType> getParameterTypes() {
                List<ParameterType> types = super.getParameterTypes();
                types.add(new ParameterTypeFile(PARAMETER_DIR, "Directory to create", "fil", false));

                return types;
        }

}
