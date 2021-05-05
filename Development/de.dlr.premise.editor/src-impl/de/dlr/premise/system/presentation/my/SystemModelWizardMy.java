/**
 * 
 */
package de.dlr.premise.system.presentation.my;

import java.util.ArrayList;
import java.util.Collection;

import de.dlr.premise.system.presentation.SystemModelWizard;

/**
 * @author hschum, berr_ae
 *
 */
public class SystemModelWizardMy extends SystemModelWizard {

	@Override
	protected Collection<String> getInitialObjectNames() {
		if (initialObjectNames == null) {
			initialObjectNames = new ArrayList<String>();
			initialObjectNames.add(systemPackage.getProjectRepository().getName());
		}
		return initialObjectNames;
	}
}
