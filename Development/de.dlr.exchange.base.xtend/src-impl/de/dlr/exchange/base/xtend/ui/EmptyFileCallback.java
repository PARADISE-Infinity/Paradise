/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.base.xtend.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;

/**
 * @author hschum
 *
 */
public class EmptyFileCallback implements EclipseResourceFileSystemAccess2.IFileCallback {

	@Override
	public void afterFileUpdate(IFile file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterFileCreation(IFile file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean beforeFileDeletion(IFile file) {
		// TODO Auto-generated method stub
		return false;
	}
}
