package com.shakshin.isoparser.parser;

import com.shakshin.isoparser.Trace;
import com.shakshin.isoparser.configuration.Configuration;
import com.shakshin.isoparser.containers.CleanInputStream;
import com.shakshin.isoparser.structure.AbstractStructure;
import com.shakshin.isoparser.structure.FieldDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

ISO 8583 message data class
 */

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
    public HashMap<String, String> namedFields = new HashMap<>();
    public HashMap<Integer, FieldData> isoFields = new HashMap<Integer, FieldData>();
    public Integer number;
    public Long offset;

    private CleanInputStream in;
    private Configuration cfg;

    public IsoMessage(Configuration _cfg, CleanInputStream _in) {
        cfg = _cfg;
        in = _in;
    }

    private void parse(AbstractStructure struc) throws IsoFieldNotDefined, IsoFieldReadError {
        Trace.log("IsoMessage", "Parsing fields");
        Map<Integer, FieldDefinition> defs = struc.getIsoFieldsDefinition();
        Trace.log("IsoMessage", "Got fields definitions from structure");

        String ids = "";
        for (int i = 0; i < header.fields.size(); i++) ids += String.valueOf(header.fields.get(i)) + " ";

        Trace.log("IsoMessage", "Fields ids: " + ids);


        for (int i = 0; i < header.fields.size(); i++) {
            Integer idx = header.fields.get(i);
            if (idx == 1)
                continue; // secondary bitmap
            Trace.log("IsoMessage", "Reading field " + idx);
            FieldDefinition def = defs.get(idx);
            if (def == null) {
                Trace.error("IsoMessage", "Field definition is not present: " + idx);
                throw new IsoFieldNotDefined(idx);
            }
            try {
                Long off = in.getOffset();
                byte[] rawData = Utils.readFromStream(in, def, cfg);

                FieldData field = new FieldData();
                field.name = def.name;
                field.rawData = rawData;
                field.offset = off;
                if (!def.binary) {
                    field.rawConvertedData = Utils.convertBytes(rawData, cfg.getCharset());
                    field.parsedData = new String(field.rawConvertedData);
                } else {
                    field.parsedData = "(binary)";
                }
                if (def.mask && cfg.masked) {
                    field.masked = true;
                }
                fields.add(field);
                isoFields.put(idx, field);

            } catch (Exception e) {
                Trace.error("IsoMessage", "Field read error: " + e.getMessage());
                throw new IsoFieldReadError(e.getMessage());
            }
        }

        try {
            Trace.log("IsoMessage", "Invoking application-level parser");
            struc.afterMessageParsed(this);
        } catch (AbstractStructure.ApplicationDataParseError e) {
            Trace.error("IsoMessage", "Application-level parser error: " + e.getMessage());
        }

        for (FieldData fd : fields) namedFields.put(fd.name, fd.parsedData);
    }

    public static IsoMessage read(Configuration _cfg, CleanInputStream _in) {
        try {
            Trace.log("IsoMessage", "Reading message");
            Long off = _in.getOffset();
            IsoHeader hdr = IsoHeader.read(_cfg, _in);
            if (hdr != null) {
                Trace.log("IsoMessage", "ISO header read ok");
                IsoMessage msg = new IsoMessage(_cfg, _in);
                msg.header = hdr;
                msg.header.readAndParse();
                msg.offset = off;

                AbstractStructure struc = AbstractStructure.getStructure(_cfg);
                if (struc == null) {
                    Trace.error("IsoMessage", "No structure defined");
                    return null;
                }

                msg.parse(struc);

                return msg;
            } else {
                Trace.log("IsoMessage", "Header was not read");
                return null;
            }
        } catch (Exception e) {
            Trace.error("IsoMessage", "Message can not be parsed: " + e.getMessage());
            return null;
        }
    }

    public String asText() {
        String res = "";
        res += "Message number: " + number;
        if (offset != null) res += "\nOffset: 0x" + Long.toHexString(offset);
        res += "\nMTI: " + header.mti;
        res += "\nPrimary bitmap: " + Utils.bin2hex(header.bitmap1);
        if (header.bitmap2 != null)
            res += "\nSecondary bitmap: " + Utils.bin2hex(header.bitmap2);
        res += "\nFields: \n";
        for (int i = 0; i < fields.size(); i++) {
            res += fields.get(i).asText(cfg) + "\n";
        }
        return res;
    }

    private String getDescription() {
        String def = header.mti + "-" + isoFields.get(24).parsedData;
        switch (def) {
            case "1644-697":
                def += ": File Header";
            case "1644-695":
                def += ": File Trailer";
            case "1644-603":
                def += ": Retrieval Request";
            case "1644-693":
                def += ": Text Message";
            case "1644-699":
                def += ": File Reject";
            case "1644-685":
                def += ": Financial Position Detail";
            case "1644-688":
                def += ": Settlement Position Detail";
            case "1644-680":
                def += ": File Currency Summary";
            case "1644-691":
                def += ": Message Exception";
            case "1644-640":
                def += ": Currency Update";
            case "1240-200":
                def += ": First Presentment";
            case "1240-205":
            case "1240-282":
                def += ": Second Presentment";
            case "1442-450":
            case "1442-453":
            case "1442-451":
            case "1442-454":
                def += ": Chargeback";
            default:
                ;
        }

        if (offset != null)
            def += "; Offfset: " + Long.toHexString(offset);

        return def;
    }

    @Override
    public String toString() {

        return "IsoMessage{"+getDescription()+"}";
    }
}
