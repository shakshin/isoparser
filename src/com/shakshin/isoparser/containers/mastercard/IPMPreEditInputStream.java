package com.shakshin.isoparser.containers.mastercard;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

Mastercard IPM Pre-Edit layout wrapper
 */

public class IPMPreEditInputStream extends FilterInputStream {
    private final int HEADER_SIZE = 4; // record header size: 4 bytess
    private final int FILE_HEADER_SIZE = 128; // file header size: 128 bytes

    private boolean integrityControl = true; // integrity control enabled by default

    private int counter = 0; // message size counter
    private int state = 0; // reading state; 0 - header; 1 - message
    private int messageSize = 0; // current message size from header
    public byte[] fileHeader; // bytes from file header
    private boolean isFirstRecord = true; // first record indicator

    public IPMPreEditInputStream(InputStream in) throws IOException {
        super(in);
        readFileHeader(); // need to read file record after initialization
    }

    public IPMPreEditInputStream(InputStream in, boolean integrityControl) throws IOException {
        super(in);
        this.integrityControl = integrityControl;
        readFileHeader(); // need to read file header after initialization
    }

    private void readFileHeader() throws IOException { // just read bytes to byte array
        fileHeader = new byte[FILE_HEADER_SIZE];
        int rBytes = in.read(fileHeader);
        if (rBytes != FILE_HEADER_SIZE && integrityControl) // check header size
            throw new IOException("Pre-Edit file header read failed");
    }

    private void readHeader() throws IOException
    {
        if (isFirstRecord) // reset first record indicator
            isFirstRecord = false;
        else { // or do alignment to 4 bytes
            int toSkip = 4 - (messageSize % 4);
            if (toSkip > 0 && toSkip < 4) {
                byte[] skip = new byte[toSkip];
                in.read(skip);
            }
        }
        // read and parse message size
        byte[] size = new byte[HEADER_SIZE];
        int rBytes;
        rBytes = in.read(size);
        if (rBytes == -1) { // end of stream
            messageSize = 0;
            state = 1;
            return;
        }
        if (rBytes < HEADER_SIZE && integrityControl) // check header size
            throw new IOException("Pre-Edit message integrity violated. Header read failed.");
        if (size[0] != 64 && integrityControl) // check header format
            throw new IOException("Pre-Edit message integrity violated. Incorrect record size format");
        size[0] = 0; // pre-edit specific manipulation with header data
        ByteBuffer sBuff = ByteBuffer.wrap(size);
        messageSize = sBuff.getInt(); // set current message size
        state = 1; // change state
    }

    @Override
    public int read() throws IOException {
        if (state == 0) { // it's time to read record header
            readHeader();
            if (messageSize == 0) { // end of stream. nothing to read anymore
                return -1;
            }
        }
        int res = in.read();
        if (res == -1 && integrityControl && counter > 0) // check record integrity
            throw new IOException("Pre-Edit message integrity violated. Message length mismatch");
        if (res != -1)
            counter++;
        if (counter == messageSize) { // end of record reached. reset counter and set state
            counter = 0;
            state = 0;
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
        for (int i = off; i < off + len; i++ ) { // read payload in loop
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
