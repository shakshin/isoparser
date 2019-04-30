package com.shakshin.isoparser.parser;

import com.shakshin.isoparser.Trace;
import com.shakshin.isoparser.configuration.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

ISO 8583 message header data class
 */

public class IsoHeader {
    public InputStream in;

    public String mti;
    public byte[] bitmap1;
    public byte[] bitmap2;
    public ArrayList<Integer> fields = new ArrayList<Integer>();



    public void readAndParse() throws IOException {
        Trace.log("IsoHeader", "Parsing primary bitmap");
        parseBitmap(bitmap1, 1);
        if (fields.contains(1)) {
            Trace.log("IsoHeader", "Need to read secondary bitmap");
            bitmap2 = new byte[8];
            if (in.read(bitmap2) < 8) {
                Trace.log("IsoHeader", "Secondary bitmap read failed. No enough data");
                throw new IOException("Secondary bitmap can not be read: no enough bytes");
            }
            parseBitmap(bitmap2, 2);
            Trace.log("IsoHeader", "Bitmap(s) parsed");
        }
    }

    private void parseBitmap(byte[] buff, int seq) {
        for (int b = 0; b < 8; b++) {
            BitSet bs = BitSet.valueOf(new byte[] {buff[b]});
            for (int i = 0; i < 8; i++) {
                int idx =
                    ((seq - 1) * 64) // bitmap number
                    + (b * 8) // byte number
                    + i+1; // bit number
                if (bs.get(7-i)) {
                    fields.add(idx);
                }
            }
        }

    }


    public static IsoHeader read(Configuration _cfg, InputStream _in) {
        byte[] rawMti = new byte[4];

        try {
            Trace.log("IsoHeader", "Reading MTI");
            int r = _in.read(rawMti);
            if (r <= 0) {
                Trace.log("IsoHeader", "No more data. EOF reached");
                return null;
            } else if (r < 4) {
                Trace.log("IsoHeader", "MTI can not be read. No enough data");
                throw new IOException("MTI can not be read. No enough bytes.");
            }

            rawMti = Utils.convertBytes(rawMti, _cfg.getCharset());

            String mti = new String(rawMti);

            Trace.log("IsoHeader", "Reading primary bitmap");
            byte[] bm1 = new byte[8];
            int br = _in.read(bm1);
            if (br == 0) { // no data in stream for next message
                Trace.log("IsoHeader", "EOF reached");
                return null;
            } else if (br < 8) {
                Trace.log("IsoHeader", "Primary bitmap can not be read. No enough data");
                throw new IOException("Primary bitmap can not be read. No enough bytes.");
            }

            IsoHeader hdr = new IsoHeader();
            hdr.bitmap1 = bm1;
            hdr.mti = mti;
            hdr.in = _in;

            return hdr;

        } catch (IOException e) {
            Trace.log("IsoHeader", "Can not read header: " + e.getMessage());
            System.out.println("Can not read ISO 8583 header: " + e.getMessage());
            return null;
        }
    }
}
