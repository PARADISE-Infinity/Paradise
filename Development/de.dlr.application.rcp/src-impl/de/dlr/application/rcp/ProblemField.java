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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

public class ProblemField extends MarkerField {

	public ProblemField() {
		super();
	}

	@Override
	public String getValue(MarkerItem item) {
		return item.getAttributeValue(IMarker.MESSAGE, "Unnamed Marker");
	}

	public Image getImage(MarkerItem item) {
		// see
		// org.eclipse.emf.common.ui.DiagnosticComposite.createLabelProvider().getImage()
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		switch (item.getAttributeValue(IMarker.SEVERITY, IMarker.SEVERITY_INFO)) {
		case IMarker.SEVERITY_ERROR:
			return sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		case IMarker.SEVERITY_WARNING:
			return sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		default:
			return sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		}
	}

	public int getDefaultColumnWidth(Control control) {
		return 1000;
	}

	public void update(ViewerCell cell) {
		super.update(cell);

		MarkerItem item = (MarkerItem) cell.getElement();
		cell.setImage(getImage(item));
	}
}
