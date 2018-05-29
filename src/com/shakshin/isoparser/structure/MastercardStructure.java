package com.shakshin.isoparser.structure;

import com.shakshin.isoparser.parser.FieldData;
import com.shakshin.isoparser.parser.IsoMessage;

import java.util.HashMap;
import java.util.Map;

public class MastercardStructure extends AbstractStructure {
    private String pdsBuffer = "";

    @Override
    public Map<Integer, FieldDefinition> getIsoFieldsDefinition() {
        HashMap<Integer, FieldDefinition> res = new HashMap<Integer, FieldDefinition>();

        res.put(2, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 2"));
        res.put(3, new FieldDefinition(FieldDefinition.LengthType.Fixed, 6, "DE 3"));
        res.put(4, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 4"));
        res.put(5, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 5"));
        res.put(6, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 6"));
        res.put(9, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "DE 9"));
        res.put(10, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "DE 10"));
        res.put(12, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 12"));
        res.put(14, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "DE 14"));
        res.put(22, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 22"));
        res.put(23, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 23"));
        res.put(24, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 24"));
        res.put(25, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "DE 25"));
        res.put(26, new FieldDefinition(FieldDefinition.LengthType.Fixed, 4, "DE 26"));
        res.put(30, new FieldDefinition(FieldDefinition.LengthType.Fixed, 24, "DE 30"));
        res.put(31, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 31"));
        res.put(32, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 32"));
        res.put(33, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 33"));
        res.put(37, new FieldDefinition(FieldDefinition.LengthType.Fixed, 12, "DE 37"));
        res.put(38, new FieldDefinition(FieldDefinition.LengthType.Fixed, 6, "DE 38"));
        res.put(40, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 40"));
        res.put(41, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "DE 41"));
        res.put(42, new FieldDefinition(FieldDefinition.LengthType.Fixed, 15, "DE 42"));
        res.put(43, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 43"));
        res.put(48, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 48"));
        res.put(49, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 49"));
        res.put(50, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 50"));
        res.put(51, new FieldDefinition(FieldDefinition.LengthType.Fixed, 3, "DE 51"));
        res.put(54, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 54"));
        res.put(55, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 55"));
        res.put(62, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 62"));
        res.put(63, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 63"));
        res.put(71, new FieldDefinition(FieldDefinition.LengthType.Fixed, 8, "DE 71"));
        res.put(72, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 72"));
        res.put(73, new FieldDefinition(FieldDefinition.LengthType.Fixed, 6, "DE 73"));
        res.put(93, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 93"));
        res.put(94, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 94"));
        res.put(95, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 95"));
        res.put(100, new FieldDefinition(FieldDefinition.LengthType.Embedded, 2, "DE 100"));
        res.put(111, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 111"));
        res.put(123, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 123"));
        res.put(124, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 124"));
        res.put(125, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 125"));
        res.put(127, new FieldDefinition(FieldDefinition.LengthType.Embedded, 3, "DE 127"));

        return res;
    }

    @Override
    public void afterParse(IsoMessage msg) throws ApplicationDataParseError {
        getPdsBuffer(msg);
        try {
            while (pdsBuffer.length() > 0) {
                String tag = bufRead(4);
                String len = bufRead(3);
                Integer l = Integer.parseInt(len);
                String data = bufRead(l);

                FieldData d = new FieldData();
                d.name = "pds" + tag;
                d.parsedData = data;
                msg.fields.add(d);
            }
        } catch (Exception e) {
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
        pdsBuffer += msg.isoFields.containsKey(48) ? msg.isoFields.get(48).parsedData : "";
        pdsBuffer += msg.isoFields.containsKey(62) ? msg.isoFields.get(62).parsedData : "";
        pdsBuffer += msg.isoFields.containsKey(123) ? msg.isoFields.get(123).parsedData : "";
        pdsBuffer += msg.isoFields.containsKey(124) ? msg.isoFields.get(124).parsedData : "";
        pdsBuffer += msg.isoFields.containsKey(125) ? msg.isoFields.get(125).parsedData : "";
    }
}
