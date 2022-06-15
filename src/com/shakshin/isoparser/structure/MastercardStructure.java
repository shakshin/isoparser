package com.shakshin.isoparser.structure;

import com.shakshin.isoparser.Trace;
import com.shakshin.isoparser.parser.FieldData;
import com.shakshin.isoparser.parser.IsoFile;
import com.shakshin.isoparser.parser.IsoMessage;
import com.shakshin.isoparser.parser.Utils;

import java.util.*;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

Mastercard IPM File structure definition class
 */

public class MastercardStructure extends AbstractStructure {
    private String pdsBuffer = "";

    private Long checksum = Long.valueOf(0);
    private boolean checksumProblem = false;

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
    public void afterMessageParsed(IsoMessage msg) throws ApplicationDataParseError {

        if (msg.isoFields.containsKey(4)) {
            try {
                String de4 = msg.isoFields.get(4).parsedData;
                Long de4l = Long.valueOf(de4);
                checksum += de4l;
            } catch (Throwable e) {
                Trace.log("MC", "DE4 could not be parsed to numeric value: " + e.getMessage());
                checksumProblem = true;
            }
        }

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

    @Override
    public void afterFileParsed(IsoFile file) {
        file.checksum = checksum.toString();
        file.checksumProblems = checksumProblem;
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

    private void runSettleReport(IsoFile file) {
        if (file.messages.size() > 0 ) {
            HashMap<String, String> spdDt = new HashMap<>();
            HashMap<String, Long> spdAmt = new HashMap<>();
            HashMap<String, Long> fpdAmt = new HashMap<>();

            Set<IsoMessage> fpds = new HashSet<>();
            Set<IsoMessage> spds = new HashSet<>();
            HashMap<String, HashSet<IsoMessage>> kSpds = new HashMap<>();

            for (IsoMessage msg : file.messages) {
                String mti = msg.header.mti;
                String proc = msg.isoFields.get(24).parsedData;
                String key = null;

                if (!mti.equals("1644")) continue;
                //if (proc != "685" && proc != "688")  continue;

                FieldData f50 = msg.isoFields.get(50);
                FieldData f49 = msg.isoFields.get(49);
                String f300 = msg.namedFields.get("pds0300");
                key = (f50 == null ? (f49 == null ? "null" : f49.parsedData) : f50.parsedData) + ":" + f300;

                switch (proc) {
                    case "685":
                        fpds.add(msg);
                        String f394 = msg.namedFields.get("pds0394");
                        String f395 = msg.namedFields.get("pds0395");
                        Long l394 = f394 == null ? 0 : Long.parseLong(f394.substring(1));
                        Long l395 = f395 == null ? 0 : Long.parseLong(f395.substring(1));
                        fpdAmt.put(key, (fpdAmt.get(key) == null ? 0 : fpdAmt.get(key)) + l394 + l395);
                        break;

                    case "688":
                        spds.add(msg);

                        HashSet<IsoMessage> ss = kSpds.get(key);
                        if (ss == null) ss = new HashSet<>();
                        ss.add(msg);
                        kSpds.put(key, ss);

                        String f359 = msg.namedFields.get( "pds0359");
                        if (f359 != null) spdDt.put(key, f359);

                        String f390 = msg.namedFields.get("pds0390");
                        String f391 = msg.namedFields.get("pds0391");
                        Long l390 = f390 == null ? 0 : Long.parseLong(f390.substring(1));
                        Long l391 = f391 == null ? 0 : Long.parseLong(f391.substring(1));

                        Long samt = l390 + l391;
                        spdAmt.put(key, (spdAmt.get(key) == null ? 0 : spdAmt.get(key)) + samt);

                        String f392 = msg.namedFields.get( "pds0392");
                        if (f392 != null) {
                            for (String item : f392.split("(?<=\\G.{18})")) {
                                String sf2 = item.substring(3);
                                Long sf2l = Long.parseLong(sf2);
                                spdAmt.put(key, (spdAmt.get(key) == null ? 0 : spdAmt.get(key)) + sf2l);
                            }
                        }
                        String f393 = msg.namedFields.get( "pds0393");
                        if (f393 != null) {
                            for (String item : f393.split("(?<=\\G.{18})")) {
                                String sf2 = item.substring(3);
                                Long sf2l = Long.parseLong(sf2);
                                spdAmt.put(key, (spdAmt.get(key) == null ? 0 : spdAmt.get(key)) + sf2l);
                            }
                        }

                        break;
                    default:
                        continue;
                }
            }

            HashSet<String> keys = new HashSet<>();
            keys.addAll(fpdAmt.keySet());
            keys.addAll(spdAmt.keySet());

            System.out.println("File: '" + file.fileName + "'; FPDS: " + fpdAmt.size() + "; SPDS: " + spdAmt.size());

            for (String key : keys) {
                Long samt = spdAmt.get(key);
                Long famt = fpdAmt.get(key);
                System.out.print((famt != null && samt != null && famt.intValue() == samt.intValue() ? "  MATCHED" : "UNMATCHED") + ": " );
                System.out.print("Key " + key + "; ");
                System.out.print("FPD " + (famt == null ? "NOT FOUND" : fpdAmt.get(key).toString()) + "; ");
                System.out.print("SPD " +  (samt == null ? "NOT FOUND" : samt.toString()) + "; ");


                System.out.println();
            }
            System.out.println();
        }
    }

    @Override
    public void runReport(IsoFile file, String report) {
        switch (report.toUpperCase()) {
            case "SETTLE":
            case "STM":
            case "SETTLEMENT":
                runSettleReport(file);
                break;
            default:
                Trace.error("MC", "No such report");
        }
    }
}
