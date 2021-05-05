/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.registry.impl.my;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.dlr.premise.registry.Constant;
import de.dlr.premise.registry.MetaTypeDef;
import de.dlr.premise.registry.Note;
import de.dlr.premise.registry.Registry;
import de.dlr.premise.registry.impl.RegistryFactoryImpl;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 * 
 */
public class RegistryFactoryImplMy extends RegistryFactoryImpl {

	@Override
	public Registry createRegistry() {
		Registry dataItem = super.createRegistry();
        // EMF does not serialize/save default values of attributes, so, get and set it explicitly to ensure serialization:
		dataItem.setMetaModel(dataItem.getMetaModel());
		return dataItem;
	}
	
    @Override
    public Constant createConstant() {
        Constant dataItem = super.createConstant();
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }

    @Override
    public MetaTypeDef createMetaTypeDef() {
        MetaTypeDef dataItem = super.createMetaTypeDef();
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }

    @Override
    public Note createNote() {

        // create note
        Note note = super.createNote();

        // set author name
        String author = System.getProperty("user.name");
        note.setAuthor(author);

        // set creation date and time
        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(dt);
        note.setDate(date);

        return note;
    }
}
