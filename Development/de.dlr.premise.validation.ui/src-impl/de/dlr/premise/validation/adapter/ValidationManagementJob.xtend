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

import com.sun.istack.internal.Nullable
import de.dlr.premise.system.ComponentReference
import java.util.Collections
import java.util.List
import java.util.Map
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.core.runtime.jobs.JobGroup
import org.eclipse.emf.common.notify.Notification
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EValidator
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EcoreUtil

import static extension de.dlr.premise.util.PremiseHelper.closure
import static extension de.dlr.premise.util.PremiseHelper.eAllContentsIncludingRoot
import static extension de.dlr.premise.util.PremiseHelper.flatMap

/**
 * Manages validation of one resource.
 * 
 * The job determines all EObjects in the resource's resource set that need to be validated. The EObjects are batched up and the 
 * batches are validated in parallel.
 */
class ValidationManagementJob extends Job {
	val static OBJECTS_PER_JOB = 200
	
	val Resource resource
	val ValidationJobFamily family
	@Nullable val Notification notification

	val extension MarkerHelperMy markerHelper
	
	new(Resource resource, ValidationJobFamily family, @Nullable Notification notification) {
		super('''Validating «resource.URI»''')
		this.resource = resource
		this.family = family
		this.notification = notification
		
		this.markerHelper = new MarkerHelperMy()
	}
	
	override protected run(IProgressMonitor monitor) {
		val submonitor = SubMonitor.convert(monitor)
        
		val allToValidate = getEObjectsToValidate()
        
        // randomize order to better distribute objects that need different time to validate between the jobs 
        Collections.shuffle(allToValidate)
        
        if (notification != null) {
        	prioritzeChanged(allToValidate)
        }
        
        val elementCount = allToValidate.size
        submonitor.beginTask('''Validating «resource.URI»''', elementCount)
          
		val jobCount = Math.ceil((elementCount as float) / OBJECTS_PER_JOB) as int
        val group = new JobGroup('''Validating «resource.URI»''', Runtime.runtime.availableProcessors / 2, jobCount)
        
        // start the jobs
        val jobs = newArrayList
        for (i : 0..<jobCount) {
        	val start = i * OBJECTS_PER_JOB
        	val end = Math.min(allToValidate.size, start + OBJECTS_PER_JOB - 1)
        	
        	val job = new ValidationWorkerJob(resource, allToValidate.subList(start, end), family)
        	job.jobGroup = group
    		job.schedule()
    		jobs.add(job)
        }
        
        // To report progress accurately we periodically poll the worker jobs
        val updateProgressJob = new Job("Update validation progress"){
        	val Map<Job, Integer> oldProcessedByJob = newHashMap
									
			override protected run(IProgressMonitor updateProgressJobMonitor) {
				updateProgressJobMonitor.beginTask("Update validation progress", IProgressMonitor.UNKNOWN)
				while(true) {
					Thread.sleep(300)
					var newProcessed = 0
					for(job : jobs) {
						val jobProcessed = job.processedCount
						newProcessed += jobProcessed - oldProcessedByJob.getOrDefault(job, 0)
						oldProcessedByJob.put(job, jobProcessed)
					}
					submonitor.worked(newProcessed)
					
					// If the ValidationManagementJob was canceled, we want to cancel all workers
					if (submonitor.canceled) {
						group.cancel()
						return Status.OK_STATUS
					}
					// After validation, this job will be canceled and should respond accordingly
					if (updateProgressJobMonitor.canceled) {
						return Status.OK_STATUS
					}
				} 
			}
        	
			override belongsTo(Object otherFamily) {
				family == otherFamily
			}
        }
        updateProgressJob.system = true
        updateProgressJob.schedule()

		group.join(0, null)
		submonitor.checkCanceled()
		        
        // delete all markers of objects that were not validated (e.g. deleted objects)
		val uris = allToValidate.map[EcoreUtil.getURI(it).toString].toSet
		new MarkersWorkspaceJob(family)[m | 
			resource.deleteMarkers[!uris.contains(getAttribute(EValidator.URI_ATTRIBUTE))]
		].schedule()
                
		return Status.OK_STATUS
	}
	
	/**
	 * Get EObjects that will be validated.
	 * 
	 * For most PREMISE file types all contents are validated. To conserve resources, referenced library files (.component) are
	 * only validated partially: Validation is restricted to parts of the library that are used elsewhere in the model.
	 */
	def private getEObjectsToValidate() {
		// validate all resources, except .component
		val resources = resource.resourceSet.resources.filter[URI.fileExtension != "ecore" && URI.fileExtension != "component"].toList
		// the resource itself is always validated even if it is a .component, we want library to be validated completely if it is opened alone, not referenced
		resources.add(resource)
        
        // get resource contents
       	resources.iterator
        	.flatMap[allContents]
        	.toIterable
        	// add used parts of library
        	.closure[obj |
            	if (obj instanceof ComponentReference) {
            		#[obj.activeImplementation, obj.componentDefinition].filterNull.flatMap[eAllContentsIncludingRoot.toIterable]
            	} else {
            		#[]
            	}
            ]
            // remove duplicates, then create list to validate in order
            .toSet.toList
	}
	
	
	/**
	 * Re-order toValidate list to put changed objects in front.
	 * 
	 * To provide more immediate feedback to the user, objects that were changed recently are validated first.
	 */
	protected def List<EObject> prioritzeChanged(List<EObject> allToValidate) {
		val notifier = notification.notifier
    	val notifierChildren = if (notifier instanceof EObject) notifier.eContents else #[]
    	val notifierResource = switch (notifier) {
    		EObject: notifier.eResource
    		Resource: notifier
    		default: null
    	}
    	allToValidate.sortInplaceBy[
    		if (it == notification.notifier) {
    			1
    		} else if (notifierChildren.contains(it)) {
    			2
    		} else if (eResource == notifierResource) {
    			3
    		} else {
    			Integer.MAX_VALUE
    		}
    	]
	}
	
	override belongsTo(Object otherFamily) {
		family == otherFamily
	}
}