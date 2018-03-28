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
package com.rapidminer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import com.rapid_i.deployment.update.client.ExtensionDialog;
import com.rapid_i.deployment.update.client.UpdateDialog;
import com.rapidminer.BreakpointListener;
import com.rapidminer.Process;
import com.rapidminer.ProcessLocation;
import com.rapidminer.RapidMiner;
import com.rapidminer.gui.actions.AboutAction;
import com.rapidminer.gui.actions.Actions;
import com.rapidminer.gui.actions.AnovaCalculatorAction;
import com.rapidminer.gui.actions.AutoWireAction;
import com.rapidminer.gui.actions.BrowseAction;
import com.rapidminer.gui.actions.CheckForJDBCDriversAction;
import com.rapidminer.gui.actions.ExitAction;
import com.rapidminer.gui.actions.ExportProcessAction;
import com.rapidminer.gui.actions.ExportViewAction;
import com.rapidminer.gui.actions.ImportProcessAction;
import com.rapidminer.gui.actions.ManageBuildingBlocksAction;
import com.rapidminer.gui.actions.ManageTemplatesAction;
import com.rapidminer.gui.actions.NewAction;
import com.rapidminer.gui.actions.NewPerspectiveAction;
import com.rapidminer.gui.actions.OpenAction;
import com.rapidminer.gui.actions.PageSetupAction;
import com.rapidminer.gui.actions.PauseAction;
import com.rapidminer.gui.actions.PrintAction;
import com.rapidminer.gui.actions.PrintPreviewAction;
import com.rapidminer.gui.actions.RedoAction;
import com.rapidminer.gui.actions.RunAction;
import com.rapidminer.gui.actions.RunRemoteAction;
import com.rapidminer.gui.actions.SaveAction;
import com.rapidminer.gui.actions.SaveAsAction;
import com.rapidminer.gui.actions.SaveAsTemplateAction;
import com.rapidminer.gui.actions.SettingsAction;
import com.rapidminer.gui.actions.StopAction;
import com.rapidminer.gui.actions.ToggleAction;
import com.rapidminer.gui.actions.ToggleExpertModeAction;
import com.rapidminer.gui.actions.TutorialAction;
import com.rapidminer.gui.actions.UndoAction;
import com.rapidminer.gui.actions.ValidateAutomaticallyAction;
import com.rapidminer.gui.actions.ValidateProcessAction;
import com.rapidminer.gui.actions.WizardAction;
import com.rapidminer.gui.dialog.ProcessInfoScreen;
import com.rapidminer.gui.dialog.Tutorial;
import com.rapidminer.gui.dialog.UnknownParametersInfoDialog;
import com.rapidminer.gui.docking.RapidDockingToolbar;
import com.rapidminer.gui.flow.ErrorTable;
import com.rapidminer.gui.flow.ProcessPanel;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.operatortree.OperatorTreePanel;
import com.rapidminer.gui.operatortree.actions.CutCopyPasteAction;
import com.rapidminer.gui.operatortree.actions.ToggleBreakpointItem;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.processeditor.CommentEditor;
import com.rapidminer.gui.processeditor.NewOperatorEditor;
import com.rapidminer.gui.processeditor.ProcessContextProcessEditor;
import com.rapidminer.gui.processeditor.ProcessEditor;
import com.rapidminer.gui.processeditor.XMLEditor;
import com.rapidminer.gui.processeditor.results.DockableResultDisplay;
import com.rapidminer.gui.processeditor.results.ResultDisplay;
import com.rapidminer.gui.processeditor.results.ResultDisplayTools;
import com.rapidminer.gui.processeditor.results.TabbedResultDisplay;
import com.rapidminer.gui.properties.OperatorPropertyPanel;
import com.rapidminer.gui.security.PasswordManager;
import com.rapidminer.gui.templates.SaveAsTemplateDialog;
import com.rapidminer.gui.tools.LoggingViewer;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.ResourceMenu;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.SystemMonitor;
import com.rapidminer.gui.tools.WelcomeScreen;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.gui.tools.dialogs.DecisionRememberingConfirmDialog;
import com.rapidminer.gui.tools.dialogs.ManageDatabaseConnectionsDialog;
import com.rapidminer.gui.tools.dialogs.ManageDatabaseDriversDialog;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.BlobImportWizard;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.DatabaseImportWizard;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.access.AccessImportWizard;
import com.rapidminer.operator.DebugMode;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UnknownParameterInformation;
import com.rapidminer.operator.nio.CSVImportWizard;
import com.rapidminer.operator.nio.ExcelImportWizard;
import com.rapidminer.operator.nio.xml.XMLImportWizard;
import com.rapidminer.operator.ports.metadata.CompatibilityLevel;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeColor;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.repository.gui.RepositoryBrowser;
import com.rapidminer.repository.gui.process.RemoteProcessViewer;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Observable;
import com.rapidminer.tools.Observer;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.tools.config.gui.ConfigurationDialog;
import com.rapidminer.tools.plugin.Plugin;
import com.rapidminer.tools.usagestats.UsageStatsTransmissionDialog;
import com.vlsolutions.swing.docking.DockGroup;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingContext;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.toolbars.ToolBarConstraints;
import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.ToolBarPanel;

/**
 * The main component class of the RapidMiner GUI. The class holds a lot of Actions
 * that can be used for the tool bar and for the menu bar. MainFrame has methods
 * for handling the process (saving, opening, creating new). It keeps track
 * of the state of the process and enables/disables buttons. It must be
 * notified whenever the process changes and propagates this event to its
 * children. Most of the code is enclosed within the Actions.
 * 
 * @author Ingo Mierswa, Simon Fischer, Sebastian Land
 */
public class MainFrame extends ApplicationFrame implements WindowListener {

    /** The property name for &quot;The pixel size of each plot in matrix plots.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_MATRIXPLOT_SIZE = "rapidminer.gui.plotter.matrixplot.size";

    /**
     * The property name for &quot;The maximum number of rows used for a plotter, using only a sample of this size if more rows are
     * available.&quot;
     */
    public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_ROWS_MAXIMUM = "rapidminer.gui.plotter.rows.maximum";

    /** The property name for &quot;Limit number of displayed classes plotter legends. -1 for no limit.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_CLASSLIMIT = "rapidminer.gui.plotter.legend.classlimit";

    /** The property name for &quot;The color for minimum values of the plotter legend.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MINCOLOR = "rapidminer.gui.plotter.legend.mincolor";

    /** The property name for &quot;The color for maximum values of the plotter legend.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MAXCOLOR = "rapidminer.gui.plotter.legend.maxcolor";

    /** The property name for &quot;Limit number of displayed classes for colorized plots. -1 for no limit.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_COLORS_CLASSLIMIT = "rapidminer.gui.plotter.colors.classlimit";

    /** The property name for &quot;Maximum number of states in the undo list.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE = "rapidminer.gui.undolist.size";

    /** The property name for &quot;Maximum number of examples to use for the attribute editor. -1 for no limit.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_ATTRIBUTEEDITOR_ROWLIMIT = "rapidminer.gui.attributeeditor.rowlimit";

    /** The property name for &quot;Beep on process success?&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_BEEP_SUCCESS = "rapidminer.gui.beep.success";

    /** The property name for &quot;Beep on error?&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_BEEP_ERROR = "rapidminer.gui.beep.error";

    /** The property name for &quot;Beep when breakpoint reached?&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_BEEP_BREAKPOINT = "rapidminer.gui.beep.breakpoint";

    /** The property name for &quot;Limit number of displayed rows in the message viewer. -1 for no limit.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_ROWLIMIT = "rapidminer.gui.messageviewer.rowlimit";

    /** The property name for &quot;The color for notes in the message viewer.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_NOTES = "rapidminer.gui.messageviewer.highlight.notes";

    /** The property name for &quot;The color for warnings in the message viewer.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_WARNINGS = "rapidminer.gui.messageviewer.highlight.warnings";

    /** The property name for &quot;The color for errors in the message viewer.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_ERRORS = "rapidminer.gui.messageviewer.highlight.errors";

    /** The property name for &quot;The color for the logging service indicator in the message viewer.&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_LOGSERVICE = "rapidminer.gui.messageviewer.highlight.logservice";

    /** The property name for &quot;Shows process info screen after loading?&quot; */
    public static final String PROPERTY_RAPIDMINER_GUI_PROCESSINFO_SHOW = "rapidminer.gui.processinfo.show";

    public static final String PROPERTY_RAPIDMINER_GUI_SAVE_BEFORE_RUN = "rapidminer.gui.save_before_run";

    public static final String PROPERTY_RAPIDMINER_GUI_SAVE_ON_PROCESS_CREATION = "rapidminer.gui.save_on_process_creation";

    /** The property determining whether or not to switch to result view when results are produced. */
    public static final String PROPERTY_RAPIDMINER_GUI_AUTO_SWITCH_TO_RESULTVIEW = "rapidminer.gui.auto_switch_to_resultview";

    /** Determines whether we build a {@link TabbedResultDisplay} or a {@link DockableResultDisplay}. */
    public static final String PROPERTY_RAPIDMINER_GUI_RESULT_DISPLAY_TYPE = "rapidminer.gui.result_display_type";

    /** Log level of the LoggingViewer. */
    public static final String PROPERTY_RAPIDMINER_GUI_LOG_LEVEL = "rapidminer.gui.log_level";

    private static final long serialVersionUID = -1602076945350148969L;

    /**
     * Registers all RapidMiner GUI properties. This must often be done centrally in
     * mainframe to ensure that the properties are set when the GUI is started.
     */
    static {
        ParameterService.registerParameter(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_PLOTTER_MATRIXPLOT_SIZE, "The pixel size of each plot in matrix plots.", 1, Integer.MAX_VALUE, 200));
        ParameterService.registerParameter(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_PLOTTER_ROWS_MAXIMUM, "The maximum number of rows used for a plotter, using only a sample of this size if more rows are available.", 1, Integer.MAX_VALUE, PlotterPanel.DEFAULT_MAX_NUMBER_OF_DATA_POINTS));
        ParameterService.registerParameter(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_CLASSLIMIT, "Limit number of displayed classes plotter legends. -1 for no limit.", -1, Integer.MAX_VALUE, 10));
        ParameterService.registerParameter(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MINCOLOR, "The color for minimum values of the plotter legend.", java.awt.Color.blue));
        ParameterService.registerParameter(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MAXCOLOR, "The color for maximum values of the plotter legend.", java.awt.Color.red));
        ParameterService.registerParameter(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_PLOTTER_COLORS_CLASSLIMIT, "Limit number of displayed classes for colorized plots. -1 for no limit.", -1, Integer.MAX_VALUE, 10));
        ParameterService.registerParameter(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE, "Maximum number of states in the undo list.", 1, Integer.MAX_VALUE, 10));
        ParameterService.registerParameter(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_ATTRIBUTEEDITOR_ROWLIMIT, "Maximum number of examples to use for the attribute editor. -1 for no limit.", -1, Integer.MAX_VALUE, 50));
        ParameterService.registerParameter(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_BEEP_SUCCESS, "Beep on process success?", false));
        ParameterService.registerParameter(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_BEEP_ERROR, "Beep on error?", false));
        ParameterService.registerParameter(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_BEEP_BREAKPOINT, "Beep when breakpoint reached?", false));
        ParameterService.registerParameter(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_ROWLIMIT, "Limit number of displayed rows in the message viewer. -1 for no limit.", -1, Integer.MAX_VALUE, 1000));
        ParameterService.registerParameter(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_NOTES, "The color for notes in the message viewer.", new java.awt.Color(51, 151, 51)));
        ParameterService.registerParameter(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_WARNINGS, "The color for warnings in the message viewer.", new java.awt.Color(51, 51, 255)));
        ParameterService.registerParameter(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_ERRORS, "The color for errors in the message viewer.", new java.awt.Color(255, 51, 204)));
        ParameterService.registerParameter(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_LOGSERVICE, "The color for the logging service indicator in the message viewer.", new java.awt.Color(184, 184, 184)));
        ParameterService.registerParameter(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_PROCESSINFO_SHOW, "Shows process info screen after loading?", true));
        ParameterService.registerParameter(new ParameterTypeCategory(PROPERTY_RAPIDMINER_GUI_SAVE_BEFORE_RUN, "Save process before running process?", DecisionRememberingConfirmDialog.PROPERTY_VALUES, DecisionRememberingConfirmDialog.ASK));
        ParameterService.registerParameter(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_SAVE_ON_PROCESS_CREATION, "Save process when creating them?", false));
        ParameterService.registerParameter(new ParameterTypeCategory(PROPERTY_RAPIDMINER_GUI_AUTO_SWITCH_TO_RESULTVIEW, "Automatically switch to results perspective when results are created?", DecisionRememberingConfirmDialog.PROPERTY_VALUES, DecisionRememberingConfirmDialog.ASK));
        ParameterService.registerParameter(new ParameterTypeCategory(PROPERTY_RAPIDMINER_GUI_RESULT_DISPLAY_TYPE, "Determines the result display style.", ResultDisplayTools.TYPE_NAMES, 0));
        ParameterService.registerParameter(new ParameterTypeCategory(PROPERTY_RAPIDMINER_GUI_LOG_LEVEL, "Minimum level of messages that are logged in the GUIs log view.", LoggingViewer.SELECTABLE_LEVEL_NAMES, LoggingViewer.DEFAULT_LEVEL_INDEX));
    }

    /** The title of the frame. */
    public static final String TITLE = "RapidMiner";

    // --------------------------------------------------------------------------------

    public static final int EDIT_MODE = 0;
    public static final int RESULTS_MODE = 1;
    public static final int WELCOME_MODE = 2;

    // public static final String EDIT_MODE_NAME = "edit";
    // public static final String RESULTS_MODE_NAME = "results";
    // public static final String WELCOME_MODE_NAME = "welcome";

    public final transient Action AUTO_WIRE = new AutoWireAction(this, "wire", CompatibilityLevel.PRE_VERSION_5, false, true);
    public final transient Action AUTO_WIRE_RECURSIVELY = new AutoWireAction(this, "wire_recursive", CompatibilityLevel.PRE_VERSION_5, true, true);
    public final transient Action REWIRE = new AutoWireAction(this, "rewire", CompatibilityLevel.PRE_VERSION_5, false, false);
    public final transient Action REWIRE_RECURSIVELY = new AutoWireAction(this, "rewire_recursive", CompatibilityLevel.PRE_VERSION_5, true, false);

    public final transient Action NEW_ACTION = new NewAction(this);
    public final transient Action OPEN_ACTION = new OpenAction();
    public final transient SaveAction SAVE_ACTION = new SaveAction();
    public final transient Action SAVE_AS_ACTION = new SaveAsAction();
    public final transient Action SAVE_AS_TEMPLATE_ACTION = new SaveAsTemplateAction(this);
    public final transient Action MANAGE_TEMPLATES_ACTION = new ManageTemplatesAction();
    public final transient Action MANAGE_BUILDING_BLOCKS_ACTION = new ManageBuildingBlocksAction(this);
    public final transient Action PRINT_ACTION = new PrintAction(this, "all");
    public final transient Action PRINT_PREVIEW_ACTION = new PrintPreviewAction(this, "all");
    public final transient Action PAGE_SETUP_ACTION = new PageSetupAction();

    public final transient Action IMPORT_CSV_FILE_ACTION = new ResourceAction("import_csv_file") {
        private static final long serialVersionUID = 4632580631996166900L;

        @Override
        public void actionPerformed(ActionEvent e) {
            // CSVImportWizard wizard = new CSVImportWizard("import_csv_file");
            CSVImportWizard wizard;
            try {
                wizard = new CSVImportWizard();
                wizard.setVisible(true);
            } catch (OperatorException e1) {
                // should not happen if operator == null
                throw new RuntimeException("Failed to create wizard.", e1);
            }
        }
    };

    public final transient Action IMPORT_EXCEL_FILE_ACTION = new ResourceAction("import_excel_sheet") {
        private static final long serialVersionUID = 975782163819088729L;

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                ExcelImportWizard wizard = new ExcelImportWizard();
                wizard.setVisible(true);
            } catch (OperatorException e1) {
                // should not happen if operator == null
                throw new RuntimeException("Failed to create wizard.", e1);
            }
        }
    };

    public final transient Action IMPORT_XML_FILE_ACTION = new ResourceAction("import_xml_file") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                XMLImportWizard wizard = new XMLImportWizard();
                wizard.setVisible(true);
            } catch (OperatorException e1) {
                // should not happen if operator == null
                throw new RuntimeException("Failed to create wizard.", e1);
            }
        }
    };

    public final transient Action IMPORT_ACCESS_FILE_ACTION = new ResourceAction("import_access_table") {
        private static final long serialVersionUID = 3725652002686421768L;

        @Override
        public void actionPerformed(ActionEvent e) {
            AccessImportWizard wizard;
            try {
                wizard = new AccessImportWizard("import_access_table");
                wizard.setVisible(true);
            } catch (SQLException e1) {
                SwingTools.showSimpleErrorMessage("db_connection_failed_simple", e1, e1.getMessage());
            }
        }
    };
    public final transient Action IMPORT_DATABASE_TABLE_ACTION = new ResourceAction("import_database_table") {
        private static final long serialVersionUID = 3725652002686421768L;

        @Override
        public void actionPerformed(ActionEvent e) {
            DatabaseImportWizard wizard;
            try {
                wizard = new DatabaseImportWizard("import_database_table");
                wizard.setVisible(true);
            } catch (SQLException e1) {
                SwingTools.showSimpleErrorMessage("db_connection_failed_simple", e1, e1.getMessage());
            }
        }
    };
    public final transient Action IMPORT_PROCESS_ACTION = new ImportProcessAction();
    public final transient Action EXPORT_PROCESS_ACTION = new ExportProcessAction();

    public final transient Action EXPORT_ACTION = new ExportViewAction(this, "all");
    public final transient Action EXIT_ACTION = new ExitAction(this);

    public final transient RunAction RUN_ACTION = new RunAction(this);
    public final transient Action PAUSE_ACTION = new PauseAction(this);
    public final transient Action STOP_ACTION = new StopAction(this);
    public final transient Action RUN_REMOTE_ACTION = new RunRemoteAction();
    public final transient Action VALIDATE_ACTION = new ValidateProcessAction(this);
    public final transient ToggleAction VALIDATE_AUTOMATICALLY_ACTION = new ValidateAutomaticallyAction();
    public final transient Action OPEN_TEMPLATE_ACTION = new WizardAction(this);

    public final transient Action NEW_PERSPECTIVE_ACTION = new NewPerspectiveAction(this);
    public final transient Action SETTINGS_ACTION = new SettingsAction();
    public final transient ToggleAction TOGGLE_EXPERT_MODE_ACTION = new ToggleExpertModeAction(this);
    public final transient Action TUTORIAL_ACTION = new TutorialAction(this);
    public final transient Action UNDO_ACTION = new UndoAction(this);
    public final transient Action REDO_ACTION = new RedoAction(this);
    public final transient Action ANOVA_CALCULATOR_ACTION = new AnovaCalculatorAction();
    public final transient Action CHECK_FOR_JDBC_DRIVERS_ACTION = new CheckForJDBCDriversAction();
    public final transient Action MANAGE_DB_CONNECTIONS_ACTION = new ResourceAction(true, "manage_db_connections") {
        private static final long serialVersionUID = 2457587046500212869L;

        @Override
        public void actionPerformed(ActionEvent e) {
            ManageDatabaseConnectionsDialog dialog = new ManageDatabaseConnectionsDialog();
            dialog.setVisible(true);
        }
    };
    // public final transient Action ATTRIBUTE_DESCRIPTION_FILE_WIZARD = new AttributeDescriptionFileWizardAction();

    // --------------------------------------------------------------------------------

    // DOCKING

    public static final DockGroup DOCK_GROUP_ROOT = new DockGroup("root");
    public static final DockGroup DOCK_GROUP_RESULTS = new DockGroup("results");
    private final DockingContext dockingContext = new DockingContext();
    private final DockingDesktop dockingDesktop = new DockingDesktop("mainDesktop", dockingContext);
    // private final Perspective designPerspective;
    // private final Perspective resultPerspective;
    // private final Perspective welcomePerspective;

    private final Actions actions = new Actions(this);

    private final WelcomeScreen welcomeScreen = new WelcomeScreen(this);
    private final ResultDisplay resultDisplay = ResultDisplayTools.makeResultDisplay();
    private final LoggingViewer messageViewer = new LoggingViewer();
    private final SystemMonitor systemMonitor = new SystemMonitor();

    private final OperatorDocViewer operatorDocViewer = OperatorDocViewer.instantiate();
    // TODO: Enable as soon as documentation is ready
    //private final OperatorDocumentationBrowser operatorDocumentationBrowser = OperatorDocumentationBrowser.instantiate();
    private final OperatorTreePanel operatorTree = new OperatorTreePanel(this);
    private final ErrorTable errorTable = new ErrorTable(this);
    private final OperatorPropertyPanel propertyPanel = new OperatorPropertyPanel(this);
    private final XMLEditor xmlEditor = new XMLEditor(this);
    private final CommentEditor commentEditor = new CommentEditor();
    private final ProcessContextProcessEditor processContextEditor = new ProcessContextProcessEditor();
    private final NewOperatorEditor newOperatorEditor = new NewOperatorEditor();
    private final ProcessPanel processPanel = new ProcessPanel(this);
    private final RepositoryBrowser repositoryBrowser = new RepositoryBrowser();
    private final RemoteProcessViewer remoteProcessViewer = new RemoteProcessViewer();

    private final Perspectives perspectives = new Perspectives(dockingContext);

    private final EventListenerList processEditors = new EventListenerList();
    private List<Operator> selectedOperators = Collections.emptyList();

    private boolean changed = false;
    private boolean tutorialMode = false;
    private int undoIndex;

    private final JMenuBar menuBar;

    private final JMenu fileMenu;
    private final JMenu editMenu;
    private final JMenu processMenu;
    private final JMenu toolsMenu;
    private final JMenu viewMenu;
    private final JMenu helpMenu;

    private final JMenu recentFilesMenu = new ResourceMenu("recent_files");

    private final LinkedList<String> undoList = new LinkedList<String>();

    /** XML representation of the process at last validation. */
    private String lastProcessXML;

    /**
     * The host name of the system. Might be empty (no host name will be shown) and will be initialized
     * in the first call of {@link #setTitle()}.
     */
    private String hostname = null;

    private transient Process process = null;
    private transient ProcessThread processThread;

    private final MetaDataUpdateQueue metaDataUpdateQueue = new MetaDataUpdateQueue(this);

    // --------------------------------------------------------------------------------
    // LISTENERS And OBSERVERS

    private long lastUpdate = 0;
    private final Timer updateTimer = new Timer(500, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateProcessNow();
        }
    }) {
        private static final long serialVersionUID = 1L;
        {
            setRepeats(false);
        }
    };

    private void updateProcessNow() {
        lastUpdate = System.currentTimeMillis();
        String xmlWithoutGUIInformation = process.getRootOperator().getXML(true, false);
        if (!xmlWithoutGUIInformation.equals(lastProcessXML)) {
            addToUndoList(xmlWithoutGUIInformation);
            validateProcess(false);
        } else {
            processPanel.getProcessRenderer().repaint();
        }
        lastProcessXML = xmlWithoutGUIInformation;
    }

    public void validateProcess(boolean force) {
        if (force || process.getProcessState() != Process.PROCESS_STATE_RUNNING) {
            metaDataUpdateQueue.validate(process, force);
        }
        fireProcessUpdated();
    }

    private transient final Observer<Process> processObserver = new Observer<Process>() {
        @Override
        public void update(Observable<Process> observable, Process arg) {
            // if (process.getProcessState() == Process.PROCESS_STATE_RUNNING) {
            // return;
            // }
            if (System.currentTimeMillis() - lastUpdate > 500) {
                updateProcessNow();
            } else {
                if (process.getProcessState() == Process.PROCESS_STATE_RUNNING) {
                    if (!updateTimer.isRunning()) {
                        updateTimer.start();
                    }
                } else {
                    updateProcessNow();
                }
            }
        }
    };

    private transient final BreakpointListener breakpointListener = new BreakpointListener() {
        @Override
        public void breakpointReached(Process process, final Operator operator, final IOContainer ioContainer, int location) {
            if (process.equals(MainFrame.this.process)) {
                RUN_ACTION.setState(process.getProcessState());
                ProcessThread.beep("breakpoint");
                MainFrame.this.toFront();
                resultDisplay.showData(ioContainer, "Breakpoint in " + operator.getName() + ", application " + operator.getApplyCount());
            }
        }

        /** Since the mainframe triggers the resume itself this method does nothing. */
        @Override
        public void resume() {
            RUN_ACTION.setState(process.getProcessState());
        }
    };

    // --------------------------------------------------------------------------------

    /** Creates a new main frame containing the RapidMiner GUI. */
    public MainFrame() {
        this("welcome");
    }

    public MainFrame(String initialPerspective) {
        super(TITLE);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        addProcessEditor(actions);
        addProcessEditor(xmlEditor);
        addProcessEditor(commentEditor);
        addProcessEditor(propertyPanel);
        addProcessEditor(operatorTree);
        addProcessEditor(operatorDocViewer);
        //addProcessEditor(operatorDocumentationBrowser);
        addProcessEditor(processPanel);
        addProcessEditor(errorTable);
        addProcessEditor(processContextEditor);
        addProcessEditor(getStatusBar());
        addProcessEditor(resultDisplay);

        SwingTools.setFrameIcon(this);


        dockingContext.addDesktop(dockingDesktop);
        dockingDesktop.registerDockable(welcomeScreen);
        dockingDesktop.registerDockable(repositoryBrowser);
        dockingDesktop.registerDockable(operatorTree);
        dockingDesktop.registerDockable(propertyPanel);
        dockingDesktop.registerDockable(processPanel);
        dockingDesktop.registerDockable(commentEditor);
        dockingDesktop.registerDockable(xmlEditor);
        dockingDesktop.registerDockable(newOperatorEditor);
        dockingDesktop.registerDockable(errorTable);
        dockingDesktop.registerDockable(resultDisplay);
        dockingDesktop.registerDockable(messageViewer);
        dockingDesktop.registerDockable(systemMonitor);
        dockingDesktop.registerDockable(operatorDocViewer);
        //dockingDesktop.registerDockable(operatorDocumentationBrowser);
        dockingDesktop.registerDockable(processContextEditor);
        dockingDesktop.registerDockable(remoteProcessViewer);
        dockingDesktop.registerDockable(processPanel.getProcessRenderer().getOverviewPanel());
        //Test
        
        ToolBarContainer toolBarContainer = ToolBarContainer.createDefaultContainer(true, true, true, true);
        getContentPane().add(toolBarContainer, BorderLayout.CENTER);
        toolBarContainer.add(dockingDesktop, BorderLayout.CENTER);

        systemMonitor.startMonitorThread();
        resultDisplay.init(this);

        // menu bar
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        fileMenu = new ResourceMenu("file");
        fileMenu.add(NEW_ACTION);
        fileMenu.add(OPEN_ACTION);
        fileMenu.add(OPEN_TEMPLATE_ACTION);
        updateRecentFileList();
        fileMenu.add(recentFilesMenu);
        fileMenu.addSeparator();
        fileMenu.add(SAVE_ACTION);
        fileMenu.add(SAVE_AS_ACTION);
        fileMenu.add(SAVE_AS_TEMPLATE_ACTION);
        fileMenu.addSeparator();
        ResourceMenu importMenu = new ResourceMenu("file.import");
        importMenu.add(IMPORT_CSV_FILE_ACTION);
        importMenu.add(IMPORT_EXCEL_FILE_ACTION);
        importMenu.add(IMPORT_XML_FILE_ACTION);
        importMenu.add(IMPORT_ACCESS_FILE_ACTION);
        importMenu.add(IMPORT_DATABASE_TABLE_ACTION);
        importMenu.add(BlobImportWizard.IMPORT_BLOB_ACTION);
        fileMenu.add(importMenu);
        fileMenu.add(IMPORT_PROCESS_ACTION);
        fileMenu.add(EXPORT_PROCESS_ACTION);
        fileMenu.addSeparator();
        fileMenu.add(PRINT_ACTION);
        fileMenu.add(PRINT_PREVIEW_ACTION);
        fileMenu.add(PAGE_SETUP_ACTION);
        fileMenu.add(EXPORT_ACTION);
        fileMenu.addSeparator();
        fileMenu.add(EXIT_ACTION);
        menuBar.add(fileMenu);

        // edit menu
        editMenu = new ResourceMenu("edit");
        editMenu.add(UNDO_ACTION);
        editMenu.add(REDO_ACTION);
        editMenu.addSeparator();
        editMenu.add(actions.INFO_OPERATOR_ACTION);
        editMenu.add(actions.TOGGLE_ACTIVATION_ITEM.createMenuItem());
        editMenu.add(actions.RENAME_OPERATOR_ACTION);
        editMenu.addSeparator();
        editMenu.add(actions.NEW_OPERATOR_ACTION);
        editMenu.add(actions.NEW_BUILDING_BLOCK_ACTION);
        editMenu.add(actions.SAVE_BUILDING_BLOCK_ACTION);
        editMenu.addSeparator();
        editMenu.add(CutCopyPasteAction.CUT_ACTION);
        editMenu.add(CutCopyPasteAction.COPY_ACTION);
        editMenu.add(CutCopyPasteAction.PASTE_ACTION);
        editMenu.add(actions.DELETE_OPERATOR_ACTION);
        editMenu.addSeparator();
        for (ToggleBreakpointItem item : actions.TOGGLE_BREAKPOINT) {
            editMenu.add(item.createMenuItem());
        }
        editMenu.add(actions.TOGGLE_ALL_BREAKPOINTS.createMenuItem());
        // editMenu.add(actions.MAKE_DIRTY_ACTION);
        menuBar.add(editMenu);

        // process menu
        processMenu = new ResourceMenu("process");
        processMenu.add(RUN_ACTION);
        processMenu.add(PAUSE_ACTION);
        processMenu.add(STOP_ACTION);
        processMenu.addSeparator();
        processMenu.add(VALIDATE_ACTION);
        processMenu.add(VALIDATE_AUTOMATICALLY_ACTION.createMenuItem());
        // JCheckBoxMenuItem onlyDirtyMenu = new JCheckBoxMenuItem(new ResourceAction(true, "execute_only_dirty") {
        // private static final long serialVersionUID = 2158722678316407076L;
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // if (((JCheckBoxMenuItem)e.getSource()).isSelected()) {
        // getProcess().setExecutionMode(ExecutionMode.ONLY_DIRTY);
        // } else {
        // getProcess().setExecutionMode(ExecutionMode.ALWAYS);
        // }
        // }
        // });
        // expMenu.add(onlyDirtyMenu);

        JCheckBoxMenuItem debugmodeMenu = new JCheckBoxMenuItem(new ResourceAction(true, "process_debug_mode") {
            private static final long serialVersionUID = 2158722678316407076L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                    getProcess().setDebugMode(DebugMode.COLLECT_METADATA_AFTER_EXECUTION);
                } else {
                    getProcess().setDebugMode(DebugMode.DEBUG_OFF);
                }
            }
        });
        processMenu.add(debugmodeMenu);
        processMenu.addSeparator();

        JMenu wiringMenu = new ResourceMenu("wiring");
        wiringMenu.add(AUTO_WIRE);
        wiringMenu.add(AUTO_WIRE_RECURSIVELY);
        wiringMenu.add(REWIRE);
        wiringMenu.add(REWIRE_RECURSIVELY);
        processMenu.add(wiringMenu);
        JMenu orderMenu = new ResourceMenu("execution_order");
        orderMenu.add(processPanel.getProcessRenderer().getFlowVisualizer().ALTER_EXECUTION_ORDER.createMenuItem());
        orderMenu.add(processPanel.getProcessRenderer().getFlowVisualizer().SHOW_EXECUTION_ORDER);
        processMenu.add(orderMenu);
        JMenu layoutMenu = new ResourceMenu("process_layout");
        layoutMenu.add(processPanel.getProcessRenderer().ARRANGE_OPERATORS_ACTION);
        layoutMenu.add(processPanel.getProcessRenderer().AUTO_FIT_ACTION);
        layoutMenu.add(processPanel.getProcessRenderer().INCREASE_PROCESS_LAYOUT_WIDTH_ACTION);
        layoutMenu.add(processPanel.getProcessRenderer().DECREASE_PROCESS_LAYOUT_WIDTH_ACTION);
        layoutMenu.add(processPanel.getProcessRenderer().INCREASE_PROCESS_LAYOUT_HEIGHT_ACTION);
        layoutMenu.add(processPanel.getProcessRenderer().DECREASE_PROCESS_LAYOUT_HEIGHT_ACTION);
        processMenu.add(layoutMenu);
        processMenu.addSeparator();
        processMenu.add(RUN_REMOTE_ACTION);
        menuBar.add(processMenu);

        // tools menu
        toolsMenu = new ResourceMenu("tools");
        toolsMenu.add(MANAGE_BUILDING_BLOCKS_ACTION);
        toolsMenu.add(MANAGE_TEMPLATES_ACTION);
        toolsMenu.addSeparator();
        toolsMenu.add(ANOVA_CALCULATOR_ACTION);
        toolsMenu.addSeparator();
        toolsMenu.add(CHECK_FOR_JDBC_DRIVERS_ACTION);
        toolsMenu.add(MANAGE_DB_CONNECTIONS_ACTION);
        toolsMenu.add(ManageDatabaseDriversDialog.SHOW_DIALOG_ACTION);
        toolsMenu.addSeparator();
        toolsMenu.add(UsageStatsTransmissionDialog.SHOW_STATISTICS_ACTION);
        //Password Manager
        toolsMenu.add(PasswordManager.OPEN_WINDOW);
        toolsMenu.add(SETTINGS_ACTION);
        // Configurators
        toolsMenu.addSeparator();
        for (String typeID : ConfigurationManager.getInstance().getAllTypeIds()) {
        	toolsMenu.add(ConfigurationDialog.getOpenWindowAction(typeID));
        }
        menuBar.add(toolsMenu);

        // view menu
        viewMenu = new ResourceMenu("view");
        viewMenu.add(perspectives.getWorkspaceMenu());
        viewMenu.add(NEW_PERSPECTIVE_ACTION);
        viewMenu.add(new DockableMenu(dockingContext));
        viewMenu.add(perspectives.RESTORE_DEFAULT_ACTION);
        viewMenu.addSeparator();
        viewMenu.add(TOGGLE_EXPERT_MODE_ACTION.createMenuItem());
        menuBar.add(viewMenu);

        // help menu
        helpMenu = new ResourceMenu("help");
        helpMenu.add(TUTORIAL_ACTION);
        // TODO: Re-add updated manual
        // helpMenu.add(new ResourceAction("gui_manual") {
        // private static final long serialVersionUID = 1L;
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // URL manualResource = Tools.getResource("manual/RapidMinerGUIManual.html");
        // if (manualResource != null)
        // Browser.showDialog(manualResource);
        // else
        // SwingTools.showVerySimpleErrorMessage("Cannot load GUI manual: file not found.");
        // }
        //
        // });
        helpMenu.add(new BrowseAction("help_support", URI.create("http://rapid-i.com/content/view/60/89/lang,en/")));
        helpMenu.add(new BrowseAction("help_videotutorials", URI.create("http://rapid-i.com/content/view/189/198/")));
        helpMenu.add(new BrowseAction("help_forum", URI.create("http://forum.rapid-i.com")));
        helpMenu.add(new BrowseAction("help_wiki", URI.create("http://wiki.rapid-i.com")));

        helpMenu.addSeparator();
        // helpMenu.add(CHECK_FOR_UPDATES_ACTION);
        helpMenu.add(ExtensionDialog.MANAGE_EXTENSIONS);

        List allPlugins = Plugin.getAllPlugins();
        if (allPlugins.size() > 0) {
            JMenu extensionsMenu = new ResourceMenu("about_extensions");
            Iterator i = allPlugins.iterator();
            while (i.hasNext()) {
                final Plugin plugin = (Plugin) i.next();
                if (plugin.showAboutBox()) {
                    extensionsMenu.add(new ResourceAction("about_extension", plugin.getName()) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            plugin.createAboutBox(MainFrame.this).setVisible(true);
                        }
                    });
                }
            }
            helpMenu.add(extensionsMenu);
        }
        helpMenu.addSeparator();
        helpMenu.add(UpdateDialog.UPDATE_ACTION);
        helpMenu.add(new AboutAction(this));


        menuBar.add(helpMenu);

        // Tool Bar
        RapidDockingToolbar fileToolBar = new RapidDockingToolbar("file");
        fileToolBar.add(makeToolbarButton(NEW_ACTION));
        fileToolBar.add(makeToolbarButton(OPEN_ACTION));
        fileToolBar.add(makeToolbarButton(SAVE_ACTION));
        fileToolBar.add(makeToolbarButton(SAVE_AS_ACTION));
        fileToolBar.add(makeToolbarButton(PRINT_ACTION));

        RapidDockingToolbar editToolBar = new RapidDockingToolbar("edit");
        editToolBar.add(makeToolbarButton(UNDO_ACTION));
        editToolBar.add(makeToolbarButton(REDO_ACTION));

        RapidDockingToolbar runToolBar = new RapidDockingToolbar("run");
        runToolBar.add(makeToolbarButton(RUN_ACTION));
        runToolBar.add(makeToolbarButton(PAUSE_ACTION));
        runToolBar.add(makeToolbarButton(STOP_ACTION));

        if ("true".equals(System.getProperty(RapidMiner.PROPERTY_DEVELOPER_MODE))) {
            runToolBar.addSeparator();
            runToolBar.add(makeToolbarButton(VALIDATE_ACTION));
        }

        RapidDockingToolbar viewToolBar = perspectives.getWorkspaceToolBar();
        ToolBarPanel toolBarPanel = toolBarContainer.getToolBarPanelAt(BorderLayout.NORTH);
        toolBarPanel.add(fileToolBar, new ToolBarConstraints(0, 0));
        toolBarPanel.add(editToolBar, new ToolBarConstraints(0, 1));
        toolBarPanel.add(runToolBar, new ToolBarConstraints(0, 2));
        toolBarPanel.add(viewToolBar, new ToolBarConstraints(0, 3));

        getContentPane().add(getStatusBar(), BorderLayout.SOUTH);
        getStatusBar().startClockThread();

        setProcess(new Process(), true);
        selectOperator(process.getRootOperator());
        addToUndoList();

        perspectives.showPerspective(initialPerspective);
        pack();
        metaDataUpdateQueue.start();
    }

    private JButton makeToolbarButton(Action action) {
        JButton button = new JButton(action);
        if (button.getIcon() != null) {
            button.setText(null);
        }
        return button;
    }
    	
    /**
     * 
     * @deprecated Use {@link #getPerspectives()} and {@link Perspectives#showPerspective(String)}
     */
    @Deprecated
    public void changeMode(int mode) {
        // TODO: remove
    }

    public void startTutorial() {
        if (close()) {
            new Tutorial(MainFrame.this).setVisible(true);
        }
    }

    public void setTutorialMode(boolean mode) {
        this.tutorialMode = mode;
        if (tutorialMode) {
            SAVE_ACTION.setEnabled(false);
            SAVE_AS_ACTION.setEnabled(false);
        } else {
            SAVE_ACTION.setEnabled(false);
            SAVE_AS_ACTION.setEnabled(true);
        }
    }

    public boolean isTutorialMode() {
        return this.tutorialMode;
    }

    public void setExpertMode(boolean expert) {
        TOGGLE_EXPERT_MODE_ACTION.setSelected(expert);
        TOGGLE_EXPERT_MODE_ACTION.actionToggled(null);
    }

    public OperatorPropertyPanel getPropertyPanel() {
        return propertyPanel;
    }

    public LoggingViewer getMessageViewer() {
        return messageViewer;
    }

    public NewOperatorEditor getNewOperatorEditor() {
        return newOperatorEditor;
    }

    public OperatorTree getOperatorTree() {
        return operatorTree.getOperatorTree();
    }

    public Actions getActions() {
        return actions;
    }

    public ResultDisplay getResultDisplay() {
        return resultDisplay;
    }

    public int getProcessState() {
        if (process == null) {
            return Process.PROCESS_STATE_UNKNOWN;
        } else {
            return process.getProcessState();
        }
    }

    /**
     * @deprecated Use {@link #getProcess()} instead
     */
    @Deprecated
    public final Process getExperiment() {
        return getProcess();
    }

    public final Process getProcess() {
        return this.process;
    }

    // ====================================================
    // M A I N A C T I O N S
    // ===================================================

    /** Creates a new process. */
    public void newProcess() {
        if (close()) {
            stopProcess();
            setProcess(new Process(), true);
            addToUndoList();
            if (!"false".equals(ParameterService.getParameterValue(PROPERTY_RAPIDMINER_GUI_SAVE_ON_PROCESS_CREATION))) {
                SaveAction.save(getProcess());
            }
        }
    }

    /** Runs or resumes the current process. */
    public void runProcess() {
        if (getProcessState() == Process.PROCESS_STATE_STOPPED) {
            // Run
            if ((isChanged() || getProcess().getProcessLocation() == null) && !isTutorialMode()) {
                if (DecisionRememberingConfirmDialog.confirmAction("save_before_run", PROPERTY_RAPIDMINER_GUI_SAVE_BEFORE_RUN)) {
                    SaveAction.save(getProcess());
                }
            }

            processThread = new ProcessThread(MainFrame.this.process);

            try {
                processThread.start();
            } catch (Exception t) {
                SwingTools.showSimpleErrorMessage("cannot_start_process", t);
            }
        } else {
            process.resume();
        }
    }

    /**
     * Can be used to stop the currently running process. Please note that
     * the ProcessThread will still be running in the background until the current
     * operator is finished.
     */
    public void stopProcess() {
        if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
            getProcess().getLogger().info("Process stopped. Completing current operator.");
            getStatusBar().setSpecialText("Process stopped. Completing current operator.");
            if (processThread != null) {
                if (processThread.isAlive()) {
                    processThread.setPriority(Thread.MIN_PRIORITY);
                    processThread.stopProcess();
                }
            }
        }
    }

    public void pauseProcess() {
        if (getProcessState() == Process.PROCESS_STATE_RUNNING) {
            getProcess().getLogger().info("Process paused. Completing current operator.");
            getStatusBar().setSpecialText("Process paused. Completing current operator.");
            if (processThread != null) {
                processThread.pauseProcess();
            }
        }
    }

    /** Will be invoked from the process thread after the process was successfully ended. */
    void processEnded(final Process process, final IOContainer results) {
        if (process.equals(MainFrame.this.process)) {
            if (results != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        MainFrame.this.toFront();
                    }
                });
            }
        }
        if (process.equals(MainFrame.this.process)) {
            if (results != null) {
                resultDisplay.showData(results, "Process results");
                ProcessLocation location = MainFrame.this.process.getProcessLocation();
                String resultName = location != null ? location.getShortName() : "unnamed";
                RapidMinerGUI.getResultHistory().addResults(resultName, MainFrame.this.process.getRootOperator(), results);
            }
        }
    }

    /**
     * Sets a new process and registers the MainFrame listener. Please note
     * that this method does not invoke {@link #processChanged()}. Do so
     * if necessary.
     * 
     * @deprecated Use {@link #setProcess(Process, boolean)} instead
     */
    @Deprecated
    public void setExperiment(Process process) {
        setProcess(process, true);
    }

    /**
     * Sets a new process and registers the MainFrame listener. Please note
     * that this method only invoke {@link #processChanged()} if the parameter
     * newProcess is true.
     */
    public void setProcess(Process process, boolean newProcess) {
        boolean firstProcess = this.process == null;
        if (this.process != null) {
            // this.process.getRootOperator().removeObserver(processObserver);
            this.process.removeObserver(processObserver);
        }

        if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
            if (processThread != null) {
                processThread.stopProcess();
            }
        }

        if (process != null) {
            // process.getRootOperator().addObserver(processObserver, true);
            process.addObserver(processObserver, true);

            synchronized (process) {
                this.process = process;
                this.processThread = new ProcessThread(this.process);
                this.process.addBreakpointListener(breakpointListener);
                fireProcessChanged();
                selectOperator(this.process.getRootOperator());
                if (VALIDATE_AUTOMATICALLY_ACTION.isSelected()) {
                    validateProcess(false);
                }
            }
        }
        if (newProcess && !firstProcess) {
            // VLDocking appears to get nervous when applying two perspectives while the
            // window is not yet visible. So to avoid that we set design and then welcome
            // during startup, avoid applying design if this is the first process we create.
            perspectives.showPerspective("design");
        }
        setTitle();
    }

    /**
     * Must be called when the process changed (such that is different from
     * the process before). Enables the correct actions if the process
     * can be saved to disk.
     * 
     * @deprecated this method is no longer necessary (and does nothing) since the MainFrame
     *             observes the process using an Observer pattern. See {@link #processObserver}.
     */
    @Deprecated
    public void processChanged() {
    }

    /** Returns true if the process has changed since the last save. */
    public boolean isChanged() {
        return changed;
    }

    private boolean addToUndoList() {
        return addToUndoList(null);
    }
    /**
     * Adds the current state of the process to the undo list.
     * 
     * Note: This method must not be exposed by making it public. It may confuse
     * the MainFrame such that it can no longer determine correctly whether
     * validation is possible.
     * 
     * @return true if process really differs.
     */
    private boolean addToUndoList(String currentStateXML) {
        final String lastStateXML = undoList.size() != 0 ? (String) undoList.get(undoList.size() - 1) : null;
        if (currentStateXML == null)
            currentStateXML = this.process.getRootOperator().getXML(true);
        if (currentStateXML != null) {
            if (lastStateXML == null || !lastStateXML.equals(currentStateXML)) {
                if (undoIndex < undoList.size() - 1) {
                    while (undoList.size() > undoIndex + 1)
                        undoList.removeLast();
                }
                undoList.add(currentStateXML);
                String maxSizeProperty = ParameterService.getParameterValue(PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE);
                int maxSize = 20;
                try {
                    if (maxSizeProperty != null)
                        maxSize = Integer.parseInt(maxSizeProperty);
                } catch (NumberFormatException e) {
                    LogService.getRoot().warning("com.rapidminer.gui.main_frame_warning");
                	//LogService.getRoot().warning("Bad integer format for property 'rapidminer.gui.undolist.size', using default size of 20.");
                }
                while (undoList.size() > maxSize)
                    undoList.removeFirst();
                undoIndex = undoList.size() - 1;
                enableUndoAction();

                boolean oldValue = MainFrame.this.changed;
                MainFrame.this.changed = lastStateXML != null;

                if (!oldValue) {
                    setTitle();
                }
                if (MainFrame.this.process.getProcessLocation() != null && !tutorialMode) {
                    MainFrame.this.SAVE_ACTION.setEnabled(true);
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void undo() {
        if (undoIndex > 0) {
            undoIndex--;
            setProcessIntoStateAt(undoIndex);
        }
        enableUndoAction();
    }

    public void redo() {
        if (undoIndex < undoList.size()) {
            undoIndex++;
            setProcessIntoStateAt(undoIndex);
        }
        enableUndoAction();
    }

    private void enableUndoAction() {
        if (undoIndex > 0) {
            UNDO_ACTION.setEnabled(true);
        } else {
            UNDO_ACTION.setEnabled(false);
        }
        if (undoIndex < undoList.size() - 1) {
            REDO_ACTION.setEnabled(true);
        } else {
            REDO_ACTION.setEnabled(false);
        }
    }

    private void setProcessIntoStateAt(int undoIndex) {
        String stateXML = undoList.get(undoIndex);
        try {
            synchronized (process) {
                Process process = new Process(stateXML, this.process);
                // this.process.setupFromXML(stateXML);
                setProcess(process, false);
                // cannot use method processChanged() because this would add the
                // old state to the undo stack!
                this.changed = true;
                setTitle();
                if (this.process.getProcessLocation() != null && !tutorialMode) {
                    this.SAVE_ACTION.setEnabled(true);
                }
            }
        } catch (Exception e) {
            SwingTools.showSimpleErrorMessage("while_changing_process", e);
        }
    }

    /**
     * Sets the window title (RapidMiner + filename + an asterisk if process was
     * modified.
     */
    private void setTitle() {
        if (hostname == null) {
            try {
                hostname = "@" + InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostname = "";
            }
        }

        if (this.process != null) {
            ProcessLocation loc = process.getProcessLocation();
            if (loc != null) {
                setTitle(loc.getShortName() + (changed ? "*" : "") + " \u2013 " + TITLE + hostname);
            } else {
                setTitle("<new process" + (changed ? "*" : "") + "> \u2013 " + TITLE + hostname);
            }
        } else {
            setTitle(TITLE + hostname);
        }
    }

    // //////////////////// File menu actions ////////////////////

    public boolean close() {
        if (changed) {
            ProcessLocation loc = process.getProcessLocation();
            String locName;
            if (loc != null) {
                locName = loc.getShortName();
            } else {
                locName = "unnamed";
            }
            switch (SwingTools.showConfirmDialog("save", ConfirmDialog.YES_NO_CANCEL_OPTION, locName)) {
            case ConfirmDialog.YES_OPTION:
                SaveAction.save(getProcess());

                // it may happen that save() does not actually save the process, because the user hits cancel in the
                // saveAs dialog or an error occurs. In this case the process won't be marked as unchanged. Thus,
                // we return the process changed status.
                return !isChanged();
            case ConfirmDialog.NO_OPTION:
                if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
                    synchronized (processThread) {
                        processThread.stopProcess();
                    }
                }
                return true;
            default: // cancel
                return false;
            }
        } else {
            return true;
        }
    }

    public void setOpenedProcess(Process process, boolean showInfo, final String sourceName) {
        setProcess(process, true);
        if (process.getImportMessage() != null) {
            SwingTools.showLongMessage("import_message", process.getImportMessage());
        }

        SAVE_ACTION.setEnabled(false);

        synchronized (process) {
            RapidMinerGUI.useProcessFile(MainFrame.this.process);
            updateRecentFileList();
            addToUndoList();
            changed = false;
            setTitle();

            // show unsupported parameters info?
            List<UnknownParameterInformation> unknownParameters = process.getUnknownParameters();
            if (unknownParameters.size() > 0) {
                new UnknownParametersInfoDialog(MainFrame.this, unknownParameters).setVisible(true);
            } else if (showInfo && Tools.booleanValue(ParameterService.getParameterValue(PROPERTY_RAPIDMINER_GUI_PROCESSINFO_SHOW), true)) {
                // show process info?
                final String text = MainFrame.this.process.getRootOperator().getUserDescription();
                if (text != null && text.length() != 0) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ProcessInfoScreen infoScreen = new ProcessInfoScreen(sourceName, text);
                            infoScreen.setVisible(true);
                        }
                    });
                }
            }
        }
    }

    public void saveAsTemplate() {
        synchronized (process) {
            SaveAsTemplateDialog dialog = new SaveAsTemplateDialog(MainFrame.this.process);
            dialog.setVisible(true);
            if (dialog.isOk()) {
                try {
                    dialog.getTemplate().saveAsUserTemplate(MainFrame.this.process);
                } catch (Exception ioe) {
                    SwingTools.showSimpleErrorMessage("cannot_write_template_file", ioe);
                }
            }
        }
    }

    public void exit(boolean relaunch) {
        if (changed) {
            ProcessLocation loc = process.getProcessLocation();
            String locName;
            if (loc != null) {
                locName = loc.getShortName();
            } else {
                locName = "unnamed";
            }
            switch (SwingTools.showConfirmDialog("save", ConfirmDialog.YES_NO_CANCEL_OPTION, locName)) {
            case ConfirmDialog.YES_OPTION:
                SaveAction.save(process);
                if (changed)
                    return;
                break;
            case ConfirmDialog.NO_OPTION:
                break;
            case ConfirmDialog.CANCEL_OPTION:
            default:
                return;
            }
        } else {
            if (!relaunch) { // in this case we have already confirmed
                int answer = ConfirmDialog.showConfirmDialog("exit", ConfirmDialog.YES_NO_OPTION, RapidMinerGUI.PROPERTY_CONFIRM_EXIT, ConfirmDialog.YES_OPTION);
                if (answer != ConfirmDialog.YES_OPTION) {
                    return;
                }
            }
        }
        stopProcess();
        dispose();
        RapidMinerGUI.quit(relaunch ? RapidMiner.ExitMode.RELAUNCH : RapidMiner.ExitMode.NORMAL);
    }

    /** Updates the list of recently used files. */
    public void updateRecentFileList() {
        recentFilesMenu.removeAll();
        List<ProcessLocation> recentFiles = RapidMinerGUI.getRecentFiles();
        int j = 1;
        for (final ProcessLocation recentLocation : recentFiles) {
            JMenuItem menuItem = new JMenuItem(j + " " + recentLocation.toMenuString());
            menuItem.setMnemonic('0' + j);
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (RapidMinerGUI.getMainFrame().close()) {
                        OpenAction.open(recentLocation, true);
                    }
                }
            });
            recentFilesMenu.add(menuItem);
            j++;
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        exit(false);
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    /**
     * This methods provide plugins the possibility to modify the menus
     */
    public void removeMenu(int index) {
        menuBar.remove(menuBar.getMenu(index));
    }

    public void removeMenuItem(int menuIndex, int itemIndex) {
        menuBar.getMenu(menuIndex).remove(itemIndex);
    }

    public void addMenuItem(int menuIndex, int itemIndex, JMenuItem item) {
        menuBar.getMenu(menuIndex).add(item, itemIndex);
    }

    public void addMenu(int menuIndex, JMenu menu) {
        menuBar.add(menu, menuIndex);
    }

    public void addMenuSeparator(int menuIndex) {
        menuBar.getMenu(menuIndex).addSeparator();
    }

    // / LISTENERS

    public List<Operator> getSelectedOperators() {
        return selectedOperators;
    }

    public Operator getFirstSelectedOperator() {
        return selectedOperators.isEmpty() ? null : selectedOperators.get(0);
    }

    public void addProcessEditor(ProcessEditor p) {
        processEditors.add(ProcessEditor.class, p);
    }

    public void selectOperator(Operator currentlySelected) {
        if (currentlySelected == null) {
            currentlySelected = process.getRootOperator();
        }
        selectOperators(Collections.singletonList(currentlySelected));
    }

    public void selectOperators(List<Operator> currentlySelected) {
        if (currentlySelected == null) {
            currentlySelected = Collections.<Operator> singletonList(process.getRootOperator());
        }
        for (Operator op : currentlySelected) {
            Process selectedProcess = op.getProcess();
            if (selectedProcess == null || selectedProcess != process) {
                SwingTools.showVerySimpleErrorMessage("op_deleted", op.getName());
                return;
            }
        }
        this.selectedOperators = currentlySelected;
        fireSelectedOperatorChanged(selectedOperators);
    }

    /** Notifies the main editor of the change of the currently selected operator. */
    private void fireSelectedOperatorChanged(List<Operator> currentlySelected) {
        for (ProcessEditor editor : processEditors.getListeners(ProcessEditor.class)) {
            editor.setSelection(currentlySelected);
        }
    }

    public void fireProcessUpdated() {
        for (ProcessEditor editor : processEditors.getListeners(ProcessEditor.class)) {
            editor.processUpdated(process);
        }
    }

    private void fireProcessChanged() {
        for (ProcessEditor editor : processEditors.getListeners(ProcessEditor.class)) {
            editor.processChanged(process);
        }
    }

    public DockingDesktop getDockingDesktop() {
        return dockingDesktop;
    }

    public Perspectives getPerspectives() {
        return perspectives;
    }

    public void handleBrokenProxessXML(ProcessLocation location, String xml, Exception e) {
        SwingTools.showSimpleErrorMessage("while_loading", e, location.toString(), e.getMessage());
        Process process = new Process();
        process.setProcessLocation(location);
        setProcess(process, true);
        perspectives.showPerspective("design");
        // TODO: Re-enable this
        // mainEditor.changeToXMLEditor();
        xmlEditor.setText(xml);
    }

    public OperatorDocViewer getOperatorDocViewer() {
        return operatorDocViewer;
    }
//    public OperatorDocumentationBrowser getOperatorDocumentationBrowser() {
//        return operatorDocumentationBrowser;
//    }

    public ProcessPanel getProcessPanel() {
        return processPanel;
    }

    public void registerDockable(Dockable dockable) {
        dockingDesktop.registerDockable(dockable);
    }

    public void processHasBeenSaved() {
        SAVE_ACTION.setEnabled(false);
        changed = false;
        setTitle();
        updateRecentFileList();
    }

    public ProcessContextProcessEditor getProcessContextEditor() {
        return processContextEditor;
    }

    public Component getXMLEditor() {
        return xmlEditor;
    }

    /**
     * This returns the file menu to change menu entries
     */
    public JMenu getFileMenu() {
        return fileMenu;
    }

    /**
     * This returns the tools menu to change menu entries
     */

    public JMenu getToolsMenu() {
        return toolsMenu;
    }

    /**
     * This returns the complete menu bar to insert additional menus
     */
    public JMenuBar getMainMenuBar() {
        return menuBar;
    }

    /**
     * This returns the edit menu to change menu entries
     */
    public JMenu getEditMenu() {
        return editMenu;
    }

    /**
     * This returns the process menu to change menu entries
     */

    public JMenu getProcessMenu() {
        return processMenu;
    }

    /**
     * This returns the help menu to change menu entries
     */
    public JMenu getHelpMenu() {
        return helpMenu;
    }
}
