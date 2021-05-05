/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.dsl.ui

import com.google.inject.Inject
import de.dlr.calc.engine.dsl.BalancingChangeCommandCreator
import de.dlr.calc.engine.dsl.DocumentCreator
import de.dlr.calc.engine.dsl.ParameterRenamer
import de.dlr.premise.presentation.util.DefaultSizeDialog
import de.dlr.calc.engine.dsl.xtext.calcDsl.Model
import de.dlr.calc.engine.dsl.xtext.ui.internal.CalcDslActivator
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern
import org.eclipse.emf.edit.command.ChangeCommand
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Shell
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator
import de.dlr.premise.system.Balancing
import de.dlr.calc.engine.dsl.scope.IBiMapBackedBalancingScope
import de.dlr.calc.engine.dsl.BalancingScopeFactory

class CalcDialog extends DefaultSizeDialog {
	
	def static getNewInstance(Shell parentShell, Balancing balancing) {
		// This is a facade that hides DI for the XtextDialog from clients
		val activator = CalcDslActivator::getInstance()
		val injector = activator.getInjector(CalcDslActivator::DE_DLR_CALC_ENGINE_DSL_XTEXT_CALCDSL)
		val dialogInjector = injector.createChildInjector[binder | 
			binder.bind(Shell).toInstance(parentShell)
			binder.bind(Balancing).toInstance(balancing)
		]
		dialogInjector.getInstance(CalcDialog)
	}
	
	
	@Inject val EmbeddedEditorFactory factory = null
	@Inject val EditedResourceProviderMy resourceProvider = null
	@Inject val IResourceValidator validator = null
	@Inject val ParameterRenamer parameterRenamer = null
	val balancingScopeMapCreator = new BalancingScopeFactory()
	
	@Inject val Balancing balancing = null
	
	EmbeddedEditor editor
	EmbeddedEditorModelAccess model
	
	var String prefix
	var String suffix
	
	IBiMapBackedBalancingScope scope
	

	@Inject
	protected new(Shell parentShell) {
		super(parentShell)
	}

	override protected Control createDialogArea(Composite parent) {		
		this.scope =  balancingScopeMapCreator.createScope(balancing)
		
		this.prefix = DocumentCreator.createPrefix(scope)
		this.suffix = ""
		
		var Composite composite = new Composite(parent, SWT::NONE)
		var GridLayout layout = new GridLayout()
		
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants::VERTICAL_MARGIN)
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants::HORIZONTAL_MARGIN)
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants::VERTICAL_SPACING)
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants::HORIZONTAL_SPACING)
		composite.setLayout(layout)
		composite.setLayoutData(new GridData(GridData::FILL_BOTH))

		editor = factory.newEditor(resourceProvider).showErrorAndWarningAnnotations().withParent(composite)
		// editor.viewer.addVerticalRulerColumn(new LineNumberRulerColumn())
		editor.viewer.textWidget.font = JFaceResources.getFont(JFaceResources.TEXT_FONT)
		
		model = editor.createPartialEditor(prefix, parameterRenamer.createRenamedFunction(balancing) ?: "", suffix, false)

		return composite
	}

	override boolean isResizable() {
		return true
	}
	
	override configureShell(Shell shell) {
		super.configureShell(shell)
		shell.text = "Enter balancing function"
	}

	override getDefaultSize() {
		return getShell().computeSize(600, 300, true);
	}

	override okPressed() {
		if (model.editablePart != "") {
			val issues = editor.document.readOnly[ res |	
				validator.validate(res, CheckMode.ALL, null)
			]
			
			if (issues.size > 0) {
				System.err.println(issues.map[toString].join("\n"))
				MessageDialog.openError(getShell(), "Function invalid", "The given function is not valid!")
				return
			}
		}
		

		
		setReturnCode(OK)
		close()
	}

	def getContent() {
		val hasContent = editor.document.readOnly[ res |	
			val model = res.contents.get(0) as Model
			
			model.body != null && model.target != null
		]
		
		if (!hasContent) {
			return new ChangeCommand(balancing) {
				override protected doExecute() {
					balancing.actualSources.clear
					balancing.actualTarget = null
					balancing.function = ""
				}
			}
		}
		
		val text = editor.document.readOnly[ res |	
			val textContentOutputStream = new ByteArrayOutputStream
			res.save(textContentOutputStream, null)
			textContentOutputStream.toString
		]
		
		val functionText = text
			.replaceAll("^" + Pattern.quote(prefix), "")
			.replaceAll(Pattern.quote(suffix) + "$", "")
			.trim
		
		//return functionText;
		
		return editor.document.readOnly[ res |
			BalancingChangeCommandCreator.createChangeCommand(balancing, functionText, 
				balancingScopeMapCreator.createFilteredScope(scope, res.contents.get(0) as Model)
			)
		]
	}

}
