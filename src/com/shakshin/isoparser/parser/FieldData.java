package com.shakshin.isoparser.parser;

import com.shakshin.isoparser.configuration.Configuration;

import java.util.ArrayList;
import java.util.Arrays;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

Parsed field data class
 */

public class FieldData {
    public String name;
    public byte[] rawData;
    public byte[] rawConvertedData;
    public String parsedData;
    public boolean masked = false;

    public ArrayList<String> appParserProblems = new ArrayList<String>();
    public ArrayList<FieldData> children = new ArrayList<FieldData>();

    private static final String fieldSplitter = "\n-------------------------------------------------------";

    public String asText(Configuration cfg) {
        if (masked)
            return name + ": (masked sensitive data)";

        String res = name + ": '" + parsedData + "'";

        if (appParserProblems.size() > 0) {
            res += "\n  Parsing problems:";
            for (String p : appParserProblems) {
                res += "\n     " + p;
            }
            res += "\n";
        }

        boolean needSplitter = false;

        if (cfg.raw) {
            if (rawData != null) {
                res += "\nRAW: " + Utils.bin2hex(rawData);
                if (rawConvertedData != null && !Arrays.equals(rawConvertedData, rawData))
                    res += "\nRAW (converted): " + Utils.bin2hex(rawConvertedData);

                needSplitter = true;
            }

        }

        if (children.size() > 0 ) {
            needSplitter = true;
            String addRes = "";
            for (FieldData child : children) {
                String cdata = "\n" + child.asText(cfg);
                addRes += cdata.replaceAll("\n", "\n    ");
            }

            res += addRes;
        }

        if (needSplitter)
            res += fieldSplitter;

        return res;
    }

    @Override
    public String toString() {
        return "FieldData{"+name+" => "+parsedData+"}";
    }
}
