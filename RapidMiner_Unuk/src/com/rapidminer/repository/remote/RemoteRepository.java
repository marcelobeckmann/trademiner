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
package com.rapidminer.repository.remote;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.event.EventListenerList;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.rapid_i.repository.wsimport.ProcessService;
import com.rapid_i.repository.wsimport.ProcessService_Service;
import com.rapid_i.repository.wsimport.RepositoryService;
import com.rapid_i.repository.wsimport.RepositoryService_Service;
import com.rapidminer.gui.actions.BrowseAction;
import com.rapidminer.gui.tools.PasswordDialog;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.io.Base64;
import com.rapidminer.io.process.XMLTools;
import com.rapidminer.repository.Entry;
import com.rapidminer.repository.Folder;
import com.rapidminer.repository.Repository;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryListener;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.gui.RemoteRepositoryPanel;
import com.rapidminer.repository.gui.RepositoryConfigurationPanel;
import com.rapidminer.tools.GlobalAuthenticator;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.XMLException;
import com.rapidminer.tools.cipher.CipherException;
import com.rapidminer.tools.jdbc.connection.DatabaseConnectionService;
import com.rapidminer.tools.jdbc.connection.FieldConnectionEntry;

/**
 * A repository connecting to a RapidAnalytics installation.
 * 
 * @author Simon Fischer
 */
public class RemoteRepository extends RemoteFolder implements Repository {

	/** Type of object requested from a server.*/
	public static enum EntryStreamType {
		METADATA, IOOBJECT, PROCESS, BLOB
	}

	private URL baseUrl;
	private String alias;
	private String username;
	private char[] password;
	private RepositoryService repositoryService;
	private ProcessService processService;
	private final EventListenerList listeners = new EventListenerList();

	private static final Map<URI, WeakReference<RemoteRepository>> ALL_REPOSITORIES = new HashMap<URI, WeakReference<RemoteRepository>>();
	private static final Object MAP_LOCK = new Object();

	private boolean offline = true;
	private boolean isHome;

	static {
		GlobalAuthenticator.registerServerAuthenticator(new GlobalAuthenticator.URLAuthenticator() {
			@Override
			public PasswordAuthentication getAuthentication(URL url) {
				WeakReference<RemoteRepository> reposRef = null;// = ALL_REPOSITORIES.get(url);
				for (Map.Entry<URI, WeakReference<RemoteRepository>> entry : ALL_REPOSITORIES.entrySet()) {
					if (url.toString().startsWith(entry.getKey().toString()) || url.toString().replace("127\\.0\\.0\\.1", "localhost").startsWith(entry.getKey().toString())) {
						reposRef = entry.getValue();
						break;
					}
				}

				if (reposRef == null) {
					return null;
				}
				RemoteRepository repository = reposRef.get();
				if (repository != null) {
					return repository.getAuthentiaction();
				} else {
					return null;
				}
			}

			@Override
			public String getName() {
				return "Repository authenticator";
			}
			
			@Override
			public String toString() {
				return getName();
			}
		});
	}

	public RemoteRepository(URL baseUrl, String alias, String username, char[] password, boolean isHome) {
		super("/");
		setRepository(this);
		this.setAlias(alias);
		this.baseUrl = baseUrl;
		this.setUsername(username);
		this.isHome = isHome;
		if ((password != null) && (password.length > 0)) {
			this.setPassword(password);
		} else {
			this.setPassword(null);
		}
		register(this);
	}

	private static void register(RemoteRepository remoteRepository) {
		synchronized (MAP_LOCK) {
			try {
				ALL_REPOSITORIES.put(remoteRepository.getBaseUrl().toURI(), new WeakReference<RemoteRepository>(remoteRepository));
			} catch (URISyntaxException e) {
				//LogService.getRoot().log(Level.SEVERE, "Could not add repository URI: " + remoteRepository.getBaseUrl().toExternalForm(), e);
				LogService.getRoot().log(Level.WARNING,
						I18N.getMessage(LogService.getRoot().getResourceBundle(), 
						"com.rapidminer.repository.remote.RemoteRepository.adding_repository_uri_error", 
						remoteRepository.getBaseUrl().toExternalForm()),
						e);
			}
		}
	}

	public URL getRepositoryServiceBaseUrl() {
		try {
			return new URL(getBaseUrl(), "RAWS/");
		} catch (MalformedURLException e) {
			// cannot happen
			//LogService.getRoot().log(Level.WARNING, "Cannot create Web service url: " + e, e);
			LogService.getRoot().log(Level.WARNING,
					I18N.getMessage(LogService.getRoot().getResourceBundle(), 
					"com.rapidminer.repository.remote.RemoteRepository.creating_webservice_error", 
					e),
					e);
			return null;
		}
	}
	
	private URL getRepositoryServiceWSDLUrl() {
		try {
			return new URL(getBaseUrl(), "RAWS/RepositoryService?wsdl");
		} catch (MalformedURLException e) {
			// cannot happen
			//LogService.getRoot().log(Level.WARNING, "Cannot create Web service url: " + e, e);
			LogService.getRoot().log(Level.WARNING,
					I18N.getMessage(LogService.getRoot().getResourceBundle(), 
					"com.rapidminer.repository.remote.RemoteRepository.creating_webservice_error", 
					e),
					e);
			return null;
		}
	}

	private URL getProcessServiceWSDLUrl() {
		try {
			return new URL(getBaseUrl(), "RAWS/ProcessService?wsdl");
		} catch (MalformedURLException e) {
			// cannot happen
			//LogService.getRoot().log(Level.WARNING, "Cannot create Web service url: " + e, e);
			LogService.getRoot().log(Level.WARNING,
					I18N.getMessage(LogService.getRoot().getResourceBundle(), 
					"com.rapidminer.repository.remote.RemoteRepository.creating_webservice_error", 
					e),
					e);
			return null;
		}
	}

	@Override
	public void addRepositoryListener(RepositoryListener l) {
		listeners.add(RepositoryListener.class, l);
	}

	@Override
	public void removeRepositoryListener(RepositoryListener l) {
		listeners.remove(RepositoryListener.class, l);
	}

	@Override
	public boolean rename(String newName) {
		this.setAlias(newName);
		fireEntryChanged(this);
		return true;
	}

	protected void fireEntryChanged(Entry entry) {
		for (RepositoryListener l : listeners.getListeners(RepositoryListener.class)) {
			l.entryChanged(entry);
		}
	}

	protected void fireEntryAdded(Entry newEntry, Folder parent) {
		for (RepositoryListener l : listeners.getListeners(RepositoryListener.class)) {
			l.entryAdded(newEntry, parent);
		}
	}

	protected void fireEntryRemoved(Entry removedEntry, Folder parent, int index) {
		for (RepositoryListener l : listeners.getListeners(RepositoryListener.class)) {
			l.entryRemoved(removedEntry, parent, index);
		}
	}

	protected void fireRefreshed(Folder folder) {
		for (RepositoryListener l : listeners.getListeners(RepositoryListener.class)) {
			l.folderRefreshed(folder);
		}
	}

	private Map<String, RemoteEntry> cachedEntries = new HashMap<String, RemoteEntry>();

	/** Connection entries fetched from server. */
	private Collection<FieldConnectionEntry> connectionEntries;
	private boolean cachedPasswordUsed = false;

	protected void register(RemoteEntry entry) {
		cachedEntries.put(entry.getPath(), entry);
	}

	@Override
	public Entry locate(String string) throws RepositoryException {
		return RepositoryManager.getInstance(null).locate(this, string, false);
//		Entry cached = cachedEntries.get(string);
//		if (cached != null) {
//			return cached;
//		}
//		Entry firstTry = RepositoryManager.getInstance(null).locate(this, string, true);
//		if (firstTry != null) {
//			return firstTry;
//		}
//
//		if (!string.startsWith("/")) {
//			string = "/" + string;
//		}
//
//		EntryResponse response = getRepositoryService().getEntry(string);
//		if (response.getStatus() != RepositoryConstants.OK) {
//			if (response.getStatus() == RepositoryConstants.NO_SUCH_ENTRY) {
//				return null;
//			}
//			throw new RepositoryException(response.getErrorMessage());
//		}
//		if (response.getType().equals(Folder.TYPE_NAME)) {
//			return new RemoteFolder(response, null, this);
//		} else if (response.getType().equals(ProcessEntry.TYPE_NAME)) {
//			return new RemoteProcessEntry(response, null, this);
//		} else if (response.getType().equals(IOObjectEntry.TYPE_NAME)) {
//			return new RemoteIOObjectEntry(response, null, this);
//		} else if (response.getType().equals(BlobEntry.TYPE_NAME)) {
//			return new RemoteBlobEntry(response, null, this);
//		} else {
//			throw new RepositoryException("Unknown entry type: " + response.getType());
//		}
	}

	@Override
	public String getName() {
		return getAlias();
	}

	@Override
	public String getState() {
		return (isOffline() ? "offline" : (repositoryService != null ? "connected" : "disconnected"));
	}
	
	@Override
	public String getIconName() {
		return I18N.getMessage(I18N.getGUIBundle(), "gui.repository.remote.icon");
	}

	/** Returns a short HTML description of this repository. Does not include surrounding <html> tags. */
	public String toHtmlString() {
		return getAlias() + "<br/><small style=\"color:gray\">(" + getBaseUrl() + ")</small>";
	}
	
//	@Override
//	public String toString() {
//		return "<html>" + getAlias() + "<br/><small style=\"color:gray\">(" + getBaseUrl() + ")</small></html>"; //super.toString();
//	}

	private PasswordAuthentication getAuthentiaction() {		
		if (password == null) {
			//LogService.getRoot().info("Authentication requested for URL: " + getBaseUrl());
			LogService.getRoot().log(Level.INFO, "com.rapidminer.tools.repository.remote.RemoteRepository.authentication_requested", getBaseUrl());
			PasswordAuthentication passwordAuthentication;
			if (cachedPasswordUsed) {
				// if we have used a cached password last time, and we enter this method again,
				// this is probably because the password was wrong, so rather force dialog than
				// using cache again.
				passwordAuthentication = PasswordDialog.getPasswordAuthentication(getBaseUrl().toString(), false, false);
				this.cachedPasswordUsed = false;
			} else {
				passwordAuthentication = PasswordDialog.getPasswordAuthentication(getBaseUrl().toString(), false, true);
				this.cachedPasswordUsed = true;
			}			
			if (passwordAuthentication != null) {
				this.setPassword(passwordAuthentication.getPassword());
				this.setUsername(passwordAuthentication.getUserName());
				RepositoryManager.getInstance(null).save();
			}
			return passwordAuthentication;
		} else {
			return new PasswordAuthentication(getUsername(), password);
		}
	}

	public RepositoryService getRepositoryService() throws RepositoryException {
		// if (offline) {
		// throw new RepositoryException("Repository "+getName()+" is offline. Connect first.");
		// }
		installJDBCConnectionEntries();
		if (repositoryService == null) {
			try {
				RepositoryService_Service serviceService = new RepositoryService_Service(getRepositoryServiceWSDLUrl(), new QName("http://service.web.rapidanalytics.de/", "RepositoryService"));
				repositoryService = serviceService.getRepositoryServicePort();
				
				setCredentials((BindingProvider)repositoryService);
				 
				setOffline(false);
			} catch (Exception e) {
				setOffline(true);
				setPassword(null);
				repositoryService = null;
				throw new RepositoryException("Cannot connect to " + getBaseUrl() + ": " + e, e);
			}
		}
		return repositoryService;
	}

	private void setCredentials(BindingProvider bp) {
		if (password != null) {
			bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, getUsername());
			bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, new String(password));
		}
	}

	public ProcessService getProcessService() throws RepositoryException {
		// if (offline) {
		// throw new RepositoryException("Repository "+getName()+" is offline. Connect first.");
		// }
		if (processService == null) {
			try {
				ProcessService_Service serviceService = new ProcessService_Service(getProcessServiceWSDLUrl(), new QName("http://service.web.rapidanalytics.de/", "ProcessService"));
				processService = serviceService.getProcessServicePort();
				
				setCredentials((BindingProvider)processService);

				setOffline(false);
			} catch (Exception e) {
				setOffline(true);
				setPassword(null);
				processService = null;
				throw new RepositoryException("Cannot connect to " + getBaseUrl() + ": " + e, e);
			}
		}
		return processService;
	}

	@Override
	public String getDescription() {
		return "Remote repository at " + getBaseUrl();
	}

	@Override
	public void refresh() throws RepositoryException {
		setOffline(false);
		cachedEntries.clear();
		super.refresh();
		removeJDBCConnectionEntries();
		installJDBCConnectionEntries();
	}

	/** Returns a connection to a given location in the repository. 
	 * @param preAuthHeader If set, the Authorization: header will be set to basic auth. Otherwise, the {@link GlobalAuthenticator} mechanism
	 *  will be used. 
	 *  @param type can be null*/
	public HttpURLConnection getResourceHTTPConnection(String location, EntryStreamType type, boolean preAuthHeader) throws IOException {
		String split[] = location.split("/");
		StringBuilder encoded = new StringBuilder();
		encoded.append("RAWS/resources");
		for (String fraction : split) {		
			if (!fraction.isEmpty()) { // only for non empty to prevent double //
				encoded.append('/');
				encoded.append(URLEncoder.encode(fraction, "UTF-8"));
			}
		}
		if (type == EntryStreamType.METADATA) {
			encoded.append("?format=binmeta");
		}
		return getHTTPConnection(encoded.toString(), preAuthHeader);
	}

	public HttpURLConnection getHTTPConnection(String pathInfo, boolean preAuthHeader) throws IOException {
		final HttpURLConnection conn = (HttpURLConnection) new URL(getBaseUrl(), pathInfo).openConnection();
		conn.setRequestProperty("Accept-Charset", "UTF-8"); 
		if (preAuthHeader && (username != null) && (password != null)) {
			String userpass = username + ":" + new String(password);
			String basicAuth = "Basic " + new String(Base64.encodeBytes(userpass.getBytes()));
			conn.setRequestProperty ("Authorization", basicAuth);
		}		
		return conn;
	}

	@Override
	public Element createXML(Document doc) {
		Element repositoryElement = doc.createElement("remoteRepository");

		Element url = doc.createElement("url");
		url.appendChild(doc.createTextNode(this.getBaseUrl().toString()));
		repositoryElement.appendChild(url);

		Element alias = doc.createElement("alias");
		alias.appendChild(doc.createTextNode(this.getAlias()));
		repositoryElement.appendChild(alias);

		Element user = doc.createElement("user");
		user.appendChild(doc.createTextNode(this.getUsername()));
		repositoryElement.appendChild(user);

		return repositoryElement;
	}

	public static RemoteRepository fromXML(Element element) throws XMLException {
		String url = XMLTools.getTagContents(element, "url", true);
		try {
			return new RemoteRepository(new URL(url), XMLTools.getTagContents(element, "alias", true), XMLTools.getTagContents(element, "user", true), null, false);
		} catch (MalformedURLException e) {
			throw new XMLException("Illegal url '" + url + "': " + e, e);
		}
	}

	@Override
	public void delete() {
		RepositoryManager.getInstance(null).removeRepository(this);
	}

	public static List<RemoteRepository> getAll() {
		List<RemoteRepository> result = new LinkedList<RemoteRepository>();
		for (WeakReference<RemoteRepository> ref : ALL_REPOSITORIES.values()) {
			RemoteRepository rep = ref.get();
			if (ref != null) {
				result.add(rep);
			}
		}
		return result;
	}

	public boolean isConnected() {
		return !isOffline();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
		int uriModificator = 0;
		try {
			uriModificator = (getBaseUrl() == null) ? 0 : getBaseUrl().toURI().hashCode();
		} catch (URISyntaxException e) {
		}
		result = prime * result + uriModificator;
		result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteRepository other = (RemoteRepository) obj;
		if (getAlias() == null) {
			if (other.getAlias() != null)
				return false;
		} else if (!getAlias().equals(other.getAlias()))
			return false;
		if (getBaseUrl() == null) {
			if (other.getBaseUrl() != null)
				return false;
		} else
			try {
				if (!getBaseUrl().toURI().equals(other.getBaseUrl().toURI()))
					return false;
			} catch (URISyntaxException e) {
				// this cannot happen, since we already had have a valid URL: no possible problem when converting to uri
				return false;
			}
		if (getUsername() == null) {
			if (other.getUsername() != null)
				return false;
		} else if (!getUsername().equals(other.getUsername()))
			return false;
		return true;
	}

	/** Returns the URI to which a browser can be pointed to browse a given entry. */
	public URI getURIForResource(String path) {
		try {
			return getBaseUrl().toURI().resolve("/RA/faces/restricted/browse.xhtml?location=" + URLEncoder.encode(path, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/** Returns the URI to which a browser can be pointed to access the RA web interface. */
	private URI getURIWebInterfaceURI() {
		try {
			return getBaseUrl().toURI().resolve("RA/faces/restricted/index.xhtml");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void browse(String location) {
		try {
			Desktop.getDesktop().browse(getURIForResource(location));
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("cannot_open_browser", e);
		}
	}

	public void showLog(int id) {
		try {
			Desktop.getDesktop().browse(getProcessLogURI(id));
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("cannot_open_browser", e);
		}
	}

	public URI getProcessLogURI(int id) {
		try {
			return getBaseUrl().toURI().resolve("/RA/processlog?id=" + id);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<Action> getCustomActions() {
		Collection<Action> actions = super.getCustomActions();
		actions.add(new BrowseAction("remoterepository.administer", getRepository().getURIWebInterfaceURI()));
		return actions;
	}

	@Override
	public boolean shouldSave() {
		return !isHome;
	}

	// JDBC entries provided by server

	private Collection<FieldConnectionEntry> fetchJDBCEntries() throws XMLException, CipherException, SAXException, IOException {
		URL xmlURL = new URL(getBaseUrl(), "RAWS/jdbc_connections.xml");
		Document doc = XMLTools.parse(xmlURL.openStream());
		final Collection<FieldConnectionEntry> result = DatabaseConnectionService.parseEntries(doc.getDocumentElement());
		for (FieldConnectionEntry entry : result) {
			entry.setRepository(getAlias());
		}
		return result;
	}

	@Override
	public void postInstall() {
	}

	private void installJDBCConnectionEntries() {
		if (this.connectionEntries != null) {
			return;
		}
		try {
			this.connectionEntries = fetchJDBCEntries();
			for (FieldConnectionEntry entry : connectionEntries) {
				DatabaseConnectionService.addConnectionEntry(entry);
			}
			//LogService.getRoot().config("Added " + connectionEntries.size() + " jdbc connections exported by " + getName() + ".");
			LogService.getRoot().log(Level.CONFIG, "com.rapidminer.repository.remote.RemoteRepository.added_jdbc_connections_exported_by", 
					new Object[] {connectionEntries.size(), getName()});

		} catch (Exception e) {
			//LogService.getRoot().log(Level.WARNING, "Failed to fetch JDBC connection entries from server " + getName() + ".", e);
			LogService.getRoot().log(Level.WARNING,
					I18N.getMessage(LogService.getRoot().getResourceBundle(), 
					"com.rapidminer.repository.remote.RemoteRepository.fetching_jdbc_connections_entries_error", 
					getName()),
					e);		
		}
	}

	private void removeJDBCConnectionEntries() {
		if (this.connectionEntries != null) {
			for (FieldConnectionEntry entry : connectionEntries) {
				DatabaseConnectionService.deleteConnectionEntry(entry);
			}
			this.connectionEntries = null;
		}
	}

	@Override
	public void preRemove() {
	}

	public URL getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(URL url) {
		baseUrl = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}
	
	
	@Override
	public boolean isConfigurable() {
		return true;
	}

	@Override
	public RepositoryConfigurationPanel makeConfigurationPanel() {
		return new RemoteRepositoryPanel();
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	private boolean isOffline() {
		return offline;
	}
	
	/** If value changes, notifies {@link #connectionListeners}. */
	private void setOffline(boolean offline) {
		if (offline == this.offline) {
			return;
		}
		this.offline = offline;
		for (ConnectionListener l : connectionListeners) {
			if (isConnected()) {
				l.connectionEstablished(this);
			} else {
				l.connectionLost(this);
			}
		}
	}
	
	private List<ConnectionListener> connectionListeners = new LinkedList<ConnectionListener>();
	public void addConnectionListener(ConnectionListener l) {
		connectionListeners.add(l);
	}
	public void removeConnectionListener(ConnectionListener l) {
		connectionListeners.remove(l);
	}
}
