/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.safetyview;

import java.io.IOException;
import java.net.URL;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.dlr.premise.element.AElement;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.Transition;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.safety.impl.my.FailureHelper;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.util.LabelHelper;
import de.dlr.premise.util.PremiseHelper;

/*
 * TabelLabelProvider used for data representation in table
 * */
public class TabelLabelProvider implements ITableLabelProvider, ITableColorProvider{


    private static final String N_A = "N.A.";

    private SafetyViewPage page;
    private Image useCaseImage;
    private Image systemComponentImage;
    private Image modeImage;

    private final String PLUGIN_NAME = "de.dlr.premise.edit";

    public TabelLabelProvider(SafetyViewPage page) {
        super();
        this.page = page;
        setImages();
    }

    private void setImages(){
     // set images
        URL url;
        try {
            url = new URL("platform:/plugin/" + PLUGIN_NAME + "/icons/full/obj16/Mode.gif");
            modeImage = new Image(page.getSite().getShell().getDisplay(), url.openStream());
            url = new URL("platform:/plugin/" + PLUGIN_NAME + "/icons/full/obj16/SystemComponent.gif");
            systemComponentImage = new Image(page.getSite().getShell().getDisplay(), url.openStream());
            url = new URL("platform:/plugin/" + PLUGIN_NAME + "/icons/full/obj16/UseCase.gif");
            useCaseImage = new Image(page.getSite().getShell().getDisplay(), url.openStream());
        } catch (IOException e) {
            System.err.println("Failed to load images");
        }

    }

    @Override
    public void addListener(ILabelProviderListener listener) {

    }

    @Override
    public Color getForeground(Object element, int columnIndex) {
        return null;
    }

    @Override
    public Color getBackground(Object element, int columnIndex) {

        if (element instanceof Transition && page.getHighlightBasicEvents()){
            try {
            Transition obsTrans = (Transition)element;
            if (obsTrans.getCondition() == null){
                Device device = Display.getCurrent();
                return new Color(device, 255, 240, 160);
            }
            } catch (IndexOutOfBoundsException e){}
        }



        return null;
    }

    @Override
    public void dispose() {
        modeImage.dispose();
        systemComponentImage.dispose();
        useCaseImage.dispose();
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {

    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        // set Icons for Column Name

        // state machine column
        if (page.columns.get(columnIndex).id == ColumnValue.ID.TARGET_MODE) {
            if (element instanceof Transition) {
                return modeImage;
            }

        // Component Column
        } else if (page.columns.get(columnIndex).id == ColumnValue.ID.COMPONENT
                && ((EObject) element).eContainer() instanceof StateMachine) {
            if (((EObject) element).eContainer().eContainer() instanceof SystemComponent) {
                return systemComponentImage;
            } else if (((EObject) element).eContainer().eContainer() instanceof UseCase) {
                return useCaseImage;
            }
        }

        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {

        switch (page.columns.get(columnIndex).id) {

            case NAME:
                String name = FailureHelper.getHazardName(element);
                return FailureHelper.getShortHazardName(name);

            case STATEMACHINE:
                return getHazardNameText(element);

            case FAILURE_RATE:
                return getValueColumnText(element);

            case COMPONENT:
                return getRootComponentText(element);

            case TARGET_MODE:
                return getTargetModeText(element);

            case TRANSITION:
                 return getTransitionText(element);

            default:
                return null;
        }
    }

    // display the name of the target mode of the first transition in a state machine
    private String getTargetModeText(Object element) {

        if (element instanceof Transition) {
            Transition trans = (Transition) element;
            Mode target = trans.getTarget();

            if (target != null) {
                return target.getName();
            }
        }

        return null;
    }

    // display the name of the transition of the first transition in a state machine
    private String getTransitionText(Object element) {

        if (element instanceof Transition) {
            return ((Transition)element).getName();
        }

        return null;
    }

    // display the name of container element
    private String getRootComponentText(Object element) {

        if (element instanceof Transition){
            AElement aelement = (AElement)((Transition)element).eContainer().eContainer();
            if (aelement != null) {
                String qualifiedName = PremiseHelper.getQualifyingNamePrefix(aelement) + aelement.getName();
                return LabelHelper.cleanName(qualifiedName);
            }
        }

        return null;
    }

    // display the name of container element
    private String getHazardNameText(Object element) {
        String retString = "Add: Hazard Name";
        if (element instanceof Transition) {
            retString = FailureHelper.getHazardName(element);
        }

        return retString;
    }

    // display the value of the transition parameter
    private String getValueColumnText(Object element) {

        if (element instanceof Transition) {
            // get the first transition parameter value in the first transition of the state machine
            try {
                AParameterDef param = ((Transition) element).getParameters().get(0);
                return PremiseHelper.getStrValue(param.getValue());
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                return N_A;
            }
        }
        return "";
    }
}