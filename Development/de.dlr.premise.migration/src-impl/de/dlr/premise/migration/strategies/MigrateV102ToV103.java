/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.migration.strategies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.dlr.premise.migration.IPremiseMigration;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;


public class MigrateV102ToV103 extends AMigration implements IPremiseMigration {
        
	private Pattern globalURIFragmentPattern = Pattern.compile("^//@global/@calcEngines.(\\d+)$");
	private Pattern disciplinesURIFragmentPattern = Pattern.compile("^//@disciplines.(\\d+)/@calcEngines.(\\d+)$");

	private MigrationModel model;
	private Map<String, Element> calcEngines;
	private Map<String, Document> referencedFiles;

	@Override
    public String getTargetVersion() {
        return ModelVersion.V103.toString();
    }

	@Override
    protected void migrateRoot(MigrationModel model, Element root) {
		this.model = model;
		calcEngines = new HashMap<String, Element>();
		referencedFiles = model.getReferencedFiles();

		extractCalcEngines();

		for (Map.Entry<String, Document> entry : referencedFiles.entrySet()) {
			Element docRoot = entry.getValue().getDocumentElement();
			docRoot.normalize();

			if (docRoot.getTagName().equals("fnp:FunctionPool")) {
				moveCalcEngines(entry.getKey(), docRoot);
			}
		}

		// delete calc engines from registries
		deleteCalcEngines();

		// delete discipline refrences in values
		NodeList disciplines = model.getModelRoot().getElementsByTagName("discipline");
		while(disciplines.getLength() > 0) {
			disciplines.item(0).getParentNode().removeChild(disciplines.item(0));
			model.setChange();
		}
	}

	private void extractCalcEngines() {
		for (Map.Entry<String, Document> entry : referencedFiles.entrySet()) {
			Element root = entry.getValue().getDocumentElement();
			root.normalize();

			if (root.getTagName().equals("reg:Registry")) {
				NodeList calcEngineNodes = root.getElementsByTagName("calcEngines");
				for (int i = 0; i < calcEngineNodes.getLength(); i++) {
					Element calcEngine = (Element) calcEngineNodes.item(i);
					String calcEnginePathAndIdentifier = entry.getKey() + "#" + calcEngine.getAttribute("name");

					if (!calcEngines.containsKey(calcEnginePathAndIdentifier)) {
						calcEngines.put(calcEnginePathAndIdentifier, calcEngine);
					}
				}
			}
		}
	}

	private void moveCalcEngines(String path, Element root) {
		NodeList functionNodes = root.getElementsByTagName("functions");
		List<String> addedCalcEngines = new ArrayList<String>();


		for (int i = 0; i < functionNodes.getLength(); i++) {
			try {
				Element function = (Element) functionNodes.item(i);
				Element calcEngineReference = (Element) function.getElementsByTagName("calcEngine").item(0);

				if (calcEngineReference != null) {
					String calcEngineFilePath = new File(path).getCanonicalFile().getParent() + File.separator
							+ calcEngineReference.getAttribute("href").split("#")[0];
					calcEngineFilePath = new File(calcEngineFilePath).getCanonicalPath();

					String calcEngineIdentifier = calcEngineReference.getAttribute("href").split("#")[1];

					String calcEnginePathAndIndentifier = calcEngineFilePath + "#" + calcEngineIdentifier;

					function.removeChild(calcEngineReference);
					model.setChange();

					if (!addedCalcEngines.contains(calcEnginePathAndIndentifier)) {
						Element calcEngine = getCalcEngine(calcEngineFilePath, calcEngineIdentifier);
						if (calcEngine != null) {
							calcEngine = (Element) root.getOwnerDocument().importNode(calcEngine, true);

							calcEngine.setAttribute("xsi:type",
									calcEngine.getAttribute("xsi:type").replaceAll("^reg:", "fnp:"));

							root.appendChild(calcEngine);

							addedCalcEngines.add(calcEnginePathAndIndentifier);
						}
					}
					if (addedCalcEngines.contains(calcEnginePathAndIndentifier)) {
						function.setAttribute("calcEngine", "//@calcEngines." + addedCalcEngines.indexOf(calcEnginePathAndIndentifier));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Element getCalcEngine(String calcEngineFilePath, String calcEngineIdentifier) {
		Element calcEngine = calcEngines.get(calcEngineFilePath + "#" + calcEngineIdentifier);

		if (calcEngine == null) {
			Element calcEngineRepositoryRoot = referencedFiles.get(calcEngineFilePath).getDocumentElement();

			Matcher m;

			m = globalURIFragmentPattern.matcher(calcEngineIdentifier);
			if (m.matches()) {
				int calcEngineIndex = Integer.parseInt(m.group(1), 10);
				Element globalEl = (Element) calcEngineRepositoryRoot.getElementsByTagName("global").item(0);
				calcEngine = ((Element) globalEl.getElementsByTagName("calcEngines").item(calcEngineIndex));
			}

			m = disciplinesURIFragmentPattern.matcher(calcEngineIdentifier);
			if (m.matches()) {
				int disciplineIndex = Integer.parseInt(m.group(1), 10);
				int calcEngineIndex = Integer.parseInt(m.group(2), 10);
				Element disciplineEl = (Element) calcEngineRepositoryRoot.getElementsByTagName("disciplines").item(
						disciplineIndex);
				calcEngine = ((Element) disciplineEl.getElementsByTagName("calcEngines").item(calcEngineIndex));
			}
		}

		return calcEngine;
	}

	private void deleteCalcEngines() {
		for (Map.Entry<String, Document> entry : referencedFiles.entrySet()) {
			Element root = entry.getValue().getDocumentElement();
			root.normalize();

			if (root.getTagName().equals("reg:Registry")) {
				Node nextNode = root.getFirstChild();
				while (nextNode != null) {
					Node currentNode = nextNode;
					nextNode = currentNode.getNextSibling();

					if (currentNode.getNodeName().equals("disciplines") || currentNode.getNodeName().equals("global")) {
						root.removeChild(currentNode);
						model.setChange();
					}
				}
			}
		}
	}
}
