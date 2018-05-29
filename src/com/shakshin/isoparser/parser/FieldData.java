package com.shakshin.isoparser.parser;

import com.shakshin.isoparser.configuration.Configuration;

import java.util.ArrayList;

public class FieldData {
    public String name;
    public byte[] rawData;
    public byte[] rawConvertedData;
    public String parsedData;
    public ArrayList<FieldData> children = new ArrayList<FieldData>();

    public String asText(Configuration cfg) {
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
