/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.application.rcp;

import java.net.URL;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.framework.Bundle;

@SuppressWarnings({"restriction" })
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "com.example.test"; //$NON-NLS-1$

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	@Override
    public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}
	
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		configurer.setSaveAndRestore(true);
		IDE.registerAdapters();
		declareWorkbenchImages();       
	}
	
	@Override
	public IAdaptable getDefaultPageInput() {
		return ResourcesPlugin.getWorkspace().getRoot();         
	}
	
	/**
	* Declares all IDE-specific workbench images. This includes both "shared"
	* images (named in {@link IDE.SharedImages}) and internal images (named in
	* {@link org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages}).
	*
	* @see IWorkbenchConfigurer#declareImage
	*/
	private void declareWorkbenchImages() {

		final String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$
		final String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; // Enabled //$NON-NLS-1$
	
		final String PATH_OBJECT = ICONS_PATH + "obj16/"; // Model object //$NON-NLS-1$
		
		Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);
	
		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT,
                              PATH_OBJECT + "prj_obj.png", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle,
		IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, PATH_OBJECT
                                      + "cprj_obj.png", //$NON-NLS-1$
                              true);
		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OPEN_MARKER,
                              PATH_ELOCALTOOL + "gotoobj_tsk.png", true); //$NON-NLS-1$
	
	}

	/**
	* Declares an IDE-specific workbench image.
	*
	* @param symbolicName
	*            the symbolic name of the image
	* @param path
	*            the path of the image file; this path is relative to the base
	*            of the IDE plug-in
	* @param shared
	*            <code>true</code> if this is a shared image, and
	*            <code>false</code> if this is not a shared image
	* @see IWorkbenchConfigurer#declareImage
	*/
	private void declareWorkbenchImage(Bundle ideBundle, String symbolicName, String path, boolean shared) {
		URL url = FileLocator.find(ideBundle, new Path(path), null);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		getWorkbenchConfigurer().declareImage(symbolicName, desc, shared);
	}
		
}
