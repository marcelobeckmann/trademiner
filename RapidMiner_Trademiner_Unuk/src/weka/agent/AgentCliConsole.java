package weka.agent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
 
public class AgentCliConsole extends JPanel  {
    public JTextArea output;
    
    
    public static AgentCliConsole instance;
    
    public static AgentCliConsole getInstance()
    {
        
        if (instance==null)
        {
            instance = new AgentCliConsole();
            
            
        }
        return instance;
        
        
    }
    
    private AgentCliConsole() {
        super(new GridBagLayout());
 
 
        output = new JTextArea(4, 20);
        output.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(output);
 
        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
 
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(scrollPane, c);
    }
 
   
    
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public static void showGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TextDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add contents to the window.
        frame.add(getInstance());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void println(String line)
    {
        
        getInstance().output.append(line+'\n');
     
        
    }
}
