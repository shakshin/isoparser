package com.shakshin.isoparser.structure;

import com.shakshin.isoparser.Trace;
import com.shakshin.isoparser.parser.FieldData;
import com.shakshin.isoparser.parser.IsoFile;
import com.shakshin.isoparser.parser.IsoMessage;

import java.util.HashMap;
import java.util.Map;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

JCB Interchange File structure definition class
 */

public class JcbStructure extends AbstractStructure {
    private String pdeBuffer = "";
    @Override
    public Map<Integer, FieldDefinition> getIsoFieldsDefinition() {
        HashMap<Integer, FieldDefinition> res = new HashMap<Integer, FieldDefinition>();

        res.put(2, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "Bit 2", false, true));
        res.put(3, new FieldDefinition(FieldDefinition.LengthType.Fixed, 6, "Bit 3", false));
        res.put(4, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "Bit 4", false));
        res.put(5, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "Bit 5", false));
        res.put(6, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "Bit 6", false));
        res.put(9, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "Bit 9", false));
        res.put(10, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "Bit 10", false));
        res.put(12, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "Bit 12", false));
        res.put(14, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "Bit 14", false));
        res.put(16, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "Bit 16", false));
        res.put(22, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "Bit 22", false));
        res.put(23, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "Bit 23", false));
        res.put(24, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "Bit 24", false));
        res.put(25, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "Bit 25", false));
        res.put(26, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "Bit 26", false));
        res.put(30, new FieldDefinition(FieldDefinition.LengthType.Fixed, 24, "Bit 30", false));
        res.put(31, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "Bit 31", false));
        res.put(32, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "Bit 32", false));
        res.put(33, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "Bit 33", false));
        res.put(37, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "Bit 37", false));
        res.put(38, new FieldDefinition(FieldDefinition.LengthType.Fixed, 6, "Bit 38", false));
        res.put(40, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "Bit 40", false));
        res.put(41, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "Bit 41", false));
        res.put(42, new FieldDefinition(FieldDefinition.LengthType.Fixed, 15, "Bit 42", false));
        res.put(43, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "Bit 43", false));
        res.put(48, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "Bit 48", false));
        res.put(49, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "Bit 49", false));
        res.put(50, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "Bit 50", false));
        res.put(51, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "Bit 51", false));
        res.put(54, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "Bit 54", false));
        res.put(55, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "Bit 55", true));
        res.put(62, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "Bit 62", false));
        res.put(71, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "Bit 71", false));
        res.put(72, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "Bit 72", false));
        res.put(93, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "Bit 93", false));
        res.put(94, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "Bit 94", false));
        res.put(97, new FieldDefinition(FieldDefinition.LengthType.Fixed, 17, "Bit 97", false));
        res.put(100, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "Bit 100", false));
        res.put(123, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "Bit 123", false));
        res.put(124, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "Bit 124", false));
        res.put(125, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "Bit 125", false));
        res.put(126, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "Bit 126", false));

        return res;
    }

    @Override
    public void afterMessageParsed(IsoMessage msg) throws ApplicationDataParseError {
        getPdeBuffer(msg);
        try {
            while (pdeBuffer.length() > 0) {
                String tag = bufRead(4);
                String len = bufRead(3);
                Integer l = Integer.parseInt(len);
                String data = bufRead(l);

                FieldData d = new FieldData();
                d.name = "PDE" + tag;
                d.parsedData = data;
                msg.fields.add(d);
            }
        } catch (Exception e) {
            throw new ApplicationDataParseError("Can not parse PDE: " + e.getMessage());
        }
    }

    @Override
    public void afterFileParsed(IsoFile file) {

    }

    private String bufRead(Integer len) throws ApplicationDataParseError {
        if (len > pdeBuffer.length())
            throw new ApplicationDataParseError("No enough data in PDE buffer");
        String rd = pdeBuffer.substring(0,len);
        pdeBuffer = pdeBuffer.substring(len, pdeBuffer.length());
        return rd;
    }

    private void getPdeBuffer(IsoMessage msg) {
        pdeBuffer += msg.isoFields.containsKey(48) ? msg.isoFields.get(48).parsedData : "";
        pdeBuffer += msg.isoFields.containsKey(62) ? msg.isoFields.get(62).parsedData : "";
        pdeBuffer += msg.isoFields.containsKey(123) ? msg.isoFields.get(123).parsedData : "";
        pdeBuffer += msg.isoFields.containsKey(124) ? msg.isoFields.get(124).parsedData : "";
        pdeBuffer += msg.isoFields.containsKey(125) ? msg.isoFields.get(125).parsedData : "";
        pdeBuffer += msg.isoFields.containsKey(126) ? msg.isoFields.get(126).parsedData : "";
    }

    @Override
    public void runReport(IsoFile file, String report) {
        Trace.error("JCB", "No reports implemented yet");
    }
}
