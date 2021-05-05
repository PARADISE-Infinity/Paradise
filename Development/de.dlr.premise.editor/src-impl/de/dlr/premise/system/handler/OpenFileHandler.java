package de.dlr.premise.system.handler;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import de.dlr.premise.registry.IPremiseObject;
import de.dlr.premise.registry.MetaData;

public class OpenFileHandler implements IHandler {
    
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		if (selection != null & selection instanceof IStructuredSelection) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object[] arrSelection = strucSelection.toArray();
			if ((arrSelection.length == 1) && ((arrSelection[0] instanceof EObject))) {

				if (arrSelection[0] instanceof MetaData) {
					MetaData metaData = (MetaData) arrSelection[0];
					openURI(metaData);
					openFile(metaData);
					openWindow(metaData);
				} else {
					
				    EObject target = (EObject) arrSelection[0];
					while (target != null && !(target instanceof IPremiseObject)) {
					    target = target.eContainer();
					}
	
					if (target != null) {
						for (MetaData md : ((IPremiseObject) target).getMetaData()) {
							if (openURI(md) || openFile(md)) {
								break;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private boolean openURI(MetaData md){
		try{
			URI uri = new URI(md.getValue());
			Desktop.getDesktop().browse(uri);
			return true;
		} catch (Exception e){
			return false;
		}
	}
	
	private boolean openFile(MetaData md) {
		try {
			File file = new File(md.getValue());
			if (!file.exists()) {
				// possible relative path
				IEditorInput input = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getActiveEditor().getEditorInput();
				IPath path = ((FileEditorInput) input).getPath();
				file = new File(path.removeLastSegments(1) + "/"
						+ md.getValue()).getCanonicalFile();
				if (!file.exists()) {
					throw new FileNotFoundException();
				}
			}
			Desktop.getDesktop().open(file);
			return true;
		} catch (Exception e) {
		    System.out.println(e);
			//ignore; MetaData contains no file link
		}
		
		return false;
	}
	
	private boolean openWindow(MetaData md)
	{
	    try{
	        String neu = md.getValue();
            if(neu.equals("Easter Egg"))
            { 
                MessageDialog.openInformation(null, "Was ist das?", "Geh weg! Hier gibt es keine versteckten Easter Eggs");
            }
            return true;
        } catch (Exception e){
            return false;
        }
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
