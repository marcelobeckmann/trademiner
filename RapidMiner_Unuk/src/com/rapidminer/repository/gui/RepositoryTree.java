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
package com.rapidminer.repository.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.rapidminer.RepositoryProcessLocation;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.actions.OpenAction;
import com.rapidminer.gui.dnd.TransferableOperator;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.components.ToolTipWindow;
import com.rapidminer.gui.tools.components.ToolTipWindow.TipProvider;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.DataImportWizard;
import com.rapidminer.repository.DataEntry;
import com.rapidminer.repository.Entry;
import com.rapidminer.repository.Folder;
import com.rapidminer.repository.ProcessEntry;
import com.rapidminer.repository.RepositoryActionCondition;
import com.rapidminer.repository.RepositoryActionConditionImplConfigRepository;
import com.rapidminer.repository.RepositoryActionConditionImplStandard;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.gui.actions.AbstractRepositoryAction;
import com.rapidminer.repository.gui.actions.ConfigureRepositoryAction;
import com.rapidminer.repository.gui.actions.CopyEntryRepositoryAction;
import com.rapidminer.repository.gui.actions.CopyLocationAction;
import com.rapidminer.repository.gui.actions.CreateFolderAction;
import com.rapidminer.repository.gui.actions.DeleteAction;
import com.rapidminer.repository.gui.actions.OpenEntryAction;
import com.rapidminer.repository.gui.actions.PasteEntryRepositoryAction;
import com.rapidminer.repository.gui.actions.RefreshAction;
import com.rapidminer.repository.gui.actions.RenameAction;
import com.rapidminer.repository.gui.actions.StoreProcessAction;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;


/**
 * A tree displaying repository contents.
 * <p>
 * To add new actions to the popup menu, call {@link #addRepositoryAction(Class, RepositoryActionCondition, Class, boolean, boolean)} or {@link #addRepositoryAction(Class, RepositoryActionCondition, boolean, boolean)}.
 * Be sure to follow its instructions carefully.
 *
 * @author Simon Fischer, Tobias Malbrecht, Marco Boeck
 */
public class RepositoryTree extends JTree {

    /**
     * Holds the RepositoryAction entries.
     *
     */
    private static class RepositoryActionEntry {

        private Class<? extends AbstractRepositoryAction> actionClass;

        private RepositoryActionCondition condition;

        private boolean hasSeparatorBefore;

        private boolean hasSeparatorAfter;


        public RepositoryActionEntry(Class<? extends AbstractRepositoryAction> actionClass, RepositoryActionCondition condition, boolean hasSeparatorBefore, boolean hasSeparatorAfter) {
            this.actionClass = actionClass;
            this.condition = condition;
            this.hasSeparatorAfter = hasSeparatorAfter;
            this.hasSeparatorBefore = hasSeparatorBefore;
        }

        public boolean hasSeperatorBefore() {
            return hasSeparatorBefore;
        }

        public boolean hasSeperatorAfter() {
            return hasSeparatorAfter;
        }

        public RepositoryActionCondition getRepositoryActionCondition() {
            return condition;
        }

        public Class<? extends AbstractRepositoryAction> getRepositoryActionClass() {
            return actionClass;
        }
    }

    public final AbstractRepositoryAction<Entry> RENAME_ACTION = new RenameAction(this);

    public final AbstractRepositoryAction<Entry> DELETE_ACTION = new DeleteAction(this);

    public final AbstractRepositoryAction<DataEntry> OPEN_ACTION = new OpenEntryAction(this);

    public final AbstractRepositoryAction<Folder> REFRESH_ACTION = new RefreshAction(this);

    public final AbstractRepositoryAction<Folder> CREATE_FOLDER_ACTION = new CreateFolderAction(this);

    private List<AbstractRepositoryAction> listToEnable = new LinkedList<AbstractRepositoryAction>();

    private EventListenerList listenerList = new EventListenerList();


    private static final long serialVersionUID = -6613576606220873341L;

    private static final List<RepositoryActionEntry> REPOSITORY_ACTIONS = new LinkedList<RepositoryTree.RepositoryActionEntry>();

    static {
        addRepositoryAction(ConfigureRepositoryAction.class, new RepositoryActionConditionImplConfigRepository(), false, true);
        addRepositoryAction(OpenEntryAction.class, new RepositoryActionConditionImplStandard(new Class<?>[]{ DataEntry.class }, new Class<?>[]{}), false, false);
        addRepositoryAction(StoreProcessAction.class, new RepositoryActionConditionImplStandard(new Class<?>[]{ ProcessEntry.class, Folder.class }, new Class<?>[]{}), false, false);
        addRepositoryAction(RenameAction.class, new RepositoryActionConditionImplStandard(new Class<?>[]{ Entry.class }, new Class<?>[]{}), false, false);
        addRepositoryAction(CreateFolderAction.class, new RepositoryActionConditionImplStandard(new Class<?>[]{ Folder.class }, new Class<?>[]{}), false, false);
        addRepositoryAction(CopyEntryRepositoryAction.class, new RepositoryActionConditionImplStandard(new Class<?>[]{}, new Class<?>[]{}), true, false);
        addRepositoryAction(PasteEntryRepositoryAction.class, new RepositoryActionConditionImplStandard(new Class<?>[]{}, new Class<?>[]{}), false, false);
        addRepositoryAction(CopyLocationAction.class, new RepositoryActionConditionImplStandard(new Class<?>[]{}, new Class<?>[]{}), false, false);
        addRepositoryAction(DeleteAction.class, new RepositoryActionConditionImplStandard(new Class<?>[]{ Entry.class }, new Class<?>[]{}), false, false);
        addRepositoryAction(RefreshAction.class, new RepositoryActionConditionImplStandard(new Class<?>[]{ Folder.class }, new Class<?>[]{}), true, false);
    }


    public RepositoryTree() {
        this(null);
    }


    public RepositoryTree(Dialog owner) {
        this(owner, false);
    }

    public RepositoryTree(Dialog owner, boolean onlyFolders) {
        super(new RepositoryTreeModel(RepositoryManager.getInstance(null), onlyFolders));

        ((RepositoryTreeModel) getModel()).setParentTree(this);

        // these actions are a) needed for the action map or b) needed by other classes for toolbars etc
        listToEnable.add(DELETE_ACTION);
        listToEnable.add(RENAME_ACTION);
        listToEnable.add(REFRESH_ACTION);
        listToEnable.add(OPEN_ACTION);
        listToEnable.add(CREATE_FOLDER_ACTION);

        RENAME_ACTION.addToActionMap(this, WHEN_FOCUSED);
        DELETE_ACTION.addToActionMap(this, WHEN_FOCUSED);
        REFRESH_ACTION.addToActionMap(this, WHEN_FOCUSED);

        setRootVisible(false);
        setShowsRootHandles(true);
        setCellRenderer(new RepositoryTreeCellRenderer());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton()== MouseEvent.BUTTON3){
                    int row = getRowForLocation(e.getX(),e.getY());
                    setSelectionInterval(row, row);
                    if (e.isPopupTrigger()) {
                        showPopup(e);
                    }
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton()== MouseEvent.BUTTON3){
                    int row = getRowForLocation(e.getX(),e.getY());
                    setSelectionInterval(row, row);
                    if (e.isPopupTrigger()) {
                        showPopup(e);
                    }
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton()== MouseEvent.BUTTON3){
                    int row = getRowForLocation(e.getX(),e.getY());
                    setSelectionInterval(row, row);
                    if (e.isPopupTrigger()) {
                        showPopup(e);
                    }
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = getSelectionPath();
                    if (path == null) {
                        return;
                    }
                    fireLocationSelected((Entry) path.getLastPathComponent());
                }
            }
        });

        addKeyListener(new KeyListener() {
            // status variable to fix bug 987
            private int lastPressedKey;

            @Override
            public void keyTyped(KeyEvent e) {
            }

            /**
             * Opens entries on enter pressed; collapses/expands folders
             */
            @Override
            public void keyReleased(KeyEvent e) {
                if (lastPressedKey != e.getKeyCode()) {
                    e.consume();
                    return;
                }
                lastPressedKey = 0;

                if (e.getModifiers() == 0) {
                    switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        TreePath path = getSelectionPath();
                        if (path == null) {
                            return;
                        }
                        Entry entry = (Entry)path.getLastPathComponent();
                        if (entry instanceof Folder) {
                            if (isExpanded(path)) {
                                collapsePath(path);
                            } else {
                                expandPath(path);
                            }
                        } else {
                            fireLocationSelected((Entry) path.getLastPathComponent());
                        }
                        e.consume();
                        break;
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                lastPressedKey = e.getKeyCode();
            }
        });

        setDragEnabled(true);
        setTransferHandler(new TransferHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
                List<DataFlavor> flavors = Arrays.asList(transferFlavors);
                boolean contains = flavors.contains(DataFlavor.javaFileListFlavor);
                contains |= flavors.contains(TransferableOperator.LOCAL_TRANSFERRED_REPOSITORY_LOCATION_FLAVOR);
                return contains;
            }

            /** Imports data files using a Wizard. */
            @Override
            public boolean importData(final TransferSupport ts) {
                // determine where to insert
                final Entry droppedOn;
                if (ts.isDrop()) {
                    Point dropPoint = ts.getDropLocation().getDropPoint();
                    TreePath path = getPathForLocation((int)dropPoint.getX(), (int)dropPoint.getY());
                    if (path == null) {
                        return false;
                    }
                    droppedOn = (Entry) path.getLastPathComponent();
                } else {
                    droppedOn = getSelectedEntry();
                }
                if (droppedOn == null) {
                    return false;
                }

                try {
                    List<DataFlavor> flavors = Arrays.asList(ts.getDataFlavors());
                    if (flavors.contains(TransferableOperator.LOCAL_TRANSFERRED_REPOSITORY_LOCATION_FLAVOR)) {
                        final RepositoryLocation loc = (RepositoryLocation) ts.getTransferable().getTransferData(TransferableOperator.LOCAL_TRANSFERRED_REPOSITORY_LOCATION_FLAVOR);
                        if (droppedOn instanceof Folder) {
                            new ProgressThread("copy_repository_entry", true) {
                                @Override
                                public void run() {
                                    try {
                                        if (ts.isDrop() && ts.getDropAction() == MOVE) {
                                            RepositoryManager.getInstance(null).move(loc, (Folder)droppedOn, getProgressListener());
                                        } else {
                                            RepositoryManager.getInstance(null).copy(loc, (Folder)droppedOn, getProgressListener());
                                        }
                                    } catch (RepositoryException e) {
                                        SwingTools.showSimpleErrorMessage("error_in_copy_repository_entry", e, loc.toString(), e.getMessage());
                                    }
                                }
                            }.start();
                            return true;
                        } else {
                            return false;
                        }
                    } else if (flavors.contains(DataFlavor.javaFileListFlavor)) {
                        List files = (List)ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        File file = (File) files.get(0);
                        DataImportWizard.importData(file, droppedOn.getLocation());
                        return true;
                    } else {
                        return false;
                    }
                } catch (UnsupportedFlavorException e) {
                    //LogService.getRoot().log(Level.WARNING, "Cannot accept drop flavor: "+e, e);
                    LogService.getRoot().log(Level.WARNING,
                            I18N.getMessage(LogService.getRoot().getResourceBundle(),
                                    "com.rapidminer.repository.RepositoryTree.accepting_flavor_error",
                                    e),
                                    e);
                    return false;
                } catch (IOException e) {
                    //LogService.getRoot().log(Level.WARNING, "Error during drop: "+e, e);
                    LogService.getRoot().log(Level.WARNING,
                            I18N.getMessage(LogService.getRoot().getResourceBundle(),
                                    "com.rapidminer.repository.RepositoryTree.error_during_drop",
                                    e),
                                    e);
                    return false;
                }
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }
            @Override
            protected Transferable createTransferable(JComponent c) {
                TreePath path = getSelectionPath();
                if (path != null) {
                    Entry e = (Entry)path.getLastPathComponent();
                    final RepositoryLocation location = e.getLocation();
                    return new Transferable() {
                        @Override
                        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                            if (flavor.equals(DataFlavor.stringFlavor)) {
                                return location.getAbsoluteLocation();
                            } else if (TransferableOperator.LOCAL_TRANSFERRED_REPOSITORY_LOCATION_FLAVOR.equals(flavor)) {
                                return location;
                            } else {
                                throw new IllegalArgumentException("Flavor not supported: "+flavor);
                            }
                        }
                        @Override
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[] {
                                    TransferableOperator.LOCAL_TRANSFERRED_REPOSITORY_LOCATION_FLAVOR,
                                    DataFlavor.stringFlavor
                            };
                        }
                        @Override
                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                            return TransferableOperator.LOCAL_TRANSFERRED_REPOSITORY_LOCATION_FLAVOR.equals(flavor) ||
                            DataFlavor.stringFlavor.equals(flavor);
                        }
                    };
                } else {
                    return null;
                }
            }
        });

        //        setTransferHandler(new OperatorTransferHandler() {
        //			private static final long serialVersionUID = 1L;
        //			@Override
        //			protected List<Operator> getDraggedOperators() {
        //				TreePath path = getSelectionPath();
        //				if (path != null) {
        //					Entry e = (Entry)path.getLastPathComponent();
        //					if (e instanceof IOObjectEntry) {
        //						RepositorySource source;
        //						try {
        //							source = OperatorService.createOperator(RepositorySource.class);
        //							source.setParameter(RepositorySource.PARAMETER_REPOSITORY_ENTRY, e.getLocation().getAbsoluteLocation());
        //							return Collections.<Operator>singletonList(source);
        //						} catch (OperatorCreationException e1) {
        //							LogService.getRoot().log(Level.WARNING, "Cannot create RepositorySource: "+e, e);
        //							return null;
        //						}
        //					} else {
        //						return null;
        //					}
        //				} else {
        //					return null;
        //				}
        //			}
        //        });

        getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                enableActions();
            }
        });

        addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                // select the last expanded/collapsed path
                selectionModel.setSelectionPath(event.getPath());
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                // select the last expanded/collapsed path
                treeExpanded(event);
            }
        });

        enableActions();

        new ToolTipWindow(owner, new TipProvider() {
            @Override
            public String getTip(Object o) {
                if (o instanceof Entry) {
                    return ToolTipProviderHelper.getTip((Entry) o);
                } else {
                    return null;
                }
            }

            @Override
            public Object getIdUnder(Point point) {
                TreePath path = getPathForLocation((int)point.getX(), (int)point.getY());
                if (path != null) {
                    return path.getLastPathComponent();
                } else {
                    return null;
                }
            }

            @Override
            public Component getCustomComponent(Object o) {
                if (o instanceof Entry) {
                    return ToolTipProviderHelper.getCustomComponent((Entry) o);
                } else {
                    return null;
                }
            }
        }, this);
    }

    public void enableActions() {
        for (AbstractRepositoryAction action : listToEnable) {
            action.enable();
        }
    }

    public void addRepositorySelectionListener(RepositorySelectionListener listener) {
        listenerList.add(RepositorySelectionListener.class, listener);
    }

    public void removeRepositorySelectionListener(RepositorySelectionListener listener) {
        listenerList.remove(RepositorySelectionListener.class, listener);
    }

    private void fireLocationSelected(Entry entry) {
        RepositorySelectionEvent event = null;
        for (RepositorySelectionListener l : listenerList.getListeners(RepositorySelectionListener.class)) {
            if (event == null) {
                event = new RepositorySelectionEvent(entry);
            }
            l.repositoryLocationSelected(event);
        }
    }

    /** Selects as much as possible of the selected path to the given location.
     *  Returns true if the given location references a folder. */
    boolean expandIfExists(RepositoryLocation relativeTo, String location) {
        RepositoryLocation loc;
        boolean full = true;
        try {
            loc = new RepositoryLocation(relativeTo, location);
        } catch (Exception e) {
            // do nothing
            return false;
        }
        Entry entry = null;
        while (true) {
            try {
                entry = loc.locateEntry();
                if (entry != null) {
                    break;
                }
            } catch (RepositoryException e) {
                return false;
            }
            loc = loc.parent();
            if (loc == null) {
                return false;
            }
            full = false;
        }
        if (entry != null) {
            RepositoryTreeModel model = (RepositoryTreeModel) getModel();
            TreePath pathTo = model.getPathTo(entry);
            expandPath(pathTo);
            setSelectionPath(pathTo);
            if (entry instanceof Folder) {
                return full;
            }
        }
        return false;
        //loc = loc.parent();
    }

    private void showPopup(MouseEvent e) {
        TreePath path = getSelectionPath();
        if (path == null) {
            return;
        }
        Object selected = path.getLastPathComponent();
        JPopupMenu menu = new JPopupMenu();

        // can support multiple selections, not needed right now
        List<Entry> entryList = new ArrayList<Entry>(1);
        if (selected instanceof Entry) {
            entryList.add((Entry)selected);
        }
        List<Action> actionList = createContextMenuActions(this, entryList);
        // go through ordered list of actions and add them
        for (Action action : actionList) {
            if (action == null) {
                menu.addSeparator();
            } else {
                menu.add(action);
            }
        }

        // append custom actions if there are any
        if (selected instanceof Entry) {
            Collection<Action> customActions = ((Entry) selected).getCustomActions();
            if (customActions != null && !customActions.isEmpty()) {
                menu.addSeparator();
                for (Action a : customActions) {
                    menu.add(a);
                }
            }
        }

        menu.show(this, e.getX(), e.getY());
    }

    /** Opens the process held by the given entry (in the background) and opens it. */
    public static void openProcess(final ProcessEntry processEntry) {
        ProgressThread openProgressThread = new ProgressThread("open_process") {
            @Override
            public void run() {
                RepositoryProcessLocation processLocation = new RepositoryProcessLocation(processEntry.getLocation());
                if (RapidMinerGUI.getMainFrame().close()){
                    OpenAction.open(processLocation, true);
                }
                /* PRE FIX OF BUG 308: When opening process with double click all changes are discarded
                 * 
                 * try {
					RepositoryProcessLocation processLocation = new RepositoryProcessLocation(processEntry.getLocation());
					String xml = processEntry.retrieveXML();
					try {
						final Process process = new Process(xml);
						process.setProcessLocation(processLocation);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								RapidMinerGUI.getMainFrame().setOpenedProcess(process, true, processEntry.getLocation().toString());
							}
						});
					} catch (Exception e) {
						RapidMinerGUI.getMainFrame().handleBrokenProxessXML(processLocation, xml, e);
					}
				} catch (Exception e1) {
					SwingTools.showSimpleErrorMessage("cannot_fetch_data_from_repository", e1);
				}*/

            }
        };
        openProgressThread.start();
    }

    public Entry getSelectedEntry() {
        TreePath path = getSelectionPath();
        if (path == null) {
            return null;
        }
        Object selected = path.getLastPathComponent();
        if (selected instanceof Entry) {
            return (Entry) selected;
        } else {
            return null;
        }
    }

    public Collection<AbstractRepositoryAction<?>> getAllActions() {
        List<AbstractRepositoryAction<?>> listOfAbstractRepositoryActions = new LinkedList<AbstractRepositoryAction<?>>();
        for (Action action : createContextMenuActions(this, new LinkedList<Entry>())) {
            if (action instanceof AbstractRepositoryAction<?>) {
                listOfAbstractRepositoryActions.add((AbstractRepositoryAction<?>)action);
            }
        }
        return listOfAbstractRepositoryActions;
    }


    /**
     * Appends the given {@link AbstractRepositoryAction} extending class to the popup menu actions.
     * <p>The class <b>MUST</b> have one public constructor taking only a RepositoryTree.
     * </br>Example: public MyNewRepoAction(RepositoryTree tree) { ... }
     * </br>Otherwise creating the action via reflection will fail.
     * 
     * @param actionClass the class extending {@link AbstractRepositoryAction}
     * @param condition the {@link RepositoryActionCondition} which determines on which selected entries the action will be visible.
     * @param hasSeparatorBefore if true, a separator will be added before the action
     * @param hasSeparatorAfter if true, a separator will be added after the action
     * @return true if the action was successfully added; false otherwise
     */
    public static void addRepositoryAction(Class<? extends AbstractRepositoryAction> actionClass, RepositoryActionCondition condition, boolean hasSeparatorBefore, boolean hasSeparatorAfter) {
        addRepositoryAction(actionClass, condition, null, hasSeparatorBefore, hasSeparatorAfter);
    }

    /**
     * Adds the given {@link AbstractRepositoryAction} extending class to the popup menu actions at the given index.
     * <p>The class <b>MUST</b> have one public constructor taking only a RepositoryTree.
     * </br>Example: public MyNewRepoAction(RepositoryTree tree) { ... }
     * </br>Otherwise creating the action via reflection will fail.
     * 
     * @param actionClass the class extending {@link AbstractRepositoryAction}
     * @param condition the {@link RepositoryActionCondition} which determines on which selected entries the action will be visible.
     * @param insertAfterThisAction the class of the action after which the new action should be inserted. Set to {@code null} to append the action at the end.
     * @param hasSeparatorBefore if true, a separator will be added before the action
     * @param hasSeparatorAfter if true, a separator will be added after the action
     * @return true if the action was successfully added; false otherwise
     */
    public static void addRepositoryAction(Class<? extends AbstractRepositoryAction> actionClass, RepositoryActionCondition condition, Class<? extends Action> insertAfterThisAction, boolean hasSeparatorBefore, boolean hasSeparatorAfter) {
        if (actionClass == null || condition == null) {
            throw new IllegalArgumentException("actionClass and condition must not be null!");
        }

        RepositoryActionEntry newEntry = new RepositoryActionEntry(actionClass, condition, hasSeparatorBefore, hasSeparatorAfter);
        if (insertAfterThisAction == null) {
            REPOSITORY_ACTIONS.add(newEntry);
        } else {
            // searching for class to insert after
            boolean inserted = false;
            int i = 0;
            for (RepositoryActionEntry entry : REPOSITORY_ACTIONS) {
                Class<? extends Action> existingAction = entry.getRepositoryActionClass();
                if (existingAction.equals(insertAfterThisAction)) {
                    REPOSITORY_ACTIONS.add(i + 1, newEntry);
                    inserted = true;
                    break;
                }
                i++;
            }

            // if reference couldn't be found: just add as last
            if(!inserted)
                REPOSITORY_ACTIONS.add(newEntry);
        }
    }

    /**
     * Removes the given action from the popup menu actions.
     * @param actionClass the class of the {@link AbstractRepositoryAction} to remove
     */
    public static void removeRepositoryAction(Class<? extends AbstractRepositoryAction> actionClass) {
        Iterator<RepositoryActionEntry> iterator = REPOSITORY_ACTIONS.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().getRepositoryActionClass().equals(actionClass)) {
                iterator.remove();
            }
        }
    }

    /**
     * This method returns a list of actions shown in the context menu if the given {@link RepositoryActionCondition} is true.
     * Contains {@code null} elements for each separator.
     * This method is called by each {@link RepositoryTree} instance during construction time and
     * creates instances via reflection of all registered acionts.
     * See {@link #addRepositoryAction(Class, RepositoryActionCondition, Class, boolean, boolean)} to add actions.
     */
    private static List<Action> createContextMenuActions(RepositoryTree repositoryTree, List<Entry> entryList) {
        List<Action> listOfActions = new LinkedList<Action>();
        boolean lastWasSeparator = true;

        for (RepositoryActionEntry entry: REPOSITORY_ACTIONS) {
            try {
                if (entry.getRepositoryActionCondition().evaluateCondition(entryList)) {
                    if (!lastWasSeparator && entry.hasSeperatorBefore()) {
                        // add null element which means a separator will be added in the menu
                        listOfActions.add(null);
                    }
                    Constructor constructor = entry.getRepositoryActionClass().getConstructor(new Class[]{ RepositoryTree.class });
                    AbstractRepositoryAction createdAction = (AbstractRepositoryAction)constructor.newInstance(repositoryTree);
                    createdAction.enable();
                    listOfActions.add(createdAction);
                    if (entry.hasSeperatorAfter()) {
                        listOfActions.add(null);
                        lastWasSeparator = true;
                    } else {
                        lastWasSeparator = false;
                    }
                }
            } catch (Exception e) {
                //LogService.getGlobal().log("could not create repository action: " + entry.getRepositoryActionClass(), LogService.ERROR);
                LogService.getRoot().log(Level.SEVERE, "com.rapidminer.repository.gui.RepositoryTree.creating_repository_action_error", entry.getRepositoryActionClass());
            }
        }
        return listOfActions;
    }
}
