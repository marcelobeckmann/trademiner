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
package com.rapidminer.operator.ports.impl;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.gui.tools.dialogs.SelectionInputDialog;
import com.rapidminer.operator.IOMerger;
import com.rapidminer.operator.IOMultiplier;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.operator.ports.PortException;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.OperatorService;

/**
 * 
 * @author Simon Fischer, Tobias Malbrecht
 */
public class CannotConnectPortException extends PortException {

	private static final long serialVersionUID = 5242982041478562116L;
	
	private final OutputPort source;
	private final InputPort dest;
	private final String reason;

	private static final String[] OPTIONS = {
		I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.input.cannot_connect.option.disconnect_connect"),
		I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.input.cannot_connect.option.insert_multiplier"),
		I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.input.cannot_connect.option.dont_connect"),
	};
	
	public CannotConnectPortException(OutputPort source, InputPort dest, InputPort sourceDest, OutputPort destSource) {
	super("Cannot connect "+source.getSpec() + " to " + dest.getSpec());
		this.source = source;
		this.dest = dest;
		this.reason = I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.input.cannot_connect.reason.both_connected", sourceDest.getSpec(), destSource.getSpec());
	}
	
	public CannotConnectPortException(OutputPort source, InputPort dest, InputPort sourceDest) {
		super("Cannot connect "+source.getSpec() + " to " + dest.getSpec());
		this.source = source;
		this.dest   = dest;
		this.reason = I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.input.cannot_connect.reason.source_connected", sourceDest.getSpec());
	}
	
	public CannotConnectPortException(OutputPort source, InputPort dest, OutputPort destSource) {
		super("Cannot connect "+source.getSpec() + " to " + dest.getSpec());
		this.source = source;
		this.dest   = dest;
		this.reason = I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.input.cannot_connect.reason.dest_connected", destSource.getSpec());
	}

	@Override
	public boolean hasRepairOptions() {
		return true;
	}

	@Override
	public void showRepairDialog(Component component) {
		List<Port> toUnlock = new LinkedList<Port>();
		try {
			SelectionInputDialog dialog = new SelectionInputDialog("cannot_connect", OPTIONS, OPTIONS[0]) {
				private static final long serialVersionUID = -5167666577334883031L;

				@Override
				protected String getInfoText() {
					return I18N.getMessage(I18N.getGUIBundle(), getKey() + ".message", source.getSpec(), dest.getSpec(), reason);
				}
			};
			dialog.setVisible(true);
			Object input = dialog.getInputSelection();
			if (input == null) {
				return;
			}
			if (input == OPTIONS[0]) {
				toUnlock.add(source);
				toUnlock.add(dest);
				source.lock();
				dest.lock();
				if (source.isConnected()) {
					source.disconnect();
				}
				if (dest.isConnected()) {
					dest.getSource().disconnect();
				}
				source.connectTo(dest);
			} else if (input == OPTIONS[1]) {
				boolean repairSource = source.isConnected();
				boolean repairDest = dest.isConnected();
								
				if (repairSource) {
					try {
						InputPort oldDest = source.getDestination();
						toUnlock.add(source);
						toUnlock.add(source.getDestination());
						source.getDestination().lock();
						source.lock();
						source.disconnect();
						IOMultiplier multiplier = OperatorService.createOperator(IOMultiplier.class);
						source.getPorts().getOwner().getConnectionContext().addOperator(multiplier);
						source.connectTo(multiplier.getInputPorts().getPortByIndex(0));
						multiplier.getOutputPorts().getPortByIndex(0).connectTo(oldDest);
						multiplier.getOutputPorts().getPortByIndex(1).connectTo(dest);
					} catch (OperatorCreationException e) {				
						throw new PortException("Cannot create multiplier: " + e);
					}
				}
				if (repairDest) {
					OutputPort oldSource = dest.getSource();					
					toUnlock.add(oldSource);
					toUnlock.add(dest);
					oldSource.lock();
					dest.lock();
					oldSource.disconnect();
					try {
						IOMerger merger = OperatorService.createOperator(IOMerger.class);
						source.getPorts().getOwner().getConnectionContext().addOperator(merger);
						oldSource.connectTo(merger.getInputPorts().getPortByIndex(0));
						source.connectTo(merger.getInputPorts().getPortByIndex(1));
						merger.getOutputPorts().getPortByIndex(0).connectTo(dest);
					} catch (OperatorCreationException e) {				
						throw new PortException("Cannot create multiplier: " + e);
					}
				}
			} else {
				return;
			}
		} finally {
			for (Port port : toUnlock) {
				port.unlock();
			}
		}
	}

}
