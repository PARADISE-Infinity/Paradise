/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.ViewPart;

import de.dlr.premise.view.graphicaleditorview.actions.ConnectionAction;
import de.dlr.premise.view.graphicaleditorview.actions.DisableLayoutAction;
import de.dlr.premise.view.graphicaleditorview.actions.HierarchyAction;
import de.dlr.premise.view.graphicaleditorview.actions.LayoutOrientationAction;
import de.dlr.premise.view.graphicaleditorview.actions.PullUpEdgesAction;
import de.dlr.premise.view.graphicaleditorview.actions.RelationAction;
import de.dlr.premise.view.graphicaleditorview.actions.SatisfyAction;
import de.dlr.premise.view.graphicaleditorview.actions.SaveAsGraphMLAction;
import de.dlr.premise.view.graphicaleditorview.actions.SpinnerAction;
import de.dlr.premise.view.graphicaleditorview.actions.StatesAction;
import de.dlr.premise.view.graphicaleditorview.actions.ZoomToFitAction;
import de.dlr.premise.view.graphicaleditorview.view.EditModeImpl;
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;

/**
 * The SWT-View containing the yFiles Graph.
 */
public class GraphicalEditorView extends ViewPart {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "de.dlr.premise.view.graphicaleditorview.GraphicalEditorView";
    public static final String OPT_TECH = "Association Editor";

    public static final String OPT_CONTEXT_HIERARCHY = "Show Hierarchy";
    public static final String OPT_CONTEXT_RELATION = "Draw Relations";
    public static final String OPT_CONTEXT_CONNECTION = "Draw Connections";
    public static final String OPT_CONTEXT_SATISFIES = "Draw Satisfy-Relations";
    public static final String OPT_CONTEXT_STATES = "Edit local statemachines";

    public static final String OPT_GROUP_LAYOUT = "Group Elements";
    public static final String OPT_GROUP_EDGES = "Group Edges (Bus-Style Edge Routing)";
    private Frame frame;
    private Composite composite;
    private static Set<IWorkbenchAction> viewActions = new LinkedHashSet<>();

    @Override
    public void createPartControl(Composite parent) {
        // set system look and feel for the swing view
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // applying look and feel failed
        }

        // reduce flickering
        System.setProperty("sun.awt.noerasebackground", "true");

        initActions();

        Graph2DView view = createGraph2DView(parent);

        JPanel panel = new JPanel(new BorderLayout());
        JApplet applet = new JApplet();
        applet.add(panel);
        panel.add(view, BorderLayout.CENTER);

        composite = new Composite(parent, SWT.EMBEDDED);
        frame = SWT_AWT.new_Frame(composite);
        frame.add(applet);

        // handle graphing stuff
        Graph2D graph = view.getGraph2D();
        GraphRegistry.init(graph);
    }

    /**
     * Create the view and set it up.
     * 
     * @return the Graph2DView.
     */
    private Graph2DView createGraph2DView(Composite parent) {
        Graph2DView view = new Graph2DView();
        // add mouse wheel zoom support
        view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());

        // allow editing
        EditMode editMode = new EditModeImpl(parent);
        // To enable convenient switching between group node and folder node presentation.
        editMode.getMouseInputMode().setNodeSearchingEnabled(true);
        view.setFitContentOnResize(true);
        view.addViewMode(editMode);

        // enable keyboard interaction
        new Graph2DViewActions(view).install();

        return view;
    }

    /**
     * Setup the ToolBar with our actions.
     */
    private void initActions() {
        viewActions.add(HierarchyAction.getInstance());
        viewActions.add(ConnectionAction.getInstance());
        viewActions.add(RelationAction.getInstance());
        viewActions.add(SatisfyAction.getInstance());
        viewActions.add(StatesAction.getInstance());

        ZoomToFitAction zoomToFitAction = ZoomToFitAction.getInstance();
        LayoutOrientationAction layoutOrientationAction = LayoutOrientationAction.getInstance();
        PullUpEdgesAction pullUpEdgesAction = PullUpEdgesAction.getInstance();
        DisableLayoutAction disableLayoutAction = DisableLayoutAction.getInstance();
        SaveAsGraphMLAction saveAsGraphMLAction = SaveAsGraphMLAction.getInstance();

        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

        viewActions.forEach(toolBarManager::add);
        toolBarManager.add(new Separator());
        toolBarManager.add(zoomToFitAction);
        toolBarManager.add(new SpinnerAction("depthSpinner"));
        toolBarManager.add(pullUpEdgesAction);
        toolBarManager.add(layoutOrientationAction);
        toolBarManager.add(disableLayoutAction);
        toolBarManager.add(saveAsGraphMLAction);

    }

    public static void setActiveButton(IWorkbenchAction action) {
        action.setChecked(true);
        viewActions.stream().filter(a -> a != action).forEach(a -> a.setChecked(false));
    }

    @Override
    public void setFocus() {
        composite.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        GraphRegistry.dispose();
        super.dispose();
    }
}
