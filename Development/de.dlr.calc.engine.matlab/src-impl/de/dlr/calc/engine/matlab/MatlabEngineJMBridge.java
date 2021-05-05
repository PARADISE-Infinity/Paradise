/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.matlab;

import java.io.File;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import com.modelengineers.tooladapter.jmbridge.api.JMBridge;
import com.modelengineers.tooladapter.jmbridge.api.Matlab;
import com.modelengineers.tooladapter.jmbridge.net.server.MatlabException;

public class MatlabEngineJMBridge {

    public final String PROP_MULTICALC = "MULTI_CALCULATION";
    public final String PROP_MATLAB = "MATLAB_PATH";
    public final String PROP_JMBRIDGE = "JMBRIDGE_PATH";
    public final String PROP_JMBRIDGE_PORT = "JMBRIDGE_PORT";
    public final String PROP_JMBRIDGE_TIME = "JMBRIDGE_TIME";

    private final MatlabConfig config;
    
    private JMBridge jmbridge;
    private Matlab matlab;
    
    /**
     * Constructor
     */
    public MatlabEngineJMBridge(MatlabConfig config) {
        this.config = config;
        
        jmbridge = new JMBridge(config.getMatlabPath(), 
        						config.getJMBridgePath(), 
        						config.getJMBridgePort(), 
        						config.getJMBridgeTimeout());
        try {
            matlab = jmbridge.startMatlabAndConnect();
        } catch (Exception e) {
            close();
            System.err.println(e.getMessage());
        }        
    }

    public boolean setWorkingDir(final String uri) {

        // check inputs
        boolean valid = false;
        if (matlab == null) {
            return valid;
        };

        try {
            // get new working directory and change it in MATLAB
            File file = new File(uri);
            matlab.changeDirectory(file.getCanonicalPath().toString());

            // validate the changed MATLAB directory
            String varName = "p_31231245323";
            matlab.eval(varName + " = pwd;");
            String mPath = matlab.getVariableString(varName);
            matlab.eval("clear " + varName + ";");
            if (mPath.equals(file.getCanonicalPath().toString())) {
                valid = true;
            }
        } catch (Exception e) {
            System.err.println("Unable to set working Dir caused by:" + e.getMessage());
        }
        return valid;
    }

    public boolean addLibrary(final String uri) {
        
        // check inputs
        boolean valid = false;
        if (matlab == null) {
            return valid;
        }

        try {
            // get library directory and add it to MATLAB path
            File file = new File(uri);
            String libPath = file.getAbsolutePath().toString();
            matlab.addPaths(libPath);

            // check new MATLAB path
            String mPath = matlab.getPath();
            valid = mPath.contains(libPath);
            
        } catch (Exception e) {
            System.err.println("add Library failed caused by:" + e.getMessage());
            e.printStackTrace();
        }

        return valid;
    }

    public double callScript(String name, EList<Double> inputs) {

        // check inputs
        Double value = Double.NaN;
        if (matlab == null) {
            return value;
        }

        // build calculation command
        String varName = MatlabHelper.getTmpVarName(32);
        String cmd = varName + "=" + name + "(" + getParameter(inputs) + ");";

        try {
            matlab.eval(cmd);
            value = new Double(matlab.getVariableDouble(varName));
            matlab.eval("clear " + varName + ";");
        } catch (Exception e) {
            System.err.println("Unable to calculate cmd:" + cmd);
        }

        System.out.println("Calculated " + value);
        
        return value;
    }

    public void setProperty(final String name, final String value) {

        if (name.equals(PROP_MULTICALC)) {
        	config.setMultiCalc(value);
        }

        if (name.equals(PROP_MATLAB)) {
            config.setMatlabPath(value);
        }

        if (name.equals(PROP_JMBRIDGE)) {
            config.setJMBridgePath(value);
        }
        
        if(name.equals(PROP_JMBRIDGE_PORT)) {
        	int iValue =Integer.parseInt(value);
        	config.setJMBridgePort(iValue);
        }
        
        if(name.equals(PROP_JMBRIDGE_TIME)) {
        	int iValue =Integer.parseInt(value);
        	config.setJMBridgeTimeOut(iValue);
        }
    }

    /**
     * Close JMBridge and MATLAB connection an
     */
    public void close() {

    	if (config.getMultiCalc() == true) {
    		return;
    	}
    	
        try {
            if (jmbridge != null) {
                jmbridge.closeMatlab();
            }
        } catch (MatlabException e) {
            System.err.println(e.getMessage());
        }

        jmbridge = null;
    }

    /**
     * Checks if the MATLAB engine is available
     * 
     * @return
     */
    public boolean isAvailable() {
        // check if JMBridge and MATLAB where initialized
        if (jmbridge != null && matlab != null && jmbridge.matlabIsRunning()) {
            return true;
        }
    
        return false;
    }

    public MatlabConfig getConfig() {
    	return config;
    }
    
    
    /**
     * @param inputParameters list of double parameters
     * @returns
     *  Returns a string with a comma seperated parameter names list 
     */
    private String getParameter(List<Double> inputParameters) {

        String pList = "";

        int length = inputParameters.size();
        for (int i = 0; i < length; i++) {
            pList += inputParameters.get(i);
            if (i < length - 1) {
                pList += ",";
            }
        }

        
        System.out.println("Parameter " + pList);
        
        return pList;
    }
}
