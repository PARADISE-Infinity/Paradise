/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block.wizard

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job

class BackgroundHelper {
	
	def public static inBackgroundJob(() => void fn) {
		val job = new Job("Update exporter settings") {
			override protected run(IProgressMonitor monitor) {
				fn.apply()
				return Status.OK_STATUS
			}
		}
		job.priority = Job.SHORT
		job.schedule()
	}
}