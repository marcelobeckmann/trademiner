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

import java.awt.event.ActionEvent;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import com.rapid_i.Launcher;
import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.deployment.client.wsimport.UpdateService;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.PasswordDialog;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.tools.GlobalAuthenticator;
import com.rapidminer.tools.LogService;

/**
 * 
 * @author Simon Fischer
 * 
 */
public class UpdateDialog extends ButtonDialog {

	private static final long serialVersionUID = 1L;

	public static final Action UPDATE_ACTION = new ResourceAction("update_manager") {
		private static final long serialVersionUID = 1L;
		{
			setCondition(EDIT_IN_PROGRESS, DONT_CARE);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showUpdateDialog();
		}
	};

	private final UpdateService service;

	private final UpdateListPanel ulp;

	static {
		GlobalAuthenticator.registerServerAuthenticator(new GlobalAuthenticator.URLAuthenticator() {
			@Override
			public PasswordAuthentication getAuthentication(URL url) {
				try {
					if (url.toString().startsWith(UpdateManager.getUpdateServerURI("").toString())) {
						return PasswordDialog.getPasswordAuthentication(url.toString(), false, false);
					} else {
						return null;
					}
				} catch (URISyntaxException e) {
					return null;
				}
			}

			@Override
			public String getName() {
				return "UpdateService authenticator.";
			}
			
			@Override
			public String toString() {
				return getName();
			}
		});
	}

	public UpdateDialog(UpdateService service, List<PackageDescriptor> descriptors, String[] preselectedExtensions) {
		super("update");
		this.service = service;
		ulp = new UpdateListPanel(this, descriptors, preselectedExtensions);
		layoutDefault(ulp,	ulp.getFetchFromAccountButton(), ulp.getInstallButton(), makeOkButton("update.install"), makeCloseButton());
	}

	public static void showUpdateDialog(final String... preselectedExtensions) {
		new ProgressThread("fetching_updates", true) {
			public void run() {
				getProgressListener().setTotal(100);
				getProgressListener().setCompleted(10);
				final UpdateService service;
				try {
					service = UpdateManager.getService();
				} catch (Exception e) {
					SwingTools.showSimpleErrorMessage("failed_update_server", e, UpdateManager.getBaseUrl());
					getProgressListener().complete();
					return;
				} finally {
					getProgressListener().complete();
				}
				//final UpdateService service = serviceTmp;
				try {
					getProgressListener().setCompleted(20);

					final List<PackageDescriptor> descriptors = new LinkedList<PackageDescriptor>();

					if (Launcher.isDevelopmentBuild()) {
						//LogService.getRoot().config("This is a development build. Ignoring update check.");
						LogService.getRoot().log(Level.CONFIG, "com.rapid_i.deployment.update.client.UpdateDialog.ignoring_update_check");
					} else {
						String rmPlatform = Launcher.getPlatform();
						String latestRMVersion = service.getLatestVersion(UpdateManager.PACKAGEID_RAPIDMINER, rmPlatform);
						PackageDescriptor packageInfo = service.getPackageInfo(UpdateManager.PACKAGEID_RAPIDMINER, latestRMVersion, rmPlatform);
						if (packageInfo != null) {
							descriptors.add(packageInfo);
						}
					}
					getProgressListener().setCompleted(30);

					String targetPlatform = "ANY";
					List<String> extensions = service.getExtensions(UpdateManager.PACKAGEID_RAPIDMINER);
					int i = 0;
					for (String extension : extensions) {
						String version = service.getLatestVersion(extension, targetPlatform);
						PackageDescriptor packageInfo = service.getPackageInfo(extension, version, targetPlatform);
						descriptors.add(packageInfo);
						i++;
						getProgressListener().setCompleted(30 + 70 * i / extensions.size());
					}
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							UpdateManager.saveLastUpdateCheckDate();
							new UpdateDialog(service, descriptors, preselectedExtensions).setVisible(true);
						}
					});
				} catch (Exception e) {
					SwingTools.showSimpleErrorMessage("error_during_update", e, e.getMessage());
				} finally {
					getProgressListener().complete();
				}
			}
		}.start();
	}

	public void startUpdate(final List<PackageDescriptor> downloadList) {
		new ProgressThread("installing_updates", true) {
			@Override
			public void run() {
				try {
					getProgressListener().setTotal(100);
					getProgressListener().setCompleted(10);

					// Download licenses
					Map<String, String> licenses = new HashMap<String, String>();
					for (PackageDescriptor desc : downloadList) {
						String license = licenses.get(desc.getLicenseName());
						if (license == null) {
							license = service.getLicenseText(desc.getLicenseName());
							licenses.put(desc.getLicenseName(), license);
						}
					}

					// Confirm licenses
					getProgressListener().setCompleted(20);
					List<PackageDescriptor> acceptedList = new LinkedList<PackageDescriptor>();
					for (PackageDescriptor desc : downloadList) {
						if (ConfirmLicenseDialog.confirm(desc, licenses.get(desc.getLicenseName()))) {
							acceptedList.add(desc);
						}
					}

					if (!acceptedList.isEmpty()) {
						UpdateManager um = new UpdateManager(service);
						um.performUpdates(acceptedList, getProgressListener());
						getProgressListener().complete();
						UpdateDialog.this.dispose();
						// TODO: re-enable
						// ManagedExtension.checkForLicenseConflicts();
						if (SwingTools.showConfirmDialog("update.complete_restart", ConfirmDialog.YES_NO_OPTION) == ConfirmDialog.YES_OPTION) {
							RapidMinerGUI.getMainFrame().exit(true);
						}
					} else {
						getProgressListener().complete();
					}
				} catch (Exception e) {
					SwingTools.showSimpleErrorMessage("error_installing_update", e, e.getMessage());
				}
			}
		}.start();
	}

	@Override
	protected void ok() {
		ulp.startUpdate();
	}
}
