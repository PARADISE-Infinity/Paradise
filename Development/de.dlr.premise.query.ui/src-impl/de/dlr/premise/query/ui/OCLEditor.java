/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.query.ui;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ocl.xtext.essentialocl.ui.internal.EssentialOCLActivator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;

import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class OCLEditor {

    private EmbeddedEditor handle;

    private OCLEditor(EmbeddedEditor handle) {
        this.handle = handle;
    }

    public static OCLEditor createEditor(Composite parent, EClassifier context) {
        Injector baseInjector =
                EssentialOCLActivator.getInstance().getInjector(EssentialOCLActivator.ORG_ECLIPSE_OCL_XTEXT_ESSENTIALOCL_ESSENTIALOCL);

        Injector injector = baseInjector.createChildInjector(binder -> {
            binder.bind(EClassifier.class).toInstance(context);
        });

        return internalCreateEditor(parent, injector);
    }

    private static OCLEditor internalCreateEditor(Composite parent, Injector injector) {

        IEditedResourceProvider provider = new OCLResourceProviderMy();
        injector.injectMembers(provider);
        EmbeddedEditorFactory factory = injector.getInstance(EmbeddedEditorFactory.class);
        EmbeddedEditor handle = factory.newEditor(provider).showErrorAndWarningAnnotations().withParent(parent);

        handle.getDocument().addDocumentListener(new IDocumentListener() {

            @Override
            public void documentChanged(DocumentEvent event) {
                // nothing to do here
            }

            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
                // nothing to do here
            }
        });
        handle.createPartialEditor();

        OCLEditor editor = new OCLEditor(handle);
        return editor;
    }

    public String getText() {
        return handle.getDocument().get();
    }

    public void setText(String text) {
        handle.getDocument().set(text);
    }

    public Control getControl() {
        return handle.getViewer().getControl();
    }

}
