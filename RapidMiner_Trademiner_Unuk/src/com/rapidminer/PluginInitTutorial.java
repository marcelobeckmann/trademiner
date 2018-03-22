/**
 * 
 */
package com.rapidminer;

import com.rapidminer.gui.MainFrame;

/**
 * This class provides hooks for initialization
 * 
 * @author Sebastian Land
 */
public class PluginInitTutorial {
	
	/**
	 * This method will be called directly after the extension is initialized.
	 * This is the first hook during start up. No initialization of the operators
	 * or renderers has taken place when this is called.
	 */
	public static void initPlugin() {}
	
	/**
	 * This method is called during start up as the second hook. It is 
	 * called before the gui of the mainframe is created. The Mainframe is 
	 * given to adapt the gui.
	 * The operators and renderers have been registered in the meanwhile.
	 */
	public static void initGui(MainFrame mainframe) {
	/*	final SimpleWindow simpleWindow = new SimpleWindow();
		mainframe.getDockingDesktop().registerDockable(simpleWindow);
		
		JMenu menu = new ResourceMenu("tutorial.tutorial");
		menu.add(new ResourceAction("tutorial.greetings", "Earthling") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				simpleWindow.setLabel("Greetings, Earthling");
			}
			
		});
		
		mainframe.getMainMenuBar().add(menu);
	*/
	}
	
	/**
	 * The last hook before the splash screen is closed. Third in the row.
	 */
	public static void initFinalChecks() {}

	/**
	 * Will be called as fourth method, directly before the UpdateManager is used
	 * for checking updates. Location for exchanging the UpdateManager. 
	 * The name of this method unfortunately is a result of a historical typo, so it's
	 * a little bit misleading.
	 */
	public static void initPluginManager() {}
}
