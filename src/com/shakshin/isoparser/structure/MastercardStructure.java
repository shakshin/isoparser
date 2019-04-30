package com.shakshin.isoparser.structure;

import com.shakshin.isoparser.Trace;
import com.shakshin.isoparser.parser.FieldData;
import com.shakshin.isoparser.parser.IsoMessage;
import com.shakshin.isoparser.parser.Utils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

Mastercard IPM File structure definition class
 */

public class MastercardStructure extends AbstractStructure {
    private String pdsBuffer = "";

    @Override
    public Map<Integer, FieldDefinition> getIsoFieldsDefinition() {
        Trace.log("MC", "Preparing field definitions");
        HashMap<Integer, FieldDefinition> res = new HashMap<Integer, FieldDefinition>();

        res.put(2, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 2", false, true));
        res.put(3, new FieldDefinition(FieldDefinition.LengthType.Fixed, 6, "DE 3", false));
        res.put(4, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 4", false));
        res.put(5, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 5", false));
        res.put(6, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 6", false));
        res.put(9, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "DE 9", false));
        res.put(10, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "DE 10", false));
        res.put(12, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 12", false));
        res.put(14, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "DE 14", false));
        res.put(22, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 22", false));
        res.put(23, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 23", false));
        res.put(24, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 24", false));
        res.put(25, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "DE 25", false));
        res.put(26, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "DE 26", false));
        res.put(30, new FieldDefinition(FieldDefinition.LengthType.Fixed, 24, "DE 30", false));
        res.put(31, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 31", false));
        res.put(32, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 32", false));
        res.put(33, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 33", false));
        res.put(37, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 37", false));
        res.put(38, new FieldDefinition(FieldDefinition.LengthType.Fixed, 6, "DE 38", false));
        res.put(40, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 40", false));
        res.put(41, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "DE 41", false));
        res.put(42, new FieldDefinition(FieldDefinition.LengthType.Fixed, 15, "DE 42", false));
        res.put(43, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 43", false));
        res.put(48, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 48", false));
        res.put(49, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 49", false));
        res.put(50, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 50", false));
        res.put(51, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 51", false));
        res.put(54, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 54", false));
        res.put(55, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 55", true));
        res.put(62, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 62", false));
        res.put(63, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 63", false));
        res.put(71, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "DE 71", false));
        res.put(72, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 72", false));
        res.put(73, new FieldDefinition(FieldDefinition.LengthType.Fixed, 6, "DE 73", false));
        res.put(93, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 93", false));
        res.put(94, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 94", false));
        res.put(95, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 95", false));
        res.put(100, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 100", false));
        res.put(111, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 111", false));
        res.put(123, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 123", false));
        res.put(124, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 124", false));
        res.put(125, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 125", false));
        res.put(127, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 127", false));

        return res;
    }

    @Override
    public void afterParse(IsoMessage msg) throws ApplicationDataParseError {
        Trace.log("MC", "Performing application-level parsing");
        try {
            parseEMV(msg);
            Trace.log("MC", "EMV parser finished");
        } catch (Exception e) {}

        Trace.log("MC", "parsing PDS-fields");
        getPdsBuffer(msg);
        try {
            while (pdsBuffer.length() > 0) {
                String tag = bufRead(4);
                Trace.log("MC", "PDS tag: " + tag);
                String len = bufRead(3);
                Trace.log("MC", "PDS length " + len);
                Integer l = Integer.parseInt(len);
                String data = bufRead(l);
                Trace.log("MC", "PDS tag data read ok");

                FieldData d = new FieldData();
                d.name = "pds" + tag;
                d.parsedData = data;
                msg.fields.add(d);
            }
        } catch (Exception e) {
            Trace.log("MC", "Can not parse PDS: " + e.getMessage());
            throw new ApplicationDataParseError("Can not parse PDS: " + e.getMessage());
        }


    }

    private String bufRead(Integer len) throws ApplicationDataParseError {
        if (len > pdsBuffer.length())
            throw new ApplicationDataParseError("No enough data in PDS buffer");
        String rd = pdsBuffer.substring(0,len);
        pdsBuffer = pdsBuffer.substring(len, pdsBuffer.length());
        return rd;
    }

    private void getPdsBuffer(IsoMessage msg) {
        Trace.log("MC", "Combining additional data fields to single buffer");
        pdsBuffer += msg.isoFields.containsKey(48) ? msg.isoFields.get(48).parsedData : "";
        pdsBuffer += msg.isoFields.containsKey(62) ? msg.isoFields.get(62).parsedData : "";
        pdsBuffer += msg.isoFields.containsKey(123) ? msg.isoFields.get(123).parsedData : "";
        pdsBuffer += msg.isoFields.containsKey(124) ? msg.isoFields.get(124).parsedData : "";
        pdsBuffer += msg.isoFields.containsKey(125) ? msg.isoFields.get(125).parsedData : "";
    }

    private void parseEMV(IsoMessage msg) {
        Trace.log("MC", "Parsing EMV data");
        if (msg.isoFields.get(55) == null)
            return;

        FieldData de55 = msg.isoFields.get(55);

        Utils.parseBerTLV(de55.rawData, de55.children, de55.appParserProblems);

    }
}
