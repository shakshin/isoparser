package com.shakshin.isoparser;

import com.shakshin.isoparser.configuration.Configuration;
import com.shakshin.isoparser.containers.Container;
import com.shakshin.isoparser.parser.IsoFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

CLI entry point
 */

public class Main {

    public static void main(String[] args) {

        Configuration cfg = new Configuration(args);
        if (!cfg.isValid()) {
            cfg.printHelp();
            return;
        }
        InputStream in;
        try {
            in = Container.getContainerStream(cfg, new FileInputStream(cfg.inputFile));
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found");
            return;
        } catch (IOException e) {
            System.out.println("File was not opened: " + e.getMessage());
            return;
        }
        IsoFile file = new IsoFile(cfg, in);

        if (file.messages.size() > 0 )
            System.out.println(file.asText());
        else
            System.out.println("No messages parsed");


    }
}
