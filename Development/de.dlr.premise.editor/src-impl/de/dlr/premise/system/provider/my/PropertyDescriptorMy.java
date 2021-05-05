/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.provider.my;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.WrapperItemProvider;
import org.eclipse.emf.edit.ui.provider.PropertyDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

import de.dlr.premise.system.extensionpoints.ICustomCellEditor;

final class PropertyDescriptorMy extends PropertyDescriptor {

    private final class CustomCellEditorSafeRunnable implements ISafeRunnable {

        private final ICustomCellEditor customCellEditor;
        private final Composite composite;
        private final Object feature;
        private CellEditor editor = null;

        private CustomCellEditorSafeRunnable(ICustomCellEditor customCellEditor, Composite composite, Object feature) {
            this.customCellEditor = customCellEditor;
            this.composite = composite;
            this.feature = feature;
        }

        @Override
        public void run() throws Exception {
            if (customCellEditor.appliesForFeature(feature)) {
                Object targetObject;
                if (object instanceof ComponentReferenceChildrenDelegatingWrapperItemProvider) {
                    targetObject = ((ComponentReferenceChildrenDelegatingWrapperItemProvider) object).getDelegateValue();
                } else if (object instanceof WrapperItemProvider) {
                    targetObject = ((WrapperItemProvider) object).getValue();
                } else {
                    targetObject = object;
                }
                ILabelProvider labelProvider = PropertyDescriptorMy.this.getLabelProvider();
                editor = customCellEditor.createPropertyEditor(targetObject, composite, labelProvider);
            }
        }

        @Override
        public void handleException(Throwable e) {
            System.out.println(e.getMessage());
        }

        public CellEditor getEditor() {
            return editor;
        }
    }

    public PropertyDescriptorMy(Object object, IItemPropertyDescriptor itemPropertyDescriptor) {
        super(object, itemPropertyDescriptor);
    }

    public CellEditor createPropertyEditor(Composite composite) {
        if (!itemPropertyDescriptor.canSetProperty(object)) {
            return null;
        }
        
        Object feature = itemPropertyDescriptor.getFeature(object);

        IConfigurationElement[] config =
                Platform.getExtensionRegistry().getConfigurationElementsFor("de.dlr.premise.editor.customCellEditor");

        try {
            for (IConfigurationElement e : config) {
                Object o = e.createExecutableExtension("class");
                if (o instanceof ICustomCellEditor) {
                    CustomCellEditorSafeRunnable runnable = new CustomCellEditorSafeRunnable((ICustomCellEditor) o, composite, feature);
                    SafeRunner.run(runnable);
                    if (runnable.getEditor() != null) {
                        return runnable.getEditor();
                    }
                }
            }
        } catch (CoreException e) {
            System.out.println(e.getMessage());
        }

        return super.createPropertyEditor(composite);

    }
}