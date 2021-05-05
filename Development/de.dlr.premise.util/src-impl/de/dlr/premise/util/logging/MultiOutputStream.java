/**
* Copyright (C) 2011-2016 systemsdesign.de, Germany
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Holger Schumann
*
*/

package de.dlr.premise.util.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;


public class MultiOutputStream extends OutputStream {

    private final List<OutputStream> delegates;

    public MultiOutputStream(OutputStream... outputStreams) {
        delegates = Arrays.asList(outputStreams);
    }


    @Override
    public void write(int b) throws IOException {
        for (OutputStream delegate : delegates) {
            delegate.write(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (OutputStream delegate : delegates) {
            delegate.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream delegate : delegates) {
            delegate.write(b, off, len);
        }
    }

    @Override
    public void close() throws IOException {
        for (OutputStream delegate : delegates) {
            delegate.close();
        }
    }


    @Override
    public void flush() throws IOException {
        for (OutputStream delegate : delegates) {
            delegate.close();
        }
    }
}
