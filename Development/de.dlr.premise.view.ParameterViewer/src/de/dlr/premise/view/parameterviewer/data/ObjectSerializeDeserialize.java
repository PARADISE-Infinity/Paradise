/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.parameterviewer.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * Make text out of any Object and the other way, if text was generated with this class
 * 
 * @author Chris Carstensen
 */
public class ObjectSerializeDeserialize {

    public static String serialize(Object obj) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArray);
            objectOutputStream.writeObject(obj);
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return new String(Base64.getEncoder().encode(byteArray.toByteArray()));
    }

    public static Object deserialize(String str) {
        byte[] byteArray = Base64.getDecoder().decode(str.getBytes());
        Object o;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(byteArray));
            o = ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return o;
    }
}