package com.shakshin.isoparser.structure;

import com.shakshin.isoparser.configuration.Configuration;
import com.shakshin.isoparser.parser.IsoMessage;

import java.util.HashMap;
import java.util.Map;

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
