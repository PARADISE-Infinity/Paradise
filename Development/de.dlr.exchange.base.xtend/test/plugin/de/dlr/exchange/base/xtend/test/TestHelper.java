/**
* Copyright (C) 2011-2016 systemsdesign.de, Germany
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Holger Schumann
*
*/

package de.dlr.exchange.base.xtend.test;

import static org.junit.Assert.fail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;

import com.google.common.io.Files;

/**
 * @author hschum
 *
 */
public class TestHelper {

    /**
     * @see de.dlr.premise.util.TestHelper#loadResource(String)
     */
    public static Resource loadResource(String filePath) {
        return de.dlr.premise.util.TestHelper.loadResource(filePath);
    }
    
    /**
     * @see de.dlr.premise.util.TestHelper#loadResource(ResourceSet, String)
     */
    public static Resource loadResource(ResourceSet resSet, String filePath) {
        return de.dlr.premise.util.TestHelper.loadResource(resSet, filePath);
    }

    /**
     * @see de.dlr.premise.util.TestHelper#replaceFileString(String, String, String, String)
     */
    public static void replaceFileString(String inputFile, String outputFile, String oldText, String newText) {
        de.dlr.premise.util.TestHelper.replaceFileString(inputFile, outputFile, oldText, newText);
    }

    /**
     * Creates a list of files with given paths/names, saves the content of given fsa into the files and returns them
     * 
     * @param fsa carrying the String content
     * @param filePaths list of path and name to files being created
     * @return the written files or failed junit test
     */
    public static EList<File> saveFiles(InMemoryFileSystemAccess fsa, EList<String> filePaths) {
        EList<File> result = new BasicEList<File>();
        try {
            Collection<Object> files = fsa.getAllFiles().values();
            for (int i = 0; i < files.size(); i++) {
                File file = new File(filePaths.get(i));
                Files.createParentDirs(file);
                Object content = files.toArray()[i];
                if (content instanceof CharSequence) {
                    Writer writer = new OutputStreamWriter(new FileOutputStream(file), fsa.getTextFileEncoding());
                    writer.write(files.toArray()[i].toString());
                    writer.close();
                } else if (content instanceof byte[]) {
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                    out.write((byte[]) content);
                    out.close();
                }
                result.add(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return result;
    }

    /**
     * Creates a file with given path/name, saves the content of given fsa into the file and returns it
     * 
     * @param fsa carrying the String content
     * @param filePath path and name to file being created
     * @return the written file or failed junit test
     */
    public static File saveFile(InMemoryFileSystemAccess fsa, String filePath) {
        EList<String> list = new BasicEList<String>();
        list.add(filePath);
        File result = saveFiles(fsa, list).get(0);
        if (result == null) {
            fail("TestHelper.saveFile(): FSA contains no files or more than one! Generation failed!");
        }
        return result;
    }
    
    /**
     * Factory method to create an InMemoryFileSystemAccess
     * 
     * @return a new instance of an InMemoryFileSystemAccess
     */
    public static CharsetProvidingInMemoryFileSystemAccess createInMemoryFileSystemAccess() {
        CharsetProvidingInMemoryFileSystemAccess fsa = new CharsetProvidingInMemoryFileSystemAccess();
        // For backwards compatibility, this replicates the behavior of relying on the workspace encoding
        fsa.setTextFileEnconding("UTF-8");
        return fsa;
    }
}
