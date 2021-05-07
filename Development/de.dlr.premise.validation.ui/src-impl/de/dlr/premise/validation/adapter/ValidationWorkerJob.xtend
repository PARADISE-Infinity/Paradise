/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.validation.adapter

import java.util.List
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.emf.common.util.BasicDiagnostic
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EValidator
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.ocl.xtext.oclinecore.validation.OCLinEcoreEObjectValidator
import org.eclipse.xtend.lib.annotations.Accessors

/**
 * Validates a given batch set of EObjects. When the validation is finished, deletes all existing 
 * validation markers for these EObjects and adds any markers.
 */
class ValidationWorkerJob extends Job {
	val Resource resource
	val List<EObject> toValidate
	val ValidationJobFamily family
	
	val EValidator validator
	val extension MarkerHelperMy markerHelper
	
	val BasicDiagnostic diagnostics
	
	@Accessors(PUBLIC_GETTER) var processedCount = 0
	
	
	new(Resource resource, List<EObject> toValidate, ValidationJobFamily family) {
		super("Validating")
		this.resource = resource
		this.toValidate = toValidate
		this.family = family
		
		this.validator = new OCLinEcoreEObjectValidator()
		this.markerHelper = new MarkerHelperMy()
		
		this.diagnostics = new BasicDiagnostic()
		
		this.system = true
	}
	
	override protected run(IProgressMonitor monitor) {	
		val submonitor = SubMonitor.convert(monitor, "Validating", toValidate.size)
		val context = newHashMap
		
		for (eObj : toValidate) {
			submonitor.checkCanceled()
			try {
				validator.validate(eObj.eClass, eObj, diagnostics, context)
			} catch (NullPointerException e) {
				// see https://trello.com/c/5XHx6im9/247-nullpointer-bei-validierung
				// ignore to prevent user-visible error
			}
			submonitor.worked(1)
			processedCount++
		}
		
		val uris = toValidate.map[EcoreUtil.getURI(it).toString].toSet
					
		new MarkersWorkspaceJob(family)[m | 
			resource.deleteMarkers[uris.contains(getAttribute(EValidator.URI_ATTRIBUTE))]
			
			for (diagonstic : diagnostics.children) {
				try {
					resource.createMarkers(diagonstic)
				} catch (Throwable t) {
					// ignore
				}
			}
		].schedule()
		
		submonitor.done() 
		
		return Status.OK_STATUS
	}
	
	override belongsTo(Object otherFamily) {
		family == otherFamily
	}
	
}