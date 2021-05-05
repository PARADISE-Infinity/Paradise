/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.common.command

import java.util.List
import java.util.Map
import java.util.WeakHashMap
import org.eclipse.emf.common.command.BasicCommandStack
import org.eclipse.emf.common.command.Command
import org.eclipse.xtend.lib.annotations.Accessors

import static extension java.util.Collections.unmodifiableList

class AssociatedCommandsCommandStack extends BasicCommandStack {	
	val Map<Command, List<Command>> associatedCommands = new WeakHashMap()
	@Accessors(PUBLIC_GETTER) var Command commandInExecution = null
	@Accessors(PUBLIC_GETTER) var boolean isUndoing = false
	@Accessors(PUBLIC_GETTER) var boolean isRedoing = false
	
	def getAssociatedCommands(Command command) {
		return associatedCommands.getOrDefault(command, #[]).unmodifiableList
	}
	
	def executeAssociatedCommand(Command command, Command associatedCommand) {
		val commandIsExecuting = command == commandInExecution
		// can only associate to known commands
		if (associatedCommand == null || (!command.wasExecuted && !commandIsExecuting)) {
			return
		}
		associatedCommands.putIfAbsent(command, newArrayList)
		associatedCommands.get(command) += associatedCommand
		
		// execute the associated command
		if (commandIsExecuting) {
			associatedCommand.execute
		} else {
			// if the base command is no longer running, make sure to set the correct context
			commandInExecution = command
			associatedCommand.execute
			commandInExecution = null
		}
	}
	
	override execute(Command command) {
		commandInExecution = command
		super.execute(command)
		commandInExecution = null
	}
	
	override undo() {
		isUndoing = true
		val prevUndoCommand = undoCommand
		getAssociatedCommands(prevUndoCommand).filter[canUndo].toList.reverseView.forEach[undo]
		super.undo()
		isUndoing = false
	}
		
	override redo() {
		isRedoing = true
		val prevRedoCommand = redoCommand
		super.redo()
		getAssociatedCommands(prevRedoCommand).forEach[redo]
		isRedoing = false
	}
	
	private def wasExecuted(Command command) {
		val index = commandList.indexOf(command)
		index != -1 && index <= top
	}
}