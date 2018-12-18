package com.shakshin.isoparser.containers;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

RDW layout wrapper
 */
public class RDWInputStream extends FilterInputStream {
    private final int HEADER_SIZE = 4; // record header size

    private boolean integrityControl = true; // integrity control enabled by default

    private int counter = 0; // message size counter
    private int state = 0; // reading state
    private int messageSize = 0; // current message size from header

    public RDWInputStream(InputStream in) {
        super(in);
    }

    public RDWInputStream(InputStream in, boolean integrityControl) {
        super(in);
        this.integrityControl = integrityControl;
    }

    private void readHeader() throws IOException
    {
        //read header bytes
        byte[] size = new byte[HEADER_SIZE];
        int rBytes;
        rBytes = in.read(size);
        if (rBytes == -1) { // end of stream reached
            messageSize = 0;
            state = 1;
            return;
        }

        if (rBytes < HEADER_SIZE && integrityControl) // check header integrity
            throw new IOException("RDW message integrity violated. Header read failed.");
        // parse header data to get message size
        ByteBuffer sBuff = ByteBuffer.wrap(size);
        messageSize = sBuff.getInt();
        state = 1; // set state
    }

    @Override
    public int read() throws IOException {
        if (state == 0) { // it's time to read RDW header
            readHeader();
            if (messageSize == 0) { // end of stream reached
                return -1;
            }
        }
        // read payload data
        int res = in.read();

        if (res == -1 && integrityControl && counter > 0) // check payload integrity
            throw new IOException("RDW message integrity violated. Message length mismatch");

        if (res != -1)
            counter++;
        if (counter == messageSize) { // end of record reached
            counter = 0; // reset counter
            state = 0; // set state
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
        for (int i = off; i < off + len; i++ ) { // read payload data in loop
            if (i > b.length)
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
