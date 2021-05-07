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

import org.eclipse.core.resources.WorkspaceJob
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.ISchedulingRule
import org.eclipse.jface.operation.IRunnableWithProgress
import org.eclipse.xtend.lib.annotations.Data

class MarkersWorkspaceJob extends WorkspaceJob {
	val ValidationJobFamily family
	val IRunnableWithProgress toRun
	
	new(ValidationJobFamily family, IRunnableWithProgress toRun){
		super("Updating validation markers")
		this.family = family
		this.toRun = toRun
		
		this.rule = new FamilyMutexRule(family)
	}
				
	override runInWorkspace(IProgressMonitor monitor) throws CoreException {
		toRun.run(monitor)
		return Status.OK_STATUS
	}
	
	override belongsTo(Object otherFamily) {
		family == otherFamily
	}
	
	@Data static class FamilyMutexRule implements ISchedulingRule {
		val ValidationJobFamily family
		
		override contains(ISchedulingRule other) {
			other.conflicting
		}
		
		override isConflicting(ISchedulingRule other) {
			switch (other) {
				FamilyMutexRule: other.family == family
				default: false
			}
		}
    	
    }
	
}