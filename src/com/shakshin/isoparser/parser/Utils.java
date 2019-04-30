package com.shakshin.isoparser.parser;

import com.shakshin.isoparser.Trace;
import com.shakshin.isoparser.configuration.Configuration;
import com.shakshin.isoparser.structure.FieldDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

Utility class
 */

public class Utils {
    public static byte[] convertBytes(byte[] src, Charset from) {
        if (from == Charset.forName("ASCII"))
            return src;

        return new String(src, from).getBytes(Charset.forName("ASCII"));
    }

    public static byte[] readFromStream(InputStream in, FieldDefinition def, Configuration cfg) throws IOException {
        if (def.lengthType == FieldDefinition.LengthType.Embedded) {
            byte[] rawLength = new byte[def.length];
            if (in.read(rawLength) < def.length) {
                Trace.log("Utils", "Can not read embedded length for field: " + def.name);
                throw new IOException("No enough bytes to read embedded length; Field: " + def.name);
            }

            Integer length;

            try {
                rawLength = convertBytes(rawLength, cfg.getCharset());
                String strLength = new String(rawLength);
                length = Integer.parseInt(strLength);
            } catch (Exception e) {
                Trace.log("Utils", "Can not parse embedded length for field: " + def.name);
                throw new IOException("Can not parse embedded length. Field: " + def.name + "; " + e.getMessage(), e);
            }

            try {
                byte[] buff = readFromStreamFixedLen(in, length);
                return buff;
            } catch (Exception e) {
                Trace.log("Utils", "Ca not read data for field: " + def.name);
                throw new IOException("Can not read data: " + e.getMessage()+ "; Field: " + def.name, e);
            }

        } else if (def.lengthType == FieldDefinition.LengthType.Fixed) {
            try {
                byte[] buff = readFromStreamFixedLen(in, def.length);
                return buff;
            } catch (Exception e) {
                Trace.log("Utils", "Can not rad data for field: " + def.name);
                throw new IOException("Can not read data: " + e.getMessage()+ "; Field: " + def.name, e);
            }
        } else {
            Trace.log("Utils", "Unsupported field length type");
            throw new IOException("Unsupported length type: " + def.lengthType + "; Field: " + def.name);
        }
    }

    public static byte[] readFromStreamFixedLen(InputStream in, Integer length) throws IOException {
        byte[] buff = new byte[length];
        if (in.read(buff) < length) {
            Trace.log("Utils", "No enough data");
            throw new IOException("No enough bytes to read data");
        }

        return buff;
    }

    public static String bin2hex(byte bin) {
        return String.format("%02x", bin);
    }

    public static String bin2hex(byte[] bin) {
        String res = "";
        for (int i = 0; i  < bin.length; i++) {
            if (i > 0)
                res += " ";
            res += bin2hex(bin[i]);
        }

        return res;
    }

    public static void parseBerTLV(byte[] raw, List<FieldData> target, List<String> problems) {
        int offset = 0;
        while (offset < raw.length) {
            String tag = null;
            String lengthStr = null;
            Integer length = null;
            String data = null;

            tag = Utils.bin2hex(raw[offset++]);

            if (tag.substring(1).equals("f")) {
                if (offset == raw.length) {
                    Trace.log("Utils", "BerTLV tag read failed: " + tag);
                    problems.add("Can not read next BerTLV tag name: no enough bytes in buffer. Current read data: " + tag);
                    break;
                }

                while (true) {
                    byte lastTagByte = raw[offset++];
                    tag += Utils.bin2hex(lastTagByte);
                    BitSet bs = BitSet.valueOf(new byte[]{lastTagByte});
                    if (!bs.get(7)) {
                        break;
                    }
                }

            }

            if (offset == raw.length) {
                Trace.log("Utils", "Can not read BerTLV tag length: " + tag);
                problems.add("Can not read next BerTLV tag length: no enough bytes in buffer. Tag name: " + tag);
                break;
            }
            lengthStr = Utils.bin2hex(raw[offset++]);
            length = Integer.decode("0x" + lengthStr);

            if (offset + length > raw.length) {
                Trace.log("Utils", "Can not read BerTLV tag data: " + tag);
                problems.add("Can not read next BerTLV tag data: no enough bytes in buffer. Tag name: " + tag + "; Declared length: " + length.toString() + "; Actual bytes: " + (raw.length - offset));
                break;
            }
            byte[] dataR = new byte[length];
            for (int i = 0; i < length; i++)
                dataR[i] = raw[offset + i];

            data = Utils.bin2hex(dataR);

            offset += length;

            FieldData fd = new FieldData();
            fd.name = tag;
            fd.parsedData = data;

            target.add(fd);
        }
    }
}
