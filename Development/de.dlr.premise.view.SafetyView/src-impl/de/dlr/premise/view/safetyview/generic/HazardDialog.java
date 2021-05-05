/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.safetyview.generic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.dlr.premise.element.AElement;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.provider.my.ElementItemProviderAdapterFactoryMy;
import de.dlr.premise.element.provider.my.StateMachineItemProviderMy;
import de.dlr.premise.safety.impl.my.FailureHelper;


class HazardDialog extends TitleAreaDialog {

    private AElement parent;

    private Text txtName;
    private Text txtDescription;
    private ComboViewer comboHazardsViewer;    
   
    private String name;
    private String description;
    private StateMachine harmfulEvent;
    
    public HazardDialog (Shell parentShell, AElement parent) {
        super(parentShell);
        this.parent = parent;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Hazard description creation dialog");
        setMessage("This is a TitleAreaDialog", IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage("Add a name, a description and select the harmful event. In case no harmful event exists the failure becomes this event.");
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        GridData gd_container = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd_container.heightHint = 192;
        container.setLayoutData(gd_container);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        createFirstName(container);
        createLastName(container);
        createHarmfulEvent(container);
        
        return area;
    }
    
    private void createFirstName(Composite container) {
        Label lbtName = new Label(container, SWT.NONE);
        lbtName.setText("Name");

        GridData dataFirstName = new GridData();
        dataFirstName.grabExcessHorizontalSpace = true;
        dataFirstName.horizontalAlignment = GridData.FILL;

        txtName = new Text(container, SWT.BORDER);
        txtName.setText("Failure");
        txtName.setLayoutData(dataFirstName);
    }

    private void createLastName(Composite container) {
        
        Label lbtDescription = new Label(container, SWT.NONE);
        lbtDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        lbtDescription.setText("Description");

        GridData dataLastName = new GridData();
        dataLastName.heightHint = 112;
        dataLastName.verticalAlignment = SWT.FILL;
        dataLastName.grabExcessHorizontalSpace = true;
        dataLastName.horizontalAlignment = GridData.FILL;
        txtDescription = new Text(container, SWT.BORDER | SWT.WRAP | SWT.MULTI);
        txtDescription.setText("Add description ...");
        txtDescription.setLayoutData(dataLastName);
    }
    
    private void createHarmfulEvent(Composite container) {
        
        Label lblHarmful = new Label(container, SWT.NONE);
        lblHarmful.setText("Harmful");
        
        comboHazardsViewer = new ComboViewer(container, SWT.BORDER | SWT.READ_ONLY);
        comboHazardsViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        comboHazardsViewer.addSelectionChangedListener(new HazardSelectionListener());
        comboHazardsViewer.setLabelProvider(new HazardLabelProvider());
        
        // set input
        comboHazardsViewer.setContentProvider(ArrayContentProvider.getInstance());        
        comboHazardsViewer.setInput(getHarmFulEvents());        
    }
    
    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    private void saveInput() {
        name = txtName.getText();
        description = txtDescription.getText();
        
        IStructuredSelection selection = (IStructuredSelection) comboHazardsViewer.getSelection();       
        if (selection.size() > 0) {
            harmfulEvent = (StateMachine) selection.getFirstElement();
        }
    }

    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
 
    public StateMachine getHarmFulEvent() {
        return harmfulEvent;
    }

    private List<StateMachine> getHarmFulEvents() {
        List<StateMachine> hazards = new ArrayList<StateMachine>();        
        
        for(EObject obj : parent.eContents()) {
            if (obj instanceof StateMachine) {
                StateMachine stateMachine = (StateMachine) obj;
                if (FailureHelper.isHarmfulHazard(stateMachine)) {
                    hazards.add(stateMachine);
                }
            }
        }
        
        return hazards;
    }
    
    // internal hazard label provider
    protected class HazardLabelProvider extends LabelProvider {
        
        private AdapterFactory adapterFactory;
        private StateMachineItemProviderMy itemAdapter;

        public HazardLabelProvider() {
            adapterFactory = new ElementItemProviderAdapterFactoryMy();
            itemAdapter = new StateMachineItemProviderMy(adapterFactory);
        }

        public String getText(Object element) {
            
            if (element instanceof StateMachine) {
                return itemAdapter.getText(element);
            }
            
            return null;
        }
    }
    
    protected class HazardSelectionListener implements ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            if (selection.size() > 0) {
                String name = ((StateMachine) selection.getFirstElement()).getName();
                System.out.println("Selection: " + name);
            }
        }
    }
}