/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.parameterviewer;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Multimap;

import de.dlr.premise.constraints.valueconstraints.ConstraintToValueMapper;
import de.dlr.premise.constraints.valueconstraints.ValueConstraintChecker;
import de.dlr.premise.element.AModeCombination;
import de.dlr.premise.element.ARepository;
import de.dlr.premise.element.Mode;
import de.dlr.premise.functions.AConstraint;
import de.dlr.premise.functions.ARange;
import de.dlr.premise.functions.ModeRangeConstraint;
import de.dlr.premise.functions.RangeConstraint;
import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.registry.Constant;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.ABalancing;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.view.parameterviewer.data.ColumnValue;
import de.dlr.premise.xtext.naming.PremiseQualifiedNameProvider;

/**
 * TabelLabelProvider used for data representation in table
 */
public class TabelLabelProvider implements ITableLabelProvider, ITableColorProvider {

    private ValueConstraintChecker valueChecker = new ValueConstraintChecker();
    private ParameterViewerPage page;

    private Color noSatisfyColor = new Color(Display.getCurrent(), new RGB(255, 75, 75));
    private Color changedValueColor = new Color(Display.getCurrent(), new RGB(238, 230, 133));
    private Color targetColor = new Color(Display.getCurrent(), new RGB(240, 240, 240));
    private Color unchangeableColor = new Color(Display.getCurrent(), new RGB(100, 100, 100));
    private AdapterFactory adapterFactory;

    public TabelLabelProvider(ParameterViewerPage page) {
        super();
        this.page = page;
        adapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory();
    }

    @Override
    public void addListener(ILabelProviderListener listener) {

    }

    @Override
    public Color getForeground(Object element, int columnIndex) {
        // Grey font for unchangeable Columns
        switch (page.getColumns().get(columnIndex).id) {
        case CHANGES_ABS:
        case CHANGES_PERCENTAGE:
        case FULL_NAME:
        case OLD_VALUE:
        case SATISFIES:
        case SOURCE:
        case TARGET:
        case UNIT:
            return unchangeableColor;
        default:
            return null;
        }
    }

    @Override
    public Color getBackground(Object element, int columnIndex) {
        ConstraintToValueMapper mapper = new ConstraintToValueMapper();
        Parameter par = null;
        Value value = null;
        List<ModeValueRef> modeValueRefs = Collections.emptyList();

        if (element instanceof AValueDef && ((AValueDef) element).eContainer() instanceof Parameter) {
            par = (Parameter) ((AValueDef) element).eContainer();

            if (element instanceof Value) {
                value = (Value) element;
            }
            if (element instanceof ModeValueRef) {
                modeValueRefs = Collections.singletonList((ModeValueRef) element);
            }
        } else if (element instanceof Parameter) {
            par = (Parameter) element;
            value = par.getValue();
            modeValueRefs = par.getModeValues();
        }

        if (par != null && value != null) {
            // Mark unsatisfied Parameters and Values
            for (RequiredParameter reqPar : par.getSatisfiesRequiredParameters()) {
                if (isValueUnsatisfied(value, reqPar)) {
                    return noSatisfyColor;
                }
                for (ModeValueRef modeValueRef : modeValueRefs) {
                    Multimap<AConstraint, AValueDef> constraintToValueMap = mapper.createConstraintToValueMap(reqPar, par);
                    for (Entry<AConstraint, AValueDef> entry : constraintToValueMap.entries()) {
                        if (isModeValueRefUnsatisfied(entry, modeValueRef)) {
                            return noSatisfyColor;
                        }
                    }
                }
            }

            // Mark changed Parameters and Values
            if (page.getChangedValues().containsKey(value)) {
                return changedValueColor;
            }
            for (ModeValueRef modeVal : modeValueRefs) {
                if (page.getChangedValues().containsKey(modeVal)) {
                    return changedValueColor;
                }
            }
        }

        // Mark Balancing targets
        for (ABalancing<?> bal : page.getAllBalancings()) {
            if (element.equals(bal.getTarget())) {
                return targetColor;

            }
        }
        return null;
    }

    private boolean isValueUnsatisfied(Value element, RequiredParameter reqPar) {
        return reqPar.getValueConstraint() != null && !valueChecker.check(element, reqPar.getValueConstraint());
    }

    private boolean isModeValueRefUnsatisfied(Entry<AConstraint, AValueDef> entry, ModeValueRef modeVal) {
        return entry.getValue() == modeVal && !valueChecker.check(entry.getValue(), entry.getKey());
    }

    @Override
    public void dispose() {
        noSatisfyColor.dispose();
        changedValueColor.dispose();
        targetColor.dispose();
        unchangeableColor.dispose();
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
        if (page.getColumns().get(columnIndex).id == ColumnValue.ID.NAME) {
            if (adapterFactory.isFactoryForType(IItemLabelProvider.class)) {
                IItemLabelProvider labelProvider = (IItemLabelProvider) adapterFactory.adapt(element, IItemLabelProvider.class);
                if (labelProvider != null) {
                    Object image = labelProvider.getImage(element);
                    if (image instanceof Image) {
                        return (Image) image;
                    } else if (image instanceof URL) {
                        try {
                            return new Image(page.getSite().getShell().getDisplay(), ((URL) image).openStream());
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        // Fills problems array for Red background
        // Set color for other Cells too
        String satisfiesString = getSatisfiesColumnText(element);

        switch (page.getColumns().get(columnIndex).id) {
        case NAME:
            return getNameColumnText(element);
        case VALUE:
            return getValueColumnText(element);
        case OLD_VALUE:
            return getOldValueColumnText(element);
        case UNIT:
            return getUnitColumnText(element);
        case CHANGES_ABS:
            return getChangeAbsoluteColumnText(element);
        case CHANGES_PERCENTAGE:
            return getChangePercentageColumnText(element);
        case SOURCE:
            return getSourceColumnText(element);
        case TARGET:
            return getTargetColumnText(element);
        case SATISFIES:
            return satisfiesString;
        case UNCERTAINTY:
            return getUncertaintyColumnText(element);
        case FULL_NAME:
            return getFullQualifiedName(element);
        default:
            return null;
        }
    }

    private String getChangePercentageColumnText(Object element) {
        try {
            if (element instanceof AValueDef && page.getOldValues().containsKey(element)) {
                AValueDef val = (AValueDef) element;
                double newVal = Double.parseDouble(val.getValue());
                double oldVal = Double.parseDouble(page.getOldValues().get(element));
                if (oldVal != 0) {
                    if (((newVal / oldVal) * 100f) - 100f > 0)
                        return "+" + Double.toString(((newVal / oldVal) * 100f) - 100f) + "%";
                    else
                        return Double.toString(((newVal / oldVal) * 100f) - 100f) + "%";
                } else
                    return "-";

            }
        } catch (NumberFormatException e) {
        }
        return "";
    }

    private String getFullQualifiedName(Object element) {
        ANameItem item = null;
        if (element instanceof ANameItem) {
            item = (ANameItem) element;
        }
        if (element instanceof AValueDef) {
            item = (ANameItem) ((AValueDef) element).eContainer();
        }

        if (item == null) {
            return "";
        }
        return new PremiseQualifiedNameProvider().qualifiedName(item).toString();
    }

    private String getChangeAbsoluteColumnText(Object element) {
        try {
            if (element instanceof AValueDef && page.getOldValues().containsKey(element)) {
                AValueDef val = (AValueDef) element;

                double newVal = Double.parseDouble(val.getValue());
                double oldVal = Double.parseDouble(page.getOldValues().get(element));
                if (newVal - oldVal > 0)
                    return "+" + Double.toString(newVal - oldVal);
                else
                    return Double.toString(newVal - oldVal);
            }
        } catch (NumberFormatException e) {
        }
        return "";
    }

    private String getNameColumnText(Object element) {
        String retString = element.getClass().getSimpleName();
        if (element instanceof ARepository)
            retString = "Project Repository";
        if (element instanceof ANameItem) {
            retString = PremiseHelper.getQualifyingNamePrefix((ANameItem) element) + ((ANameItem) element).getName();
            if (element instanceof ABalancing<?>) {
                retString = PremiseHelper.getMeaningfulName((ANameItem) element);
            }
        } else if (element instanceof AValueDef) {
            retString = PremiseHelper.getQualifyingNamePrefix((ANameItem) ((AValueDef) element).eContainer());
            if (element instanceof Value) {
                retString += ((ANameItem) ((EObject) element).eContainer()).getName() + ".Value";
            } else if (element instanceof AModeCombination) {
                for (Iterator<Mode> iterator = ((AModeCombination) element).getModes().iterator(); iterator.hasNext();) {
                    retString += ((Mode) iterator.next()).getName();
                    if (iterator.hasNext())
                        retString += " & ";
                }
            }
        }

        return retString;
    }

    private String getSourceColumnText(Object element) {
        String retString = "";
        if (element instanceof AParameterDef) {
            for (ABalancing<?> bal : page.getAllBalancings()) {
                for (AParameterDef par : bal.getSources()) {
                    if (par.equals(element)) {
                        if (retString.equals("")) {
                            retString = PremiseHelper.getMeaningfulName(bal);
                        } else {
                            retString += " || " + PremiseHelper.getMeaningfulName(bal);
                        }
                        break;
                    }
                }
            }
        }

        return retString;
    }

    private String getTargetColumnText(Object element) {
        String retString = "";
        if (element instanceof AParameterDef) {
            for (ABalancing<?> bal : page.getAllBalancings()) {
                if (bal.getTarget() != null && bal.getTarget().equals(element)) {
                    retString += PremiseHelper.getMeaningfulName(bal) + " ";
                }
            }
        }
        return retString;
    }

    private String getUncertaintyColumnText(Object element) {
        if (element instanceof AValueDef) {
            return ((AValueDef) element).getUncertainty();
        } else if (element instanceof ModeValueRef) {
            return ((ModeValueRef) element).getUncertainty();
        }
        return "";
    }

    private String getValueColumnText(Object element) {
        if (element instanceof AValueDef)
            return ((AValueDef) element).getValue();
        return "";
    }

    private String getUnitColumnText(Object element) {
        if ((element instanceof AValueDef) && ((AParameterDef) (((AValueDef) element).eContainer())).getUnit() != null)
            return ((AParameterDef) ((AValueDef) element).eContainer()).getUnit().getSymbol();
        return "";
    }

    private String getOldValueColumnText(Object element) {
        if (page.getChangedValues().containsKey(element)) {
            return page.getChangedValues().get(element);
        }
        if (page.getOldValues().containsKey(element)) {
            return page.getOldValues().get(element);
        }
        return "";
    }

    private String getSatisfiesColumnText(Object element) {
        
        // Shows Satisfy Names in Parameter Row
        if (element instanceof AParameterDef) {
            if ((element instanceof Parameter)) {
                String retString = "";
                for (Iterator<RequiredParameter> iterator =
                        ((Parameter) element).getSatisfiesRequiredParameters().iterator(); iterator.hasNext();) {
                    RequiredParameter par = (RequiredParameter) iterator.next();
                    retString += par.getName();
                    if (iterator.hasNext()) {
                        retString += ", ";
                    }
                }
                return retString;
            }
        } else { // Shows Constraint in Value Row (the smallest created out of all)
            if (element instanceof AValueDef && !(((AValueDef) element).eContainer() instanceof Constant)
                    && !((Parameter) ((AValueDef) element).eContainer()).getSatisfiedSatisfieables().isEmpty()) {
                double lower = -Double.MAX_VALUE;
                double upper = Double.MAX_VALUE;
                Parameter par = (Parameter) ((EObject) element).eContainer();
                if (!par.getSatisfiedSatisfieables().isEmpty()) { // has Satisfies?
                    for (RequiredParameter satisfie : par.getSatisfiesRequiredParameters()) {
                        // limit out of all Required Parameter
                        double tempLower = -Double.MAX_VALUE;
                        double tempUpper = Double.MAX_VALUE;
                        if (element instanceof Value) {

                            ARange con = (RangeConstraint) satisfie.getValueConstraint();
                            if (con != null) {
                                try {
                                    if (!(con.getLower() == "") && !(con.getLower() == null)) {
                                        tempLower = Double.parseDouble(con.getLower());
                                    }
                                    if (!(con.getUpper() == "") && !(con.getUpper() == null)) {
                                        tempUpper = Double.parseDouble(con.getUpper());
                                    }
                                    if (tempLower > lower) {
                                        lower = tempLower;
                                    }
                                    if (tempUpper < upper) {
                                        upper = tempUpper;
                                    }
                                } catch (NumberFormatException e) {

                                }
                            }
                        } else if (element instanceof ModeValueRef) {
                            ConstraintToValueMapper mapper = new ConstraintToValueMapper();
                            Multimap<AConstraint, AValueDef> constraintToValueMap = mapper.createConstraintToValueMap(satisfie, par);
                            for (Entry<AConstraint, AValueDef> entry : constraintToValueMap.entries()) {
                                if (entry.getValue() == element) {
                                    ModeRangeConstraint con = (ModeRangeConstraint) entry.getKey();
                                    try {
                                        if (!(con.getLower() == "") && !(con.getLower() == null)) {
                                            tempLower = Double.parseDouble(con.getLower());
                                        }
                                        if (!(con.getUpper() == "") && !(con.getUpper() == null)) {
                                            tempUpper = Double.parseDouble(con.getUpper());
                                        }
                                        if (tempLower > lower) {
                                            lower = tempLower;
                                        }
                                        if (tempUpper < upper) {
                                            upper = tempUpper;
                                        }
                                    } catch (NumberFormatException e) {

                                    }
                                }
                            }
                        }
                    }
                }

                String strLower = "" + lower;
                if (lower == -Double.MAX_VALUE) {
                    strLower = "-Inf";
                }
                String strUpper = "" + upper;
                if (upper == Double.MAX_VALUE) {
                    strUpper = "+Inf";
                }

                if (upper < lower) {
                    return "No Intersection";
                }
                return "[" + strLower + "," + strUpper + "]";
            }
        }
        return "";

    }
}