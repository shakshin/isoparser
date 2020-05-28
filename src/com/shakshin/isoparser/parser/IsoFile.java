package com.shakshin.isoparser.parser;

import com.shakshin.isoparser.Trace;
import com.shakshin.isoparser.configuration.Configuration;
import com.shakshin.isoparser.structure.AbstractStructure;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

ISO 8583 file class
 */

public class IsoFile {
    private InputStream in;
    private Configuration cfg;

    public LinkedList<IsoMessage> messages;

    public String checksum = null;
    public boolean checksumProblems = false;

    public IsoFile(Configuration _cfg, InputStream _in) {
        cfg = _cfg;
        in = _in;
        messages = new LinkedList<IsoMessage>();

        while (true) {
            Trace.log("IsoFile", "### Parsing message number " + (messages.size() + 1));
            IsoMessage msg = IsoMessage.read(cfg, in);
            if (msg == null)
                break;

            msg.number = messages.size() + 1;
            messages.add(msg);
        }

        AbstractStructure.getStructure(cfg).afterFileParsed(this);
    }

    public String asText() {
        String res = "";
        res += "ISO 8583 file: " + cfg.inputFile;
        res += "\nEncoding: " + cfg.encoding;
        res += "\nContainer (layout): " + cfg.container;
        res += "\nStructure definition: " + cfg.structure;
        res += "\n";
        res += "\nMessages:\n\n";

        for (int i = 0; i < messages.size(); i ++) {
            res += messages.get(i).asText();
            res += "\n=========================================================\n";
        }

        if (checksum != null) {
            res += "\nFile checksum: " + checksum + "\n";
        }
        if (checksumProblems) {
            res += "\nOne or more errors met while calculating checksum for the file. Please refer to trace for details. \n";
        }

        res += "\nISO 8583 parser by Sergey V. Shakshin";

        return res;
    }

}
