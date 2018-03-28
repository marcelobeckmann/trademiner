/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.rapidminer.gui.tools.SwingTools;

/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 */
public class WelcomeOpenAction extends AbstractAction {

	private static final long serialVersionUID = 1358354112149248404L;

	private static final String ICON_NAME = "folder.png";
	
	private static Icon icon = null;
	
	static {
		icon = SwingTools.createIcon("48/" + ICON_NAME);
	}
		
	public WelcomeOpenAction() {
		super("Open", icon);
		putValue(SHORT_DESCRIPTION, "Open an existing process definition");
	}

	public void actionPerformed(ActionEvent e) {
		OpenAction.open();
	}
}
