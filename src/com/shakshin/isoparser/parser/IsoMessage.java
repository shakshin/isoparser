package com.shakshin.isoparser.parser;

import com.shakshin.isoparser.configuration.Configuration;
import com.shakshin.isoparser.structure.AbstractStructure;
import com.shakshin.isoparser.structure.FieldDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IsoMessage {
    public class IsoFieldNotDefined extends Exception {
        private Integer index;
        public IsoFieldNotDefined(Integer idx) { index = idx; }
        @Override
        public String getMessage() { return "ISO field is not defined in structure: " + index.toString(); };
    };
    public class IsoFieldReadError extends Exception {
        private String message;
        public IsoFieldReadError(String msg) { message = msg; }
        @Override
        public String getMessage() { return "ISO field can not be read from stream: " + message; };
    };

    public IsoHeader header;
    public ArrayList<FieldData> fields = new ArrayList<FieldData>();
    public HashMap<Integer, FieldData> isoFields = new HashMap<Integer, FieldData>();

    private InputStream in;
    private Configuration cfg;

    public IsoMessage(Configuration _cfg, InputStream _in) {
        cfg = _cfg;
        in = _in;
    }

    private void parse(AbstractStructure struc) throws IsoFieldNotDefined, IsoFieldReadError {
        Map<Integer, FieldDefinition> defs = struc.getIsoFieldsDefinition();
        for (int i = 0; i < header.fields.size(); i++) {
            Integer idx = header.fields.get(i);
            if (idx == 1)
                continue; // secondary bitmap
            FieldDefinition def = defs.get(idx);
            if (def == null)
                throw new IsoFieldNotDefined(idx);
            try {
                byte[] rawData = Utils.readFromStream(in, def, cfg);

                FieldData field = new FieldData();
                field.name = def.name;
                field.rawData = rawData;
                field.rawConvertedData = Utils.convertBytes(rawData, cfg.getCharset());
                field.parsedData = new String(field.rawConvertedData);
                fields.add(field);
                isoFields.put(idx, field);

            } catch (Exception e) {
                throw  new IsoFieldReadError(e.getMessage());
            }
        }
        try {
            struc.afterParse(this);
        } catch (AbstractStructure.ApplicationDataParseError e) {
            System.out.println("(warning) " + e.getMessage());
        }
    }

    public static IsoMessage read(Configuration _cfg, InputStream _in) {
        try {
            IsoHeader hdr = IsoHeader.read(_cfg, _in);
            if (hdr != null) {
                IsoMessage msg = new IsoMessage(_cfg, _in);
                msg.header = hdr;
                msg.header.readAndParse();

                AbstractStructure struc = AbstractStructure.getStructure(_cfg);
                if (struc == null) {
                    System.out.println("No message structure selected");
                    return null;
                }

                msg.parse(struc);

                return msg;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("ISO message can not be read: " + e.getMessage());
            return null;
        }
    }

    public String asText() {
        String res = "";
        res += "MTI: " + header.mti;
        res += "\nPrimary bitmap: " + Utils.bin2hex(header.bitmap1);
        if (header.bitmap2 != null)
            res += "\nSecondary bitmap: " + Utils.bin2hex(header.bitmap2);
        res += "\nFields: \n";
        for (int i = 0; i < fields.size(); i++) {
            res += fields.get(i).asText(cfg) + "\n";
        }
        return res;
    }
}
