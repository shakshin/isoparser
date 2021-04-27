package com.shakshin.isoparser.containers.mastercard;

import com.shakshin.isoparser.Trace;
import com.shakshin.isoparser.configuration.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class IPMProbe {
    public Configuration.DataEncoding encoding = null;
    public Configuration.ContainerType container = null;
    public Boolean mainframe = null;

    public void probe(String path) {
        Trace.log("PROBE", "Start file structure detection");
        FileInputStream in = null;
        try {
            boolean isRdw = false;
            boolean probe1014 = false;
            boolean is1014 = false;

            File file = new File(path);

            probe1014 = file.length() % 1014 == 0;
            if (probe1014) Trace.log("PROBE", "File size is multiple of 1014. Could be MC 1014 aligned layout");

            in = new FileInputStream(file);

            Trace.log("PROBE", "Reading first 4 bytes of file");
            byte[] hdr = new byte[4];

            int rd = in.read(hdr);
            if (rd < 4) {
                Trace.error("PROBE", "No enough bytes");
                return;
            }

            byte[] hdrEBCDIC = new byte[] { (byte) 0xF1, (byte) 0xF6, (byte) 0xF4, (byte) 0xF4 };
            byte[] hdrASCII = new byte[] { (byte) 0x31, (byte) 0x36, (byte) 0x34, (byte) 0x34 };
            //byte[] hdrRDW = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4A };
            //byte[] hdrMF = new byte[] { (byte) 0x00, (byte) 0x4E, (byte) 0x00, (byte) 0x00 };

            if (hdr[0] == (byte) 0x00 && hdr[1] == (byte) 0x00 && hdr[2] == (byte) 0x00) {
                Trace.log("PROBE", "RDW header detected");
                isRdw = true;
                mainframe = false;
            } else if (hdr[0] == (byte) 0x00 && hdr[2] == (byte) 0x00 && hdr[3] == (byte) 0x00) {
                Trace.log("PROBE", "Mainframe RDW header detected");
                isRdw = true;
                mainframe = true;
            }

            if (isRdw) {
                Trace.log("PROBE", "Reading next 4 bytes of file");
                rd = in.read(hdr);
                if (rd < 4) {
                    Trace.error("PROBE", "No enough bytes");
                    return;
                }
            }

            if (Arrays.equals(hdr, hdrASCII)) {
                Trace.log("PROBE", "Detected ASCII encoding");
                encoding = Configuration.DataEncoding.ASCII;
            } else if (Arrays.equals(hdr, hdrEBCDIC)) {
                Trace.log("PROBE", "Detected EBCDIC encoding");
                encoding = Configuration.DataEncoding.EBCDIC;
            }

            if (isRdw && probe1014) {
                Trace.log("PROBE", "RDW found and file size is multiple of 1014. Will check for MC 1014 blocked layout");
                RandomAccessFile raf = null;
                try {
                    Trace.log("PROBE", "Reading last 6 bytes of file");
                    raf = new RandomAccessFile(path, "r");
                    raf.seek(file.length() - 6);
                    byte[] last = new byte[6];
                    int rd1 = raf.read(last);
                    if (rd1 < 6) {
                        Trace.log("PROBE", "No enough bytes (?!)");
                        return;
                    }

                    boolean not1014 = false;
                    for (int i = 0; i < 6; i++) {
                        if (last[i] != (byte) 0x00 && last[i] != (byte) 0x40) {
                            Trace.log("PROBE", "Non-zero byte found. This is not MC 1014");
                            not1014 = true;
                            break;
                        }
                    }
                    is1014 = !not1014;

                } finally {
                    try {
                        if (raf != null) raf.close();
                    } catch (IOException e2 ) {}
                }
            }

            if (is1014) {
                Trace.log("PROBE", "Detected container MC 1014");
                container = Configuration.ContainerType.MC1014;
            } else if (isRdw) {
                Trace.log("PROBE", "Detected container RDW");
                container = Configuration.ContainerType.RDW;
            } else {
                Trace.log("PROBE", "Detected container NONE");
                container = Configuration.ContainerType.NONE;
            }

        } catch (IOException e) {
            Trace.log("PROBE", "Probe unsuccessful");
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e1) {}
        }
    }
}
