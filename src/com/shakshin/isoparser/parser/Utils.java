package com.shakshin.isoparser.parser;

import com.shakshin.isoparser.configuration.Configuration;
import com.shakshin.isoparser.structure.FieldDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Utils {
    public static byte[] convertBytes(byte[] src, Charset from) {
        if (from == Charset.forName("ASCII"))
            return src;

        return new String(src, from).getBytes(Charset.forName("ASCII"));
    }

    public static byte[] readFromStream(InputStream in, FieldDefinition def, Configuration cfg) throws IOException {
        if (def.lengthType == FieldDefinition.LengthType.Embedded) {
            byte[] rawLength = new byte[def.length];
            if (in.read(rawLength) < def.length)
                throw new IOException("No enough bytes to read embedded length; Field: " + def.name);

            Integer length;

            try {
                rawLength = convertBytes(rawLength, cfg.getCharset());
                String strLength = new String(rawLength);
                length = Integer.parseInt(strLength);
            } catch (Exception e) {
                throw new IOException("Can not parse embedded length. Field: " + def.name + "; " + e.getMessage(), e);
            }

            try {
                byte[] buff = readFromStreamFixedLen(in, length);
                return buff;
            } catch (Exception e) {
                throw new IOException("Can not read data: " + e.getMessage()+ "; Field: " + def.name, e);
            }

        } else if (def.lengthType == FieldDefinition.LengthType.Fixed) {
            try {
                byte[] buff = readFromStreamFixedLen(in, def.length);
                return buff;
            } catch (Exception e) {
                throw new IOException("Can not read data: " + e.getMessage()+ "; Field: " + def.name, e);
            }
        } else {
            throw new IOException("Unsupported length type: " + def.lengthType + "; Field: " + def.name);
        }
    }

    public static byte[] readFromStreamFixedLen(InputStream in, Integer length) throws IOException {
        byte[] buff = new byte[length];
        if (in.read(buff) < length)
            throw new IOException("No enough bytes to read data");

        return buff;
    }

    public static String bin2hex(byte[] bin) {
        String res = "";
        for (int i = 0; i  < bin.length; i++) {
            if (i > 0)
                res += " ";
            res += String.format("%02x", bin[i]);
        }

        return res;
    }
}
