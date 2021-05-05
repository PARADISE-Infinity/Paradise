/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calc.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.dlr.premise.system.presentation.my.EditorHelper;
import system.util.my.ValueChangedContentAdapter;

public class Recalculator extends AbstractHandler  {


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        recalculateAll(HandlerUtil.getActiveShell(event));
        return null;
    }
    
    public static void recalculateAll(Shell parent) {
        final IEditorReference[] editorRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        
        IRunnableWithProgress op = new IRunnableWithProgress() {
            
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                SubMonitor subMonitor = SubMonitor.convert(monitor, editorRefs.length);
                subMonitor.subTask("Recalculating all balancings");
                
                for (IEditorReference editorRef : editorRefs) {
                    IProgressMonitor childMonitor = subMonitor.newChild(1);
                    IEditorPart part = editorRef.getEditor(false);
                    
                    Resource resource = EditorHelper.getEMFResource(part);
                    if (resource != null) {
                        ResourceSet resSet = resource.getResourceSet();
                        
                        boolean didRecalculate = false;
                        for (Adapter adapter : resSet.eAdapters()) {
                            if (adapter instanceof ValueChangedContentAdapter) {
                                ((ValueChangedContentAdapter) adapter).recalculate(resSet, childMonitor);
                                didRecalculate = true;
                                break;
                            }
                        }
                        if (!didRecalculate) {
                            new ValueChangedContentAdapter().recalculate(resSet, childMonitor);
                        }
                    }
                    
                    if (childMonitor.isCanceled()) {
                        break;
                    }
                }
            }
        };
        
        try {
            new ProgressMonitorDialog(parent).run(true, true, op);
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
