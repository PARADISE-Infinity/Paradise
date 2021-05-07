/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.petrinet.base.xtend;

import de.dlr.premise.element.ARepository;
import de.dlr.premise.element.Transition;
import de.dlr.premise.element.Mode;
import de.dlr.premise.registry.ANameItem;


/**
 * @author hschum
 *
 */
public interface IPetriNetContent {

    String getFileExtension();

    CharSequence createFileHeader(ARepository repository, String charset);

    CharSequence createFileFooter();

    CharSequence createPlace(Mode mode);

    int getMaxPriority();

    CharSequence createTransition(Transition transition, int priority);

    CharSequence createEdge(ANameItem source, ANameItem dest);
}
