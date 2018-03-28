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
package com.rapidminer.repository.gui.process;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.datatype.XMLGregorianCalendar;

import com.rapid_i.repository.wsimport.ProcessResponse;
import com.rapid_i.repository.wsimport.ProcessStackTrace;
import com.rapid_i.repository.wsimport.ProcessStackTraceElement;
import com.rapidminer.io.process.XMLTools;
import com.rapidminer.repository.RemoteProcessState;
import com.rapidminer.repository.remote.RemoteRepository;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;

/**
 * The TreeModel for the log of remotely executed processes.
 * 
 * @author Simon Fischer
 */
public class RemoteProcessesTreeModel implements TreeModel {

	private static final long serialVersionUID = 1L;

	private static final long UPDATE_PERIOD = 2500;

	private class ProcessList {
		private List<Integer> knownIds = new LinkedList<Integer>();
		private Map<Integer,ProcessResponse> processResponses = new HashMap<Integer,ProcessResponse>();
		public int add(ProcessResponse pr) {
			int newIndex = -1;
			if (!processResponses.containsKey(pr.getId())) {
				newIndex = knownIds.size();
				knownIds.add(pr.getId());				
			}
			processResponses.put(pr.getId(), pr);
			return newIndex;
		}		
		public ProcessResponse getByIndex(int index) {
			return processResponses.get(knownIds.get(index));
		}
		public ProcessResponse getById(int id) {
			return processResponses.get(id);
		}
		public int size() {
			return knownIds.size();
		}
		public int indexOf(ProcessResponse child) {
			int index = 0;
			for (Integer id : knownIds) {
				ProcessResponse pr = processResponses.get(id);
				if ((pr != null) && (pr.getId() == child.getId())) {
					return index;
				}
				index++;				
			}
			return -1;
		}
		private TreeModelEvent trim(Set<Integer> processIds, RemoteRepository repos) {			
			List<Integer> removedIndices = new LinkedList<Integer>();
			List<Object> removedObjects = new LinkedList<Object>();
			Iterator<Integer> i = knownIds.iterator();
			int index = 0;
			while (i.hasNext()) {
				Integer id = i.next();
				if (!processIds.contains(id)) {
					i.remove();
					ProcessResponse process = processResponses.remove(id);					
					removedIndices.add(index);
					removedObjects.add(process);
				} 
				index++;				
			}
			if (!removedIndices.isEmpty()) {
				int[] indices = new int[removedIndices.size()];
				for (int j = 0; j < removedIndices.size(); j++) {
					indices[j] = removedIndices.get(j);
				}
				return new TreeModelEvent(this, new Object[] { root, repos }, indices, removedObjects.toArray());
			} else {
				return null;
			}
		}
	}

	private final class UpdateTask extends TimerTask {
		@Override
		public void run() {
			final List<RemoteRepository> newRepositories = RemoteRepository.getAll();
			if (!newRepositories.equals(repositories)) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							repositories = newRepositories;
							processes.clear();
							for (RemoteRepository repos : repositories) {
								processes.put(repos, new ProcessList());
							}
							fireStructureChanged(new TreeModelEvent(this, new Object[] { root }));
						};	
					});
				} catch (InterruptedException e) {
					LogService.getRoot().log(Level.WARNING, e.toString(), e);
				} catch (InvocationTargetException e) {
					LogService.getRoot().log(Level.WARNING, e.toString(), e);
				}					
			}

			for (final RemoteRepository repos : repositories) {
				if (observedRepositories.contains(repos)) {
					final ProcessList processList = processes.get(repos);
					try {
						final Collection<Integer> processIds = repos.getProcessService().getRunningProcesses(since);
						// First, delete removed ids
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								TreeModelEvent deleteEvent = processList.trim(new HashSet<Integer>(processIds), repos);
								//processes.put(repos, processList);
								if (deleteEvent != null) {
									fireDelete(deleteEvent);									
								}									
							}
						});

						for (Integer processId : processIds) {
							ProcessResponse oldProcess = processList.getById(processId);
							// we update if we don't know the id yet or if the process is not complete						
							if (oldProcess == null) {
								final ProcessResponse newResponse = repos.getProcessService().getRunningProcessesInfo(processId);									
								SwingUtilities.invokeAndWait(new Runnable() {
									@Override
									public void run() {
										int newIndex = processList.add(newResponse);
										fireAdd(new TreeModelEvent(this, new Object[] {root, repos}, 
												new int[] {newIndex}, 
												new Object[] {newResponse}));							

									}									
								});
							} else if (!RemoteProcessState.valueOf(oldProcess.getState()).isTerminated()) {								
								final ProcessResponse updatedResponse = repos.getProcessService().getRunningProcessesInfo(processId);							
								SwingUtilities.invokeAndWait(new Runnable() {
									@Override
									public void run() {
										processList.add(updatedResponse);
										fireStructureChanged(new TreeModelEvent(this, new Object[] {root, repos, updatedResponse}));							
									}
								});
							} else {								
								// If process is terminated, there is not need to update.
								// The process is already in the list since it is copied
							}						
						}							
					} catch (Exception ex) {
						//LogService.getRoot().log(Level.WARNING, "Error fetching remote process list: "+ex, ex);
						LogService.getRoot().log(Level.WARNING,
								I18N.getMessage(LogService.getRoot().getResourceBundle(), 
								"com.rapidminer.repository.gui.process.RemoteProcessesTreeModel.fetching_remote_process_list_error", 
								ex),
								ex);
						fireStructureChanged(new TreeModelEvent(this, new TreePath(new Object[] { root, repos })));
					}
				}
			}	
		}
	}

	private Map<RemoteRepository, ProcessList> processes = new HashMap<RemoteRepository, ProcessList>(); 
	private List<RemoteRepository> repositories = new LinkedList<RemoteRepository>();
	private Set<RemoteRepository> observedRepositories = new HashSet<RemoteRepository>();

	private Object root = new Object();

	private Timer updateTimer = new Timer("RemoteProcess-Updater", true);

	private XMLGregorianCalendar since;

	public RemoteProcessesTreeModel() {
		updateTimer.schedule(new UpdateTask(), UPDATE_PERIOD, UPDATE_PERIOD);
	}

	private EventListenerList listeners = new EventListenerList();

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(TreeModelListener.class, l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(TreeModelListener.class, l);		
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == root) {
			return repositories.get(index);			
		} else if (parent instanceof RemoteRepository) {
			return processes.get(parent).getByIndex(index);
		} else if (parent instanceof ProcessResponse) {
			ProcessResponse proResponse = (ProcessResponse)parent;
			if (proResponse.getException() != null) {
				if (index == 0) {
					return new ExceptionWrapper(proResponse.getException());
				} else {
					return null;
				}
			} else {
				ProcessStackTrace trace = proResponse.getTrace();
				int elementsSize = 0;
				if ((trace != null) && (trace.getElements() != null)) {
					elementsSize = trace.getElements().size();
				}
				if (index < elementsSize) {
					return trace.getElements().get(index);
				} else {
					return new OutputLocation(proResponse.getOutputLocations().get(index - elementsSize));
				}
			}
		} else {
			return null;
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == root) {
			return repositories.size();
		}  else if (parent instanceof RemoteRepository) {
			ProcessList list = processes.get(parent);
			if (list == null) {
				return 0;
			} else {
				return list.size();
			}
		} else if (parent instanceof ProcessResponse) {
			ProcessResponse proResponse = (ProcessResponse)parent;
			if (proResponse.getException() != null) {
				return 1;
			} else {
				int size = 0;
				ProcessStackTrace trace = proResponse.getTrace();
				if ((trace != null) && (trace.getElements() != null)) {
					size += trace.getElements().size();
				} 
				if (proResponse.getOutputLocations() != null) {
					size += proResponse.getOutputLocations().size();
				} 
				return size;
			}
		} else {
			return 0;
		}
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == root) {
			return repositories.indexOf(child);
		} else if (parent instanceof RemoteRepository) {
			return processes.get(parent).indexOf((ProcessResponse) child);
		} else if (parent instanceof ProcessResponse) {
			ProcessResponse proResponse = (ProcessResponse)parent;
			if (child instanceof ProcessStackTraceElement) {
				ProcessStackTrace trace = proResponse.getTrace();
				if ((trace != null) && (trace.getElements() != null)) {
					return trace.getElements().indexOf(child);
				} else {
					return -1;
				}
			} else if (child instanceof OutputLocation) {
				if (proResponse.getOutputLocations() != null) {
					return proResponse.getOutputLocations().indexOf(((OutputLocation)child).getLocation());
				} else {
					return -1;
				}
			} else if (child instanceof ExceptionWrapper) {
				return 0;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object node) {
		return (node != root) && !(node instanceof ProcessResponse) && !(node instanceof RemoteRepository);
	}


	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// not editable		
	}


	private void fireAdd(TreeModelEvent e) {
		for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
			l.treeNodesInserted(e);
		}
	}					

//	private void fireUpdate(TreeModelEvent e) {
//		for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
//			l.treeNodesChanged(e);
//		}
//	}

	private void fireDelete(TreeModelEvent event) {
		for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
			l.treeNodesRemoved(event);
		}		
	}


	private void fireStructureChanged(TreeModelEvent e) {
		for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
			l.treeStructureChanged(e);
		}	
	}

	public void setSince(Date since) {
		if (since == null) {
			this.since = null;
		} else {
			this.since = XMLTools.getXMLGregorianCalendar(since);
		}
	}

	public void observe(RemoteRepository rep) {
		observedRepositories.add(rep);
	}
	public void ignore(RemoteRepository rep) {
		observedRepositories.remove(rep);
	}

}
