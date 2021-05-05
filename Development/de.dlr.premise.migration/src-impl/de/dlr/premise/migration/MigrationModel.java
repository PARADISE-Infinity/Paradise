/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import de.dlr.premise.migration.util.FileHelper;

public class MigrationModel {

    private boolean wasChanged = false;
    private String modelVersion = null;

    private String fileFullPath;
    private Document modelDocument;
    private Element modelRoot;

    private ModelFileType type = ModelFileType.UNKNOWN;

    private Map<String, Document> referencedFileDocuments;

    public MigrationModel(final String filePath) throws SAXException, IOException, ParserConfigurationException {

    	// open file
        File f = new File(filePath);
        f = f.getAbsoluteFile();
        fileFullPath = f.getAbsolutePath();

        // get the xml document
        modelDocument = createDocumentFromPath(fileFullPath);
        modelRoot = modelDocument.getDocumentElement();
        modelRoot.normalize();

        type = ModelFileType.getModelFileType(filePath);

        // get the meta model version
        if (modelRoot.hasAttribute("metaModel")) {
            this.modelVersion = modelRoot.getAttribute("metaModel");
        }
    }

    public void setChange() {
        wasChanged = true;
    }

    public boolean wasChanged() {
        return wasChanged;
    }

    public Element getModelRoot() {
        return modelRoot;
    }

    public Document getModelDocument() {
        return modelDocument;
    }

    public String getPath() {
        return fileFullPath;
    }

    public Map<String, Document> getReferencedFiles() {
        if (referencedFileDocuments == null) {
            referencedFileDocuments = new HashMap<String, Document>();
            Set<String> paths = getReferencedFilePaths();

            for (String path : paths) {
                try {
                    referencedFileDocuments.put(path, createDocumentFromPath(path));
                } catch (Exception e) {
                    // ignore file if it can't be read for any reason
                }
            }
        }
        return referencedFileDocuments;
    }

    public ModelFileType getModelFileType() {
		return type;
	}

	public String getVersion() {
	    return modelVersion;
	}

	public void setVersion(final String modelVersion) {
	    this.modelVersion = modelVersion;    
	}

    public void createBackup() {
        Path filePath = FileSystems.getDefault().getPath(fileFullPath);

        Iterable<Path> referencedFilePaths = Iterables.transform(referencedFileDocuments.keySet(), new Function<String, Path>() {
            @Override
            public Path apply(String input) {
                return FileSystems.getDefault().getPath(input);
            }
        });
        
        Path ancestor = getCommonAncestor(filePath, referencedFilePaths);
        Path backupRoot = filePath.getParent().resolve("backup").resolve(filePath.getFileName()).resolve("migration to " + modelVersion);
        
        int i = 2;
        while (Files.exists(backupRoot)) {
            backupRoot = filePath.getParent().resolve("backup").resolve(filePath.getFileName()).resolve("migration to " + modelVersion + " (" + i + ")");
            i++;
        }
        
        Set<Path> allPaths = Sets.newHashSet();
        allPaths.add(filePath);
        Iterables.addAll(allPaths, referencedFilePaths);
        
        for (Path originalPath : allPaths) {
            Path relativeToAncestor = ancestor.relativize(originalPath);
            Path backupPath = backupRoot.resolve(relativeToAncestor);
            try {
                Files.createDirectories(backupPath.getParent());
                Files.copy(originalPath, backupPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
	
	public void save() {
		
	    modelRoot.setAttribute("metaModel", modelVersion);
	
	    writeDocumentToPath(modelDocument, fileFullPath);
	
	    if (referencedFileDocuments != null) {
	        for (Map.Entry<String, Document> entry : referencedFileDocuments.entrySet()) {
                // update model version
                Element root = entry.getValue().getDocumentElement();
                // only write if a metaModel attribute was previously there (to prevent updating in files like registry, which don't have
                // one)
                if (!root.getAttribute("metaModel").equals("")) {
                    root.setAttribute("metaModel", modelVersion);
                }

                // write file
	            writeDocumentToPath(entry.getValue(), entry.getKey());
	        }
	    }
	}
	
	private Document createDocumentFromPath(String path) throws SAXException, IOException, ParserConfigurationException {
	
		InputStream inStream = new FileInputStream(new File(path));
	
	    Document document = null;
	    document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inStream);        
	    document.setXmlStandalone(true);
	    
	    inStream.close();
	
	    return document;
	}

	private Set<String> getReferencedFilePaths() {
        Set<String> paths = new HashSet<String>();
        getReferencedFilePaths(fileFullPath, modelRoot, paths);
        return paths;
    }

    private void getReferencedFilePaths(String path, Element root, Set<String> filePaths) {
        Set<Element> hrefElements = getElementsWithHrefAttribute(root);
        for (Element elem : hrefElements) {
            try {
                // resolve href relative to location of file
                File f = new File(new File(path).getCanonicalFile().getParent() + File.separator + elem.getAttribute("href").split("#")[0]);
                String referencedFilePath = f.getCanonicalPath();

                if (f.exists() && !filePaths.contains(referencedFilePath) && !referencedFilePath.equals(fileFullPath)) {
                    filePaths.add(referencedFilePath);
                    Element referencedFileRoot = createDocumentFromPath(referencedFilePath).getDocumentElement();
                    referencedFileRoot.normalize();
                    getReferencedFilePaths(referencedFilePath, referencedFileRoot, filePaths);
                }
            } catch (Exception e) {
                // The file is apperantly unusable, so ignore it
            }
        }
    }

    private Set<Element> getElementsWithHrefAttribute(Element root) {
        Set<Element> elements = new HashSet<Element>();

        if (root.hasAttribute("href")) {
            elements.add(root);
        }

        Node child = root.getFirstChild();
        while (child != null) {
            if (child instanceof Element) {
                elements.addAll(getElementsWithHrefAttribute((Element) child));
            }
            child = child.getNextSibling();
        }

        return elements;
    }

    private void writeDocumentToPath(final Document doc, final String path) {
        try {
            String encoding = doc.getXmlEncoding();
            
        	File input = new File(path);
        	
            OutputStream out = new FileOutputStream(input);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(out));
            out.flush();
            out.close();
            
            // ## HACK ## because of error in javax implementation
            // don't remove before the implementation is fixed
            FileHelper.removeString(input, " standalone=\"yes\"", encoding);

            // remove empty lines
            FileHelper.removeEmptyLines(input, encoding);
            
            // load with emf, modify and save, to ensure that the generated xml is as similar as possible to the previous            
//            Map<String, String> options = new HashMap<String, String>();
//            options.put(XMLResource.OPTION_ENCODING, encoding);
            
//            Resource res = new ResourceSetImpl().getResource(URI.createFileURI(path), true);
//            if (res != null) {
//                res.save(options);
//            }            
            
        } catch (TransformerFactoryConfigurationError |
        		 TransformerException |
        		 IOException e) {
            e.printStackTrace();
        }
    }    
    
    private Path getCommonAncestor(Path filePath, Iterable<Path> referencedFilePaths) {
        if (Iterables.isEmpty(referencedFilePaths)) {
            return filePath.getParent();
        }
        
        for (Path referencedFilePath : referencedFilePaths) {
            if (!filePath.getRoot().equals(referencedFilePath.getRoot()))  {
                return filePath.getRoot();
            }
        }
        
        for (int i = 0; i < filePath.getNameCount(); i++) {
            Path segment = filePath.getName(i);
            
            for (Path referencedFilePath : referencedFilePaths) {
                if (!segment.equals(referencedFilePath.getName(i))) {
                    return filePath.getRoot().resolve(filePath.subpath(0, i));
                }
            }
        }
        
        return FileSystems.getDefault().getPath("");
    }
}