/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package com.yworks.ygraphml.util.my;

import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMLHandler;

/**
 * Specialized version of SAXXMLHandler that allows ids to contain colons.
 * 
 * Usually IDs with colons inside are treated as qualified names and thus resolved differently. But yEd generates ids with colons in them,
 * and expects them to be treated like normal ids. So we modify the normal behavior to remove the check for colons inside the id string.
 */
public class SAXXMLHandlerMy extends SAXXMLHandler {

    public SAXXMLHandlerMy(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
        super(xmiResource, helper, options);
    }
    protected void setValueFromId(EObject object, EReference eReference, String ids)
    {
      StringTokenizer st = new StringTokenizer(ids);

      boolean isFirstID = true;
      boolean mustAdd = deferIDREFResolution;
      boolean mustAddOrNotOppositeIsMany = false;

      int size = 0;
      String qName = null;
      int position = 0;
      while (st.hasMoreTokens())
      {
        String id = st.nextToken();
        int index = id.indexOf('#', 0);
        if (index != -1)
        {
          if (index == 0)
          {
            id = id.substring(1);
          }
          else
          {
            Object oldAttributes = setAttributes(null);
            // Create a proxy in the correct way and pop it.
            //
            InternalEObject proxy =
              (InternalEObject)
                (qName == null ?
                   createObjectFromFeatureType(object, eReference) :
                   createObjectFromTypeName(object, qName, eReference));
            setAttributes(oldAttributes);
            if (proxy != null)
            {
              handleProxy(proxy, id);
            }
            objects.pop();
            types.pop();
            mixedTargets.pop();

            qName = null;
            ++position;
            continue;
          }
        }
// COMMENTED OUT START!
//        else if (id.indexOf(':', 0) != -1)
//        {
//          qName = id;
//          continue;
//        }
// COMMENTED OUT END!

        // Ensure that references corresponding to repeating elements are always deferred and processed in order at the end.
        //
        if (isFirstID && extendedMetaData != null && eReference.isMany() && extendedMetaData.getFeatureKind(eReference) == ExtendedMetaData.ELEMENT_FEATURE)
        {
          SingleReference ref = new SingleReference
                                     (object,
                                      eReference,
                                      id,
                                      -1,
                                      getLineNumber(),
                                      getColumnNumber());
          forwardSingleReferences.add(ref);
          return;
        }

        if (!deferIDREFResolution)
        {
          if (isFirstID)
          {
            EReference eOpposite = eReference.getEOpposite();
            if (eOpposite == null)
            {
              mustAdd = true;
              mustAddOrNotOppositeIsMany = true;
            }
            else
            {
              mustAdd = eOpposite.isTransient() || eReference.isMany();
              mustAddOrNotOppositeIsMany = mustAdd || !eOpposite.isMany();
            }
            isFirstID = false;
          }

          if (mustAddOrNotOppositeIsMany)
          {
            EObject resolvedEObject = xmlResource.getEObject(id);
            if (resolvedEObject != null)
            {
              setFeatureValue(object, eReference, resolvedEObject);
              qName = null;
              ++position;
              continue;
            }
          }
        }

        if (mustAdd)
        {
          if (size == capacity)
            growArrays();

          identifiers[size] = id;
          positions[size]   = position;
          ++size;
        }
        qName = null;
        ++position;
      }

      if (position == 0)
      {
        setFeatureValue(object, eReference, null, -2);
      }
      else if (size <= REFERENCE_THRESHOLD)
      {
        for (int i = 0; i < size; i++)
        {
          SingleReference ref = new SingleReference
                                     (object,
                                      eReference,
                                      identifiers[i],
                                      positions[i],
                                      getLineNumber(),
                                      getColumnNumber());
          forwardSingleReferences.add(ref);
        }
      }
      else
      {
        Object[] values = new Object[size];
        int[] currentPositions = new int[size];
        System.arraycopy(identifiers, 0, values, 0, size);
        System.arraycopy(positions, 0, currentPositions, 0, size);

        ManyReference ref = new ManyReference
                                   (object,
                                    eReference,
                                    values,
                                    currentPositions,
                                    getLineNumber(),
                                    getColumnNumber());
        forwardManyReferences.add(ref);
      }
    }
}