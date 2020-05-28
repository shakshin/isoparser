package com.shakshin.isoparser.structure;

import com.shakshin.isoparser.configuration.Configuration;
import com.shakshin.isoparser.parser.IsoFile;
import com.shakshin.isoparser.parser.IsoMessage;

import java.util.Map;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

Abstract structure definition class
 */

public abstract class AbstractStructure {
    public class ApplicationDataParseError extends Exception {
        private String message;
        public ApplicationDataParseError(String msg) {message = msg;}
        @Override
        public String getMessage() {return message;}
    }

    private static AbstractStructure instance = null;

    public abstract Map<Integer, FieldDefinition> getIsoFieldsDefinition();

    public abstract void afterMessageParsed(IsoMessage msg) throws ApplicationDataParseError;

    public abstract void afterFileParsed(IsoFile file);

    public static AbstractStructure getStructure(Configuration cfg) {
        if (instance == null) {
            switch (cfg.structure) {
                case MASTERCARD:
                    instance = new MastercardStructure();
                    break;
                case JCB:
                    instance = new JcbStructure();
                    break;
                default:
                    instance = null;
            }
        }
        return instance;
    }

}
