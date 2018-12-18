package com.shakshin.isoparser.structure;

import com.shakshin.isoparser.configuration.Configuration;
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

    public abstract Map<Integer, FieldDefinition> getIsoFieldsDefinition();

    public abstract void afterParse(IsoMessage msg) throws ApplicationDataParseError;

    public static AbstractStructure getStructure(Configuration cfg) {
        switch (cfg.structure) {
            case MASTERCARD:
                return new MastercardStructure();
            case JCB:
                return new JcbStructure();
            default:
                return null;
        }
    }

}
