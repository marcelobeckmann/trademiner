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
package com.rapidminer.gui.new_plotter.gui;

import java.awt.Color;
import java.awt.datatransfer.Transferable;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.rapidminer.gui.new_plotter.configuration.ValueSource.SeriesUsageType;
import com.rapidminer.gui.new_plotter.gui.dnd.DataTableColumnDropTextFieldTransferHandler;
import com.rapidminer.gui.new_plotter.gui.dnd.DataTableColumnListTransferHandler;
import com.rapidminer.gui.new_plotter.listener.DragListener;


/**
 * @author Nils Woehler
 * 
 */
public class AttributeDropTextField extends JTextField implements DragListener {

	private static final long serialVersionUID = 1L;

	private Border ongoingDropBorder;
	private Border dropEndedBorder;

	public AttributeDropTextField(JTree plotConfigurationTree, DataTableColumnListTransferHandler th, SeriesUsageType type) {
		th.addDragStartListener(this);

		this.setFocusable(false);
		this.setEditable(false);
		this.setBackground(Color.white);
		this.setTransferHandler(new DataTableColumnDropTextFieldTransferHandler(plotConfigurationTree, type, this));

		ongoingDropBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(43, 128, 0), new Color(43, 128, 0));
		dropEndedBorder = BorderFactory.createEmptyBorder();
	}

	@Override
	public void dragStarted(Transferable t) {
		TransferSupport support = new TransferSupport(this, t);
		boolean doesSupportFlavor = ((DataTableColumnDropTextFieldTransferHandler) getTransferHandler()).doesSupportFlavor(support);
		if (doesSupportFlavor) {
			setBorder(ongoingDropBorder);
		}
	}
	
	@Override
	public void dragEnded() {
		setBorder(dropEndedBorder);		
	}

}
