/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.provider.util;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.CopyCommand.Helper;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.IItemStyledLabelProvider;
import org.eclipse.emf.edit.provider.IUpdateableItemText;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import de.dlr.premise.registry.provider.commands.InitializeCopyNewUUIDCommand;

public class ItemProviderAdapterMy extends ItemProviderAdapter implements IItemStyledLabelProvider, IUpdateableItemText {
	
    protected ImageHelper imageHelper = new ImageHelper();
    
    public ItemProviderAdapterMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
    @Override
    public void dispose() {
        super.dispose();
        imageHelper.dispose();
    }
    
    @Override
    protected Command createInitializeCopyCommand(EditingDomain domain, EObject owner, Helper helper) {
        return new InitializeCopyNewUUIDCommand(domain, owner, helper);
    }

	/**
	 * Use the customized property descriptor that checks for an ocl operations that provides filtered values
	 */
	@Override
	protected ItemPropertyDescriptor createItemPropertyDescriptor(
		AdapterFactory adapterFactory,
		ResourceLocator resourceLocator,
		String displayName,
		String description,
		EStructuralFeature feature,
		boolean isSettable,
		boolean multiLine,
		boolean sortChoices,
		Object staticImage,
		String category,
		String[] filterFlags
	) {
		return new ItemPropertyDescriptorMy(
			adapterFactory,
			resourceLocator,
			displayName,
			description,
			feature,
			isSettable,
			multiLine,
			sortChoices,
			staticImage,
			category,
			filterFlags
		);
	}

    @Override
    /**
     * This implements {@link IUpdateableItemText#getUpdateableText} using the primary attribute.
     */
    public String getUpdateableText(Object object) {
        if (!(object instanceof EObject)) {
            return null;
        }
        EObject eObject = (EObject) object;
        
        EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(eObject);
        if (editingDomain != null && editingDomain.isReadOnly(eObject.eResource())) {
            return null;
        }
        
        EAttribute primaryAttribute = getPrimaryAttribute(eObject);
        if (primaryAttribute == null) {
            return null;
        }

        Object primaryAttributeValue = eObject.eGet(primaryAttribute);

        return primaryAttributeValue instanceof String ? (String) primaryAttributeValue : null;
    }

    /**
     * This implements {@link IUpdateableItemText#setText}. This allows client classes to implement the interface
     * {@link IUpdateableItemText} by simply providing a primary attribute that it to be edited.
     * 
     * @param object
     * @param text
     */
    public void setText(Object object, String text) {
        if (!(object instanceof EObject)) {
            return;
        }
        EObject eObject = (EObject) object;
        EAttribute primaryAttribute = getPrimaryAttribute(eObject);
        if (primaryAttribute == null) {
            return;
        }

        Object oldValue = eObject.eGet(primaryAttribute);
        Object newValue = convertToPrimaryAttributeType(text);

        if (!newValue.equals(oldValue)) {
            IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (activeEditor instanceof IEditingDomainProvider) {
                EditingDomain editingDomain = ((IEditingDomainProvider) activeEditor).getEditingDomain();
                editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, eObject, primaryAttribute, newValue));
            } else {
                eObject.eSet(primaryAttribute, newValue);
            }
        }
    }
    
    /**
     * Gets a primary attribute of a class.
     * 
     * Uses {@link ItemProviderAdapterMy#getPrimaryAttribute()) first. If that is not defined, defaults to an attribute named "name", if
     * that exists.
     *
     * @param object
     * @return
     */
    private EAttribute getPrimaryAttribute(EObject object)  {
        if (getPrimaryAttribute() != null) {
            return getPrimaryAttribute();
        }
        for (EAttribute attr :  object.eClass().getEAllAttributes()) {
            if (attr.getName().equals("name")) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Returns the primary attribute of a class.
     * 
     * To be overridden by client classes. Gets used for the implementation of {@link IUpdateableItemText}
     * 
     * @return The primary {@link EAttribute} of a class.
     */
    protected EAttribute getPrimaryAttribute() {
        return null;
    }

    /**
     * Convert the string entered by the user to the type of the primary attribute.
     * 
     * Implement this if your primary attribute is not of type {@link String}.
     * 
     * @param text
     * @return
     */
    protected Object convertToPrimaryAttributeType(String text) {
        return text;
    }


    @Override
    protected Object createWrapper(EObject object, EStructuralFeature feature, Object value, int index) {
        if (isWrappingNeeded(object) && feature instanceof EReference && !((EReference) feature).isContainment()) {
            return createWrapperForNonContainmentEReference(object, feature, value, index);
        }

        return super.createWrapper(object, feature, value, index);
    }

    protected Object createWrapperForNonContainmentEReference(EObject object, EStructuralFeature feature, Object value, int index) {
        return new DelegatingWrapperItemProviderMy(value, object, feature, index, adapterFactory);
    }
}
