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
package com.rapid_i.deployment.update.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.rapidminer.RapidMiner;
import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.gui.tools.SwingTools;

/**
 * Renders a cell of the update list. This contains icons for the type of extension or update.
 * 
 * @author Simon Fischer
 */
final class UpdateListCellRenderer extends JPanel implements ListCellRenderer {

	private static final Icon SELECTED_ICON = SwingTools.createIcon("16/checkbox.png");
	private static final Icon NON_SELECTED_ICON = SwingTools.createIcon("16/checkbox_unchecked.png");
	
//	private static final Icon COMMERCIAL_ICON = SwingTools.createIcon("16/shopping_cart_empty.png");
//	private static final Icon FREE_ICON = SwingTools.createIcon("16/nonprofit.png");

	/**
	 * 
	 */
	private final UpdateListPanel packageDescriptorListPanel;

	private static final long serialVersionUID = 1L;
	
//	private final JLabel freeCommercial = new JLabel();
	private final JLabel selectedLabel = new JLabel();
	private final JLabel label = new JLabel();
	
	UpdateListCellRenderer(UpdateListPanel updateListPanel) {
		packageDescriptorListPanel = updateListPanel;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(selectedLabel);
//		add(freeCommercial);
		add(label);
		setOpaque(true);
	}
	
	private Map<String,Icon> icons = new HashMap<String,Icon>();
	private Icon getIcon(PackageDescriptor pd) {
		if (pd.getIcon() == null) {
			return null;
		} else {
			Icon result = icons.get(pd.getPackageId());
			if (result == null) {
				result = new ImageIcon(pd.getIcon());
				icons.put(pd.getPackageId(), result);
			}
			return result;
		}
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {			
		if (isSelected) {
			setBackground(SwingTools.LIGHT_BLUE);
		} else {
			if (index % 2 == 0) {
				setBackground(Color.WHITE);
			} else {
				setBackground(SwingTools.LIGHTEST_BLUE);
			}
		}
		
		PackageDescriptor desc = (PackageDescriptor) value;
		String text = "<html><strong>"+desc.getName()+"</strong> "+desc.getVersion()+"<br/>";
		ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
		boolean upToDate = false;
		if (desc.getPackageTypeName().equals("RAPIDMINER_PLUGIN")) {
			if (ext == null) {
				text += "Not installed";
			} else {
				String installed = ext.getLatestInstalledVersion();
				String selected = ext.getSelectedVersion();
				if (installed != null) {
					upToDate = installed.compareTo(desc.getVersion()) >= 0;
					if (upToDate) {
						text += "This package is up to date.";
					} else {
						if (installed.equals(selected)) {
							text += "Installed version: " + ext.getSelectedVersion();
						} else {
							text += "Installed version: " + ext.getSelectedVersion() + " (selected version: "+selected+")";
						}
					}
				} else {
					text += "No version installed.";
				}
			}	
		} else if (desc.getPackageTypeName().equals("STAND_ALONE")) {
			String myVersion = RapidMiner.getLongVersion();
			upToDate = ManagedExtension.normalizeVersion(myVersion).compareTo(ManagedExtension.normalizeVersion(desc.getVersion())) >= 0;
			if (upToDate) {
				text += "This package is up to date.";
			} else {
				text += "Installed version: " + myVersion;
			}
		}
		text += "</html>";
		label.setText(text);
		label.setIcon(getIcon(desc));
		boolean selected = packageDescriptorListPanel.isSelected(desc);
		selectedLabel.setIcon(selected ? SELECTED_ICON : NON_SELECTED_ICON);
		//freeCommercial.setIcon(UpdateManager.COMMERCIAL_LICENSE_NAME.equals(desc.getLicenseName()) ? COMMERCIAL_ICON : FREE_ICON);
		
		SwingTools.setEnabledRecursive(this, !upToDate);
		
		if ("COMMERCIAL".equals(desc.getLicenseName())) {
			if (packageDescriptorListPanel.isPurchased(desc)) {
				selectedLabel.setEnabled(true);
			} else {
				selectedLabel.setEnabled(false);
			}
		}
		label.setForeground(upToDate ? Color.GRAY : Color.BLACK);
		return this;
	}

}
