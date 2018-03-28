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
package com.rapidminer.repository.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.Action;

import com.rapidminer.repository.Entry;
import com.rapidminer.repository.Folder;
import com.rapidminer.repository.MalformedRepositoryLocationException;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;

/**
 * @author Simon Fischer
 */
public abstract class SimpleEntry implements Entry {

	protected static final String PROPERTIES_SUFFIX = ".properties";

	private Properties properties;

	private String name;
	private LocalRepository repository;
	private SimpleFolder containingFolder;

	SimpleEntry(String name, SimpleFolder containingFolder, LocalRepository repository) {
		this.name = name;
		this.repository = repository;
		this.containingFolder = containingFolder;
	}

	protected LocalRepository getRepository() {
		return repository;
	}

	protected void setRepository(LocalRepository repository) {
		this.repository = repository;
	}

	/** Sets the name but does not fire any events. */
	void setName(String name) {
		this.name = name;
	}

	@Override
	public Folder getContainingFolder() {
		return containingFolder;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean rename(String newName) {
		renameFile(getPropertiesFile(), newName);
		this.name = newName;
		getRepository().fireEntryRenamed(this);
		return true;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean willBlock() {
		return false;
	}

	@Override
	public String getOwner() {
		return getProperty("owner");
	}

	@Override
	public RepositoryLocation getLocation() {
		try {
			if (getContainingFolder() != null) {
				return new RepositoryLocation(getContainingFolder().getLocation(), getName());
			} else {
				return new RepositoryLocation(getRepository().getName(), new String[] { getName() });
			}
		} catch (MalformedRepositoryLocationException e) {
			throw new RuntimeException(e);
		}
	}

	/** Renames the file, keeping the extension and directory unchanged. 
	 *  If the file does not exist, returns silently. */
	void renameFile(File file2, String newBaseName) {
		if (!file2.exists()) {
			//LogService.getRoot().warning("Cannot rename "+file2+": does not exist.");
			LogService.getRoot().log(Level.WARNING, "com.rapidminer.repository.local.SimpleEntry.renaming_file2_error", file2);
			return;
		}
		String name = file2.getName();
		int dot = name.lastIndexOf('.');
		if (dot == -1) {
			file2.renameTo(new File(file2.getParentFile(), newBaseName));
		} else {
			String extension = name.substring(dot + 1);
			file2.renameTo(new File(file2.getParentFile(), newBaseName + "." + extension));
		}
	}

	/**
	 * Moves a file to the new target directory without renaming it.
	 */
	boolean moveFile(File file, File targetDirectory) {
		return file.renameTo(new File(targetDirectory, file.getName()));
	}

	/**
	 * Moves the file to a new location.
	 *  
	 * @param newEntryName The {@link Entry}'s new name (without file extension). If newEntryName is null the old name will be used.
	 * @param extensionSuffix The {@link Entry}'s extension suffix. Will be used if newEntryName is not null to keep the correct suffix.
	 */
	boolean moveFile(File file, File targetDirectory, String newEntryName, String extensionSuffix) {
		String name;
		if (newEntryName == null) {
			name = file.getName();
		} else {
			name = newEntryName + extensionSuffix;
		}
		return file.renameTo(new File(targetDirectory, name));
	}

	@Override
	public boolean move(Folder newParent) {
		moveFile(getPropertiesFile(), ((SimpleFolder)newParent).getFile());
		this.containingFolder.removeChild(this);
		this.containingFolder = (SimpleFolder) newParent;
		this.containingFolder.addChild(this);
		return true;
	}

	@Override
	public boolean move(Folder newParent, String newName) {
		moveFile(getPropertiesFile(), ((SimpleFolder)newParent).getFile(), newName, PROPERTIES_SUFFIX);

		this.containingFolder.removeChild(this);

		if (newName != null) {
			this.name = newName;
		}

		this.containingFolder = (SimpleFolder) newParent;
		this.containingFolder.addChild(this);
		
		return true;
	}

	/* Properties
	 * We store the owner in a properties file because there is no system independent way
	 * of determining the user.
	 * TODO: Check if Java 7 has such a feature.
	 */
	private void loadProperties() {
		File propertiesFile = getPropertiesFile();
		if ((propertiesFile != null) && propertiesFile.exists()) {
			InputStream in;
			try {
				in = new FileInputStream(propertiesFile);
			} catch (FileNotFoundException e) {
				//LogService.getRoot().log(Level.WARNING, "Error loading repository entry properties from "+propertiesFile+": "+e, e);
				LogService.getRoot().log(Level.WARNING,
						I18N.getMessage(LogService.getRoot().getResourceBundle(), 
						"com.rapidminer.repository.local.SimpleEntry.loading_repository_entry_properties_error", 
						propertiesFile, e),
						e);
				return;
			}
			try {
				this.properties.loadFromXML(in);
			} catch (Exception e) {
				//LogService.getRoot().log(Level.WARNING, "Error loading repository entry properties from "+propertiesFile+": "+e, e);
				LogService.getRoot().log(Level.WARNING,
						I18N.getMessage(LogService.getRoot().getResourceBundle(), 
						"com.rapidminer.repository.local.SimpleEntry.loading_repository_entry_properties_error", 
						propertiesFile, e),
						e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {}
			}
		}
	}

	private void storeProperties() {
		File propertiesFile = getPropertiesFile();
		if (propertiesFile != null) {
			FileOutputStream os;
			try {
				os = new FileOutputStream(propertiesFile);
			} catch (FileNotFoundException e1) {
				//LogService.getRoot().log(Level.WARNING, "Error storing repository entry properties to "+propertiesFile+": "+e1, e1);
				LogService.getRoot().log(Level.WARNING,
						I18N.getMessage(LogService.getRoot().getResourceBundle(), 
						"com.rapidminer.repository.local.SimpleEntry.storing_repository_entry_properties_error", 
						propertiesFile, e1),
						e1);
				return;
			}
			try {
				properties.storeToXML(os, "Properties of repository entry " + getName());
			} catch (IOException e) {
				//LogService.getRoot().log(Level.WARNING, "Error storing repository entry properties to "+propertiesFile+": "+e, e);
				LogService.getRoot().log(Level.WARNING,
						I18N.getMessage(LogService.getRoot().getResourceBundle(), 
						"com.rapidminer.repository.local.SimpleEntry.storing_repository_entry_properties_error", 
						propertiesFile, e),
						e);
			} finally {
				try {
					os.close();
				} catch (IOException e) {}
			}
		}
	}

	private synchronized Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			loadProperties();
			if (properties.getProperty("owner") == null) {
				putProperty("owner", System.getProperty("user.name"));
			}
		}
		return properties;
	}

	protected void putProperty(String key, String value) {
		if (value != null) {
			getProperties().setProperty(key, value);
			storeProperties();
		}
	}

	protected String getProperty(String key) {
		return getProperties().getProperty(key);
	}

	private File getPropertiesFile() {
		if (getContainingFolder() != null) {
			return new File(((SimpleFolder) getContainingFolder()).getFile(), getName() + PROPERTIES_SUFFIX);
		} else {
			return new File(getRepository().getRoot(), getName() + PROPERTIES_SUFFIX);
		}
	}

	@Override
	public void delete() throws RepositoryException {
		File propFile = getPropertiesFile();
		if (propFile.exists()) {
			propFile.delete();
		}
		SimpleFolder parent = (SimpleFolder) getContainingFolder();
		if (parent != null) {
			parent.removeChild(this);
		}
	}

	@Override
	public Collection<Action> getCustomActions() {
		return null;
	}
}
