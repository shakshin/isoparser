package com.shakshin.isoparser.configuration;

import java.nio.charset.Charset;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

Working mode configurator and command line arguments parser
 */

public class Configuration {
    public enum ContainerType { NONE, RDW, MCPREEDIT, MC1014 };
    public enum DataEncoding { ASCII, EBCDIC }
    public enum Structure { MASTERCARD, JCB }

    public ContainerType container;
    public DataEncoding encoding;
    public Structure structure;
    public String inputFile;
    public boolean raw;
    public boolean masked;

    public boolean isValid() {
        if (inputFile == null)
            return false;

        if (container == null)
            return false;

        if (encoding == null)
            return false;

        if (structure == null)
            return false;

        return true;
    }

    public Charset getCharset() {
        switch (encoding) {
            case ASCII:
                return Charset.forName("ASCII");
            case EBCDIC:
                return Charset.forName("IBM500");
            default:
                return null;
        }
    }

    public void printHelp() {
        System.out.println(
                "ISO 8583 parser by Sergey V. Shakshin" +
                        "\nOptions:" +
                        "\n     -input <path> - specify input file" +
                        "\n" +
                        "\n     -container <container> - specify container layout:" +
                        "\n         None - clean ISO 8583 file (default)" +
                        "\n         RDW - layout with RDW prefix" +
                        "\n         McPreEdit - Mastercard Pre-Edit layout" +
                        "\n         Mc1014 - Mastercard 1014-block layout" +
                        "\n" +
                        "\n     -structure <structure> - specify application-level ISO 8583 structure (fields definition):" +
                        "\n         MC - Mastercard IPM file (default)" +
                        "\n         JCB - JCB Interchange file" +
                        "\n" +
                        "\n     -encoding <encoding> - specify file encoding:" +
                        "\n         ASCII (default)" +
                        "\n         EBCDIC" +
                        "\n" +
                        "\n     -raw - include RAW data for fields" +
                        "\n" +
                        "\n     -mask - mask sensitive data (PAN)" +
                        "\n"

        );
    }

    public Configuration(String[] args) {
        container = ContainerType.NONE;
        encoding = DataEncoding.ASCII;
        structure = Structure.MASTERCARD;
        inputFile = null;
        raw = false;
        masked = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i].toUpperCase()) {
                case "-RAW":
                    raw = true;
                    break;
                case "-MASK":
                    masked = true;
                    break;
                case "-INPUT":
                    i++;
                    inputFile = args[i];
                    break;
                case "-STRUCTURE":
                    i++;
                    switch (args[i].toUpperCase()) {
                        case "MASTERCARD":
                        case "MC":
                            structure = Structure.MASTERCARD;
                            break;
                        case "JCB":
                            structure = Structure.JCB;
                            break;
                        default:
                            System.out.println("Unknown structure: " + args[i]);
                            structure = null;
                            break;
                    }
                    break;
                case "-CONTAINER":
                    i++;
                    switch (args[i].toUpperCase()) {
                        case "NONE":
                            container = ContainerType.NONE;
                            break;
                        case "RDW":
                            container = ContainerType.RDW;
                            break;
                        case "MCPREEDIT":
                            container = ContainerType.MCPREEDIT;
                            break;
                        case "MC1014":
                            container = ContainerType.MC1014;
                            break;
                        default:
                            System.out.println("Unknown container: " + args[i]);
                            container = null;
                            break;
                    }
                    break;
                case "-ENCODING":
                    i++;
                    switch (args[i].toUpperCase()) {
                        case "ASCII":
                            encoding = DataEncoding.ASCII;
                            break;
                        case "EBCDIC":
                            encoding = DataEncoding.EBCDIC;
                            break;
                        default:
                            System.out.println("Unknown encoding: " + args[i]);
                            encoding = null;
                            break;
                    }
                    break;
                default:
                    System.out.println("Unknown option: " + args[i]);
                    break;
            }
        }
    }
}
