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
package com.rapidminer.parameter;
/**
 * A parameter type for specifying a repository location.
 * 
 * @author Simon Fischer, Sebastian Land
 */
public class ParameterTypeRepositoryLocation extends ParameterTypeString {

	private static final long serialVersionUID = 1L;

	private boolean allowFolders, allowEntries;
	
	/** Creates a new parameter type for files with the given extension. If the extension is null
	 *  no file filters will be used. */
	public ParameterTypeRepositoryLocation(String key, String description, boolean optional) {
		this(key, description, true, false, optional);
	}	

	/** Creates a new parameter type for files with the given extension. If the extension is null
	 *  no file filters will be used. */
	public ParameterTypeRepositoryLocation(String key, String description, boolean allowEntries, boolean allowDirectories, boolean optional) {
		super(key, description, null);
		
		setOptional(optional);	
		setAllowEntries(allowEntries);
		setAllowFolders(allowDirectories);
	}

	public boolean isAllowFolders() {
		return allowFolders;
	}

	public void setAllowFolders(boolean allowFolders) {
		this.allowFolders = allowFolders;
	}

	public boolean isAllowEntries() {
		return allowEntries;
	}

	public void setAllowEntries(boolean allowEntries) {
		this.allowEntries = allowEntries;
	}	
}
