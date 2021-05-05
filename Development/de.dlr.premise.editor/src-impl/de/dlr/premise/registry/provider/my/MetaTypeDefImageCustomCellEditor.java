/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.registry.provider.my;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.ui.celleditor.ExtendedDialogCellEditor;
import org.eclipse.emf.common.ui.dialogs.ResourceDialog;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.google.common.io.Files;

import de.dlr.premise.registry.MetaTypeDef;
import de.dlr.premise.registry.RegistryPackage;
import de.dlr.premise.system.extensionpoints.ICustomCellEditor;

public class MetaTypeDefImageCustomCellEditor implements ICustomCellEditor {

    @Override
    public boolean appliesForFeature(Object feature) {
        return feature == RegistryPackage.Literals.META_TYPE_DEF__ICON;
    }

    @Override
    public CellEditor createPropertyEditor(Object object, Composite composite, ILabelProvider propertyLabelProvider) {
        final MetaTypeDef mtd = (MetaTypeDef) object;
        final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(mtd);
        final URI baseUri = mtd.eResource().getURI();

        return new ExtendedDialogCellEditor(composite, propertyLabelProvider) {

            @Override
            protected Object openDialogBox(Control cellEditorWindow) {
                ResourceDialog resourceDialog =
                        new ResourceDialog(cellEditorWindow.getShell(), "Select icon file", SWT.OPEN & SWT.SINGLE, baseUri);

                resourceDialog.open();

                URI iconUri;
                try {
                    iconUri = resourceDialog.getURIs().get(0);
                } catch (NullPointerException e) {
                    List<Command> cl = new ArrayList<Command>();
                    cl.add(SetCommand.create(domain, mtd, RegistryPackage.Literals.META_TYPE_DEF__ICON_BASE64,
                                             RegistryPackage.Literals.META_TYPE_DEF__ICON_BASE64.getDefaultValueLiteral()));
                    cl.add(SetCommand.create(domain, mtd, RegistryPackage.Literals.META_TYPE_DEF__ICON,
                                             RegistryPackage.Literals.META_TYPE_DEF__ICON.getDefaultValueLiteral()));
                    return new CompoundCommand(cl);
                }

                try {
                    URI resolvedIconUri = resolveUri(mtd, iconUri);

                    String imagePath = resolvedIconUri.toFileString();

                    // Try encoding image, also checks whether path is valid
                    String imageEncoded;
                    try {
                        imageEncoded = DatatypeConverter.printBase64Binary(Files.toByteArray(new File(imagePath)));
                    } catch (IOException e) {
                        throw new MetaTypeDefImageException("File not found!");
                    }

                    // load image to check whether it is valid
                    GC g;
                    try {
                        ImageData[] data = (new ImageLoader()).load(imagePath);
                        g = new GC(new Image(Display.getCurrent(), data[0]));
                    } catch (SWTException e) {
                        throw new MetaTypeDefImageException("File invalid!");
                    }

                    // Check image size
                    if (g.getClipping().width >= 64 || g.getClipping().height >= 64) {
                        throw new MetaTypeDefImageException("Image file to large!\nImages must not be larger than 64 pixels by 64 pixels.");
                    }

                    // Create uri to display
                    String displayIconPath;
                    if (iconUri.isPlatform()) {
                        displayIconPath = iconUri.deresolve(mtd.eResource().getURI()).toString();
                    } else {
                        displayIconPath = resolvedIconUri.toFileString();
                    }

                    List<Command> cl = new ArrayList<Command>();
                    cl.add(SetCommand.create(domain, mtd, RegistryPackage.Literals.META_TYPE_DEF__ICON_BASE64, imageEncoded));
                    cl.add(SetCommand.create(domain, mtd, RegistryPackage.Literals.META_TYPE_DEF__ICON, "Icon integrated into registry ("
                            + displayIconPath + ")"));
                    return new CompoundCommand(cl);
                } catch (MetaTypeDefImageException e) {
                    MessageDialog.openError(cellEditorWindow.getShell(), "Error", e.getMessage());
                    return new CompoundCommand();
                }

            }
        };
    }

    private URI resolveUri(final MetaTypeDef mtd, URI iconUri) throws MetaTypeDefImageException {
        URI resolvedIconUri = iconUri;
        if (resolvedIconUri.isRelative()) {
            resolvedIconUri = resolvedIconUri.resolve(mtd.eResource().getURI());
        }
        if (!resolvedIconUri.isFile()) {
            resolvedIconUri = CommonPlugin.resolve(resolvedIconUri);
        }
        if (!resolvedIconUri.isFile()) {
            throw new MetaTypeDefImageException("File not found!\nSpecify a file URL or a relative path!");
        }
        return resolvedIconUri;
    }

    private class MetaTypeDefImageException extends Exception {

        private static final long serialVersionUID = 8568445404609900807L;

        public MetaTypeDefImageException(String string) {
            super(string);
        }
    }

}
