package com.shakshin.isoparser.structure;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

ISO 85883 field definition class
 */

public class FieldDefinition {
    public enum LengthType { Fixed, Embedded};
    public String name;
    public LengthType lengthType;
    public Integer length;
    public boolean binary = false;
    public boolean mask = false;
    public FieldDefinition(LengthType ltype, Integer l, String nm, boolean bin) {
        lengthType = ltype;
        length = l;
        name = nm;
        binary = bin;
    }
    public FieldDefinition(LengthType ltype, Integer l, String nm, boolean bin, boolean maskable) {
        lengthType = ltype;
        length = l;
        name = nm;
        binary = bin;
        mask = maskable;
    }
}
