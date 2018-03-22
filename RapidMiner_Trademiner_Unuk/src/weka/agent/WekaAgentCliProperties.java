
package weka.agent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WekaAgentCliProperties {
    
        public static void main(String args[])
        {
            
            WekaAgentCliProperties prop=WekaAgentCliProperties.getInstance();

            System.out.println(prop.getClassifier());
        }

	private Properties properties= new Properties();

	private static WekaAgentCliProperties instance;
	private WekaAgentCliProperties() 
	{
	    final String PROPERTY_FILE = "wekaagentcli.properties";
	    InputStream stream = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
	    
	    if (stream!=null) {
	        try {
                properties.load(stream);
            } catch (IOException e) {
               throw new RuntimeException(e.getMessage(),e);
            }
	    }
	    else
	    {  throw new RuntimeException("File not found " + PROPERTY_FILE);} 
	    
	}
	public static WekaAgentCliProperties getInstance() 
	{
		if (instance==null)
		{
			instance = new WekaAgentCliProperties();
			
		}
		
		return instance;
		
	}
	
	
	
	public String getOutput()
	{
		
		return properties.getProperty("OUTPUT");
	}
	
	public String getClassifier()
	{
		return properties.getProperty("CLASSIFIER");
	}

	public String getWekaCommand()
	{
		
		return properties.getProperty("WEKACOMMAND");
	}

	public String getLookupPath()
	{
		
		return properties.getProperty("LOOKUP_PATH");
	}
	
	
	public int getPoolingInterval()
	{
		
		return Integer.parseInt(properties.getProperty("POOLING_INTERVAL"));
	}
	
	
	public String getAlgo()
	{
		
		return properties.getProperty("CNN");
	}
	
	
	public String getOutputSuffix()
	{
		
		return properties.getProperty("OUT_SUFFIX");
	}
	
	
	public String getLastSymbol()
	{
		
		return properties.getProperty("LAST_SYMBOL");
	}
	

}
