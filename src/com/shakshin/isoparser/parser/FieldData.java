package com.shakshin.isoparser.parser;

import com.shakshin.isoparser.configuration.Configuration;

import java.util.ArrayList;

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
    public ArrayList<FieldData> children = new ArrayList<FieldData>();

    public String asText(Configuration cfg) {
        if (masked)
            return name + ": (masked sensitive data)";

        String res = name + ": '" + parsedData + "'";
        if (cfg.raw) {
            if (rawData != null) {
                res += "\nRAW: " + Utils.bin2hex(rawData);
                if (rawConvertedData != null && rawConvertedData != rawData)
                    res += "\nRAW (converted): " + Utils.bin2hex(rawConvertedData);

                res += "\n-------------------------------------------------------";
            }

        }

        return res;
    }
}
