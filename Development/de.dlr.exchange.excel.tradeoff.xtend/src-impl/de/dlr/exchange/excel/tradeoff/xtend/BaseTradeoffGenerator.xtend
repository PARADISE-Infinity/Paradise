/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.tradeoff.xtend

import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.IOptions
import de.dlr.exchange.base.xtend.RepresentationHelper
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.functions.UseCaseRepository
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.ArrayList
import java.util.List
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Path
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.util.RuntimeIOException

/**
 * @author enge_do
 */
class BaseTradeoffGenerator {

	private RepresentationHelper repHelper

	public static String OPT_TEMPLATE_FILE = "Template"
	public static String OPT_REFERENCE_FILE = "Reference"

	public static String NAME_OUTPUT_FILE_ABSOLUTE = "TradeOffStudyAbsolute.xlsx"
	public static String NAME_OUTPUT_FILE_RELATIVE = "TradeOffStudyRelative.xlsx"

	/**
	 * This method generates the Excel-Trade-Off file
	 */
	def public void doGenerate(ResourceSet resSet, IFileSystemAccess fsa, IOptions options, boolean relative) {
		repHelper = RepresentationHelper.getInstance(resSet)
		var UseCaseRepository usecase
		var List<ProjectRepository> premises = new ArrayList<ProjectRepository>
		var List<String> solutionNames = new ArrayList<String>

		for (resource : resSet.resources) {
			if (resource.contents.length > 0) {
				var repo = resource.contents.get(0)
				if (repo instanceof ProjectRepository) {
					premises.add(repo)
					var filename = resource.URI.toString
					solutionNames.add(
						filename.substring(Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\")) + 1,
							filename.lastIndexOf(".")))
				} else if (repo instanceof UseCaseRepository) {
					// set Options in the corresponding usecase file
					usecase = repo
					options.setOptions(usecase)
				}
			}

		}
		var relativeTemplatePath = GeneratorHelper.getMetaDataValue(usecase, OPT_TEMPLATE_FILE)
		var referenceTemplatePath = getPathRelativeToWorkspace(resSet.resources.get(0))
		var relativeReferencePath = referenceTemplatePath +
			GeneratorHelper.getMetaDataValue(usecase, OPT_REFERENCE_FILE)

		// check whether the relativeTemplatePath is relative indeed and make it absolute
		if (!new Path(relativeTemplatePath).absolute) {
			var templateFile = ResourcesPlugin.getWorkspace.getRoot.getFile(
				new Path(referenceTemplatePath + relativeTemplatePath.replaceFirst("\\.\\.[/\\\\]", "")))
			if (!templateFile.exists) {
				templateFile = ResourcesPlugin.getWorkspace.getRoot.getFile(
					new Path(referenceTemplatePath + relativeTemplatePath))
			}
			try {
				relativeTemplatePath = templateFile.rawLocation.toOSString
			} catch (NullPointerException e) {
				throw new FileNotFoundException(templateFile.toString)
			}
		}

		try {
			var engine = new ExcelTradeOffEngine(relativeTemplatePath, relative)

			if (!relative) {
				// check wether the reference file exists
				if (!ResourcesPlugin.getWorkspace.root.getFile(new Path(relativeReferencePath)).exists) {
					println('''Could not find the Reference-System "«relativeReferencePath»"''')
					return
				}

				// get the ProjectRepository of the Reference-System
				var referenceResSet = new ResourceSetImpl()
				var referenceResource = referenceResSet.getResource(
					URI.createPlatformResourceURI(relativeReferencePath, true), true)

				EcoreUtil2.resolveAll(referenceResource)

				var referenceRepo = referenceResource.contents.get(0) as ProjectRepository
				engine.init(premises, solutionNames, referenceRepo)
			} else {
				engine.init(premises, solutionNames)
			}

			engine.calculate
			engine.fill

			var nameOutputFile = if(relative) NAME_OUTPUT_FILE_RELATIVE else NAME_OUTPUT_FILE_ABSOLUTE

			if (fsa instanceof InMemoryFileSystemAccess) { // still needed, the GenerationHandler uses it
				var out = engine.write() as ByteArrayOutputStream
				fsa.generateFile(GeneratorHelper::encodeFileName(nameOutputFile),
					new ByteArrayInputStream(out.toByteArray))
			} else if (fsa instanceof EclipseResourceFileSystemAccess2) {
				var out = engine.write() as ByteArrayOutputStream
				fsa.generateFile(nameOutputFile, new ByteArrayInputStream(out.toByteArray))
			}
		} catch (FileNotFoundException e) {
			println(
				'''
			
			Could not find the Template file. Please specify the correct location:
			«e.message»''')
		} catch (RuntimeIOException e) {
			println(
				'''
			
			«e.message»
			Have you got the right to write to that file or is it opened in another application?''')
		} catch (NumberFormatException e) {
			println(e.message)
		} catch (NoUseCaseFoundException e) {
			println(e.message)
		} finally {
			repHelper.dispose()
		}

	}

	/**
	 * get the relative path of the given {@link Resource}
	 * 
	 * @param resource the Resource
	 * @return the path as a String
	 */
	def String getPathRelativeToWorkspace(Resource resource) {
		var uri = resource.URI
		if ("platform" == uri.scheme && uri.segmentCount > 1 && "resource" == uri.segment(0)) {
			'''«FOR i : 1 ..< (uri.segmentCount - 1)»«uri.segment(i)»/«ENDFOR»'''
		}
	}
}
