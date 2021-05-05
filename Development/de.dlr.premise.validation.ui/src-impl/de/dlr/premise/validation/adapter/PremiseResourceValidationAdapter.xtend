/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.validation.adapter

import org.eclipse.core.runtime.jobs.Job
import org.eclipse.emf.common.notify.Notification
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EContentAdapter


/**
 * Validates a given Resource and its containing resource set. 
 * 
 * Validation is performed in the background, using the Eclipse jobs framework. To ensure that 
 * only one validation is running for a resource at any given time, all Jobs that work on 
 * validation share a family. Before a new validation run is started, all old jobs with the 
 * family are canceled
 */
class PremiseResourceValidationAdapter extends EContentAdapter {
	/**
     * Time to wait before starting validation after notification is received
     */
    val static int VALIDATION_DELAY = 1000;
	
	val Resource resource
	val ValidationJobFamily family
	
	new(Resource resource) {
		this.resource = resource
		this.family = new ValidationJobFamily(resource.URI)
	}
	
	override notifyChanged(Notification notification) {
        super.notifyChanged(notification)

        // only validated if notification reports a change
        if (!notification.touch && notification.feature != null) {
        	doValidate(notification)
        }
		
    }

	def synchronized doValidate() {
		doValidate(null)
	}
    
    def synchronized doValidate(Notification notification) {
		Job.getJobManager().cancel(family);
		new ValidationManagementJob(resource, family, notification).schedule(VALIDATION_DELAY)
	}
}