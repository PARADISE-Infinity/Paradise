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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.registry.MetaTypeDef;

/**
 *
 */
public class ImageHelper implements IDisposable {
    
    private Map<String, Image> imageCache = new HashMap<>();

    /**
     * Get the MetaType-Image for model elements.
     * 
     * @param object The model element to get the image for. Has to be an instance of {@link IMetaTypable}.
     * @param fallbackImage The default image in case there is no metaType defined.
     * @return The icon of the metaType, if it exists, else the fallbackImage.
     */
    public Object getImage(IMetaTypable object, Object fallbackImage) {
        IMetaTypable metaTypable = object;
        Object image = null;

        if (metaTypable.getMetaTypes().size() > 0) {
            MetaTypeDef mt = metaTypable.getMetaTypes().get(0);

            image = getImage(mt);
        }

        if (image == null) {
            image = fallbackImage;
        }
        return image;
    }

    /**
     * Get the icon of the given {@link MetaTypeDef}, if it exists.
     * 
     * @param metaTypeDef
     * @return The icon.
     */
    public Object getImage(MetaTypeDef metaTypeDef) {
        Object image = null;
        try {
            String imageBase64 = metaTypeDef.getIconBase64();

            image = getImage(imageBase64);
        } catch (Exception e) {
            // just ignore them...
        }

        return image;
    }

    private Image getImage(String imageBase64) {
        if (imageCache.get(imageBase64) == null) {
            imageCache.put(imageBase64, createImage(imageBase64));
        }
        
        return imageCache.get(imageBase64);
    }

    private Image createImage(String imageBase64) {
        byte[] bytes = DatatypeConverter.parseBase64Binary(imageBase64);
        ImageData[] data = (new ImageLoader()).load(new ByteArrayInputStream(bytes));

        return new Image(Display.getCurrent(), data[0]);
    }

    @Override
    public void dispose() {
        for (Image image : imageCache.values()) {
            image.dispose();
        }
    }
}
