package com.shakshin.isoparser.containers.mastercard;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IPMBlockedInputStream extends FilterInputStream {
    private final int BLOCK_SIZE = 1012; // Block payload size
    private final int TRAILER_SIZE = 2; // Block trailer size

    private int counter = 0; // block size counter
    private boolean integrityControl = true; // integrity control enabled by default

    public IPMBlockedInputStream(InputStream in, boolean integrityControl) {
        super(in);
        this.integrityControl = integrityControl;
    }

    public IPMBlockedInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        if (counter == BLOCK_SIZE) { // end of block payload reached. need to read trailer
            byte[] skip = new byte[TRAILER_SIZE];
            int skipped = in.read(skip);
            if (integrityControl && skipped < TRAILER_SIZE) // check trailer integrity
                throw new IOException("Fixed_1014 block integrity violated.");
            counter = 0;
        }
        // read block payload
        int res = super.read();
        if (res == -1 && integrityControl && counter > 0) // check integrity
            throw new IOException("Fixed_1014 block integrity violated.");
        if (res != -1) {
            counter++;
        }
        return res;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = 0;
        for (int i = off; i < off + len; i++) { // read payload data in loop
            if (i >= b.length)
                break;
            int val = read();
            if (val == -1)
                break;
            b[i] = (byte)val;
            bytesRead++;
        }
        return bytesRead;
    }

    @Override
    public long skip(long n) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int available() throws IOException {
        return super.available();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}
