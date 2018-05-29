package com.shakshin.isoparser.structure;

public class FieldDefinition {
    public enum LengthType { Fixed, Embedded};
    public String name;
    public LengthType lengthType;
    public Integer length;
    public boolean binary = false;
    public FieldDefinition(LengthType ltype, Integer l, String nm, boolean bin) {
        lengthType = ltype;
        length = l;
        name = nm;
        binary = bin;
    }
}
