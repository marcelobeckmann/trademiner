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

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.RapidMiner;
import com.rapidminer.deployment.client.wsimport.AccountService;
import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.deployment.client.wsimport.UpdateService;
import com.rapidminer.gui.tools.ExtendedHTMLJEditorPane;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.plugin.Dependency;

/**
 * 
 * @author Simon Fischer
 * 
 */
public class UpdateListPanel extends JPanel {

	private final class PackageListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getElementAt(int index) {
			return descriptors.get(index);
		}

		@Override
		public int getSize() {
			return descriptors.size();
		}

		private void update(PackageDescriptor descr) {
			int index = descriptors.indexOf(descr);
			fireContentsChanged(this, index, index);
		}
		
		private void add(PackageDescriptor desc) {
			descriptors.add(desc);
			fireIntervalAdded(this, descriptors.size()-1, descriptors.size()-1);
		}
	}

	private final Map<PackageDescriptor, Boolean> selectionMap = new HashMap<PackageDescriptor, Boolean>();
	private final Map<PackageDescriptor, List<Dependency>> dependencyMap = new HashMap<PackageDescriptor, List<Dependency>>();
	/** Read the comment of {@link #isPurchased(PackageDescriptor)}. */
	private final Set<String> purchasedPackages = new HashSet<String>();

	private final UpdateDialog updateDialog;

	private static final long serialVersionUID = 1L;

	private final ExtendedHTMLJEditorPane displayPane = new ExtendedHTMLJEditorPane("text/html", "<html></html>");
	private final JList updateList;
	private final JToggleButton installButton = new JToggleButton(new ResourceAction("update.select") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			toggleSelection();
		}
	});
	
	private final JButton fetchFromAccountButton = new JButton(new ResourceAction("update.fetch_bookmarks") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			fetchBookmarks();
		}		
	});

	private final PackageListModel listModel = new PackageListModel();

	private final List<PackageDescriptor> descriptors;

	private final JLabel sizeLabel = new JLabel();

	public UpdateListPanel(UpdateDialog dialog, List<PackageDescriptor> descriptors, String[] preselectedExtensions) {
		for (String pE : preselectedExtensions) {
			for (PackageDescriptor desc : descriptors) {
				if (desc.getPackageId().equals(pE)) {
					selectionMap.put(desc, true);
				}
			}
		}
		for (PackageDescriptor desc : descriptors) {
			if (desc.getDependencies() != null) {
				List<Dependency> dep = Dependency.parse(desc.getDependencies());
				if (!dep.isEmpty()) {
					dependencyMap.put(desc, dep);
				}
			}
		}
		this.updateDialog = dialog;
		displayPane.installDefaultStylesheet();
		displayPane.setEditable(false);
		this.descriptors = descriptors;
		updateList = new JList(listModel);
		updateList.setCellRenderer(new UpdateListCellRenderer(this));
		updateList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					PackageDescriptor desc = (PackageDescriptor) updateList.getSelectedValue();
					if (desc != null) {
						displayPane.setText(UpdateListPanel.this.toString(desc));
						installButton.setSelected(isSelected(desc));
					}
				}
			}
		});
		updateList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					toggleSelection();
				}
			}
		});
		displayPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception e1) {
						SwingTools.showVerySimpleErrorMessage("cannot_open_browser");
					}
				}
			}
		});

		updateSize();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 0.3;
		c.insets = new Insets(0, 0, ButtonDialog.GAP, 0);
		JScrollPane updateListPane = new ExtendedJScrollPane(updateList);
		updateListPane.setPreferredSize(new Dimension(400, 300));
		updateListPane.setBorder(ButtonDialog.createTitledBorder("Available Updates"));
		add(updateListPane, c);

		c.weighty = 0.7;
		JScrollPane jScrollPane = new ExtendedJScrollPane(displayPane);
		jScrollPane.setPreferredSize(new Dimension(400, 300));
		jScrollPane.setBorder(ButtonDialog.createTitledBorder("Description"));
		add(jScrollPane, c);

		c.weighty = 0;
		add(sizeLabel, c);
	}

	public AbstractButton getInstallButton() {
		return installButton;
	}

	public AbstractButton getFetchFromAccountButton() {
		return fetchFromAccountButton;
	}

	private String toString(PackageDescriptor descriptor) {
		StringBuilder b = new StringBuilder("<html>");
		b.append("<h2>").append(descriptor.getName() + "</h2>");
		Date date = new Date(descriptor.getCreationTime().toGregorianCalendar().getTimeInMillis());
		b.append("<hr noshade=\"true\"/><strong>").append(descriptor.getVersion()).append(", released ").append(Tools.formatDate(date));
		b.append(", ").append(Tools.formatBytes(descriptor.getSize())).append("</strong>");
		if ((descriptor.getDependencies() != null) && !descriptor.getDependencies().isEmpty()) {
			b.append("<br/>Depends on: " + descriptor.getDependencies());
		}
		b.append("<p>").append(descriptor.getDescription()).append("</p>");
		// Before you are shocked, read the comment of isPurchased() :-)
		if (UpdateManager.COMMERCIAL_LICENSE_NAME.equals(descriptor.getLicenseName())) {
			if (isPurchased(descriptor)) {
				b.append("<p>You have purchased this package. However, you cannot install this extension with this version of RapidMiner. Please upgrade first.</p>");
			} else {
				try {
					b.append("<p><a href=" + UpdateManager.getUpdateServerURI("/shop/" + descriptor.getPackageId()).toString() + ">Order this extension.</a></p><p>You cannot install this extension with this pre-release of RapidMiner. Please upgrade first.</p>");
				} catch (URISyntaxException e) {
				}
			}
		}
		b.append("<br/><p><a href=\""+UpdateManager.getBaseUrl()+"/faces/product_details.xhtml?productId="+descriptor.getPackageId()+"\">Extension homepage</a></p>");
		b.append("</html>");
		return b.toString();
	}

	public boolean isSelected(PackageDescriptor desc) {
		Boolean selected = selectionMap.get(desc);
		return (selected != null) && selected.booleanValue();
	}

	private void updateSize() {
		int totalSize = getTotalSize();
		if (totalSize > 0) {
			sizeLabel.setText("Total download size: " + Tools.formatBytes(totalSize) + " (This may be less, if incremental updates are possible.)");
		} else {
			sizeLabel.setText(" ");
		}
	}

	private int getTotalSize() {
		int totalSize = 0;
		for (Map.Entry<PackageDescriptor, Boolean> entry : selectionMap.entrySet()) {
			if (entry.getValue()) {
				totalSize += entry.getKey().getSize();
			}
		}
		return totalSize;
	}

	public void startUpdate() {
		final List<PackageDescriptor> downloadList = new LinkedList<PackageDescriptor>();
		for (Entry<PackageDescriptor, Boolean> entry : selectionMap.entrySet()) {
			if (entry.getValue()) {
				downloadList.add(entry.getKey());
			}
		}
		updateDialog.startUpdate(downloadList);
	}

	private boolean isUpToDate(PackageDescriptor desc) {
		ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
		if (ext != null) {
			String remoteVersion = ManagedExtension.normalizeVersion(desc.getVersion());
			String myVersion = ManagedExtension.normalizeVersion(ext.getLatestInstalledVersion());
			if ((myVersion != null) && (remoteVersion.compareTo(myVersion) <= 0)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void toggleSelection() {
		PackageDescriptor desc = (PackageDescriptor) updateList.getSelectedValue();
		if (desc != null) {
			boolean select = !isSelected(desc);
			if (isUpToDate(desc)) {
				select = false;
			}
			if (desc.getPackageTypeName().equals("RAPIDMINER_PLUGIN")) {
				if (select) {
					resolveDependencies(desc);
				}
			} else if (desc.getPackageTypeName().equals("STAND_ALONE")) {
				String longVersion = RapidMiner.getLongVersion();
				String myVersion = ManagedExtension.normalizeVersion(longVersion);
				String remoteVersion = ManagedExtension.normalizeVersion(desc.getVersion());
				if ((myVersion != null) && (remoteVersion.compareTo(myVersion) <= 0)) {
					select = false;
				}
			}
			if (UpdateManager.COMMERCIAL_LICENSE_NAME.equals(desc.getLicenseName()) && !isPurchased(desc)) {
				select = false;
				SwingTools.showMessageDialog("purchase_package", desc.getName());
			}
			selectionMap.put(desc, select);
			listModel.update(desc);
		}
		updateSize();
	}

	private void resolveDependencies(PackageDescriptor desc) {
		List<Dependency> deps = dependencyMap.get(desc);
		if (deps != null) {
			for (Dependency dep : deps) {
				for (PackageDescriptor other : descriptors) {
					if (other.getPackageId().equals(dep.getPluginExtensionId())) {
						Boolean selected = selectionMap.get(other);
						boolean selectedB = (selected != null) && selected.booleanValue();
						if (!selectedB && !isUpToDate(other)) {
							selectionMap.put(other, true);
							resolveDependencies(other);
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * Currently, this is an unused feature. There are no extensions that can be purchased. Don't be afraid, RapidMiner
	 * is, and will always be, open source and free. However, future extensions like connectors to SAP or other data
	 * sources requiring proprietary drivers with expensive license fees may only be available on a commercial basis,
	 * for obvious reasons :-)
	 */
	public boolean isPurchased(PackageDescriptor desc) {
		return purchasedPackages.contains(desc.getPackageId());
	}
	
	/**
	 *  Connects to rapidupdate.de to fetch the bookmarks and automatically select them.
	 */
	public void fetchBookmarks() {
		// TODO: Do in progress thread
		List<String> bookmarks;
		try {
			AccountService accountService = UpdateManager.getAccountService();
			bookmarks = accountService.getBookmarkedProducts("rapidminer");
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("error_accessing_marketplace_account", e, e.toString());
			return;
		}

		Map<String,PackageDescriptor> packDescById = new HashMap<String,PackageDescriptor>();
		for (PackageDescriptor desc : descriptors) {
			packDescById.put(desc.getPackageId(), desc);
		}
		//for (PackageDescriptor desc : descriptors) {
		for (String bookmarkedId : bookmarks) {
			PackageDescriptor desc = packDescById.get(bookmarkedId);
			//if (bookmarks.contains(desc.getPackageId())) {
			//LogService.getRoot().log(Level.INFO, "Looking up "+bookmarkedId);
			LogService.getRoot().log(Level.INFO, "com.rapid_i.de.deployement.update.client.UpdateListPanel.looking_up", bookmarkedId);
			if (desc == null) {
				//LogService.getRoot().log(Level.INFO, "Bookmarked package "+bookmarkedId+" was unlisted. Fetching now.");
				LogService.getRoot().log(Level.INFO, "com.rapid_i.de.deployement.update.client.UpdateListPanel.fetching_bookmarked_package", bookmarkedId);
				String rmPlatform = "ANY"; //Launcher.getPlatform();
				try {
					UpdateService updateService = UpdateManager.getService();
					String latestRMVersion = updateService.getLatestVersion(bookmarkedId, rmPlatform);
					desc = updateService.getPackageInfo(bookmarkedId, latestRMVersion, rmPlatform);
					if (desc != null) {
						packDescById.put(desc.getPackageId(), desc);
						listModel.add(desc);
					}
				} catch (Exception e) {
					SwingTools.showSimpleErrorMessage("error_during_update", e);
					return;
				}
			}
			if (desc != null) {
				ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
				final boolean upToDate;
				if (ext == null) {
					upToDate = false;
				} else {
					String installed = ext.getLatestInstalledVersion();
					if (installed != null) {
						upToDate = installed.compareTo(desc.getVersion()) >= 0;
					} else {
						upToDate = false;
					}
				}
				if (!upToDate) {
					selectionMap.put(desc, true);
					resolveDependencies(desc);
				}
				continue;
			}
		}
		updateList.repaint();
	}

//	public static AccountService getAccountService() {
//		AccountServiceService ass = new AccountServiceService();
//		AccountService accountService = ass.getAccountServicePort();
//		return accountService;
//	}
}
