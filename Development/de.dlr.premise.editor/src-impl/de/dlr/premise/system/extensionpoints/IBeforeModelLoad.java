package de.dlr.premise.system.extensionpoints;

import org.eclipse.core.resources.IFile;

public interface IBeforeModelLoad {
	public void execute(IFile file);
}
