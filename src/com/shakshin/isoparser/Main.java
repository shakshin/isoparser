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

        Configuration cfg = Configuration.get(args);
        if (!cfg.isValid()) {
            cfg.printHelp();
            return;
        }
        Trace.log("main", "Configuration prepared");
        InputStream in;
        try {
            in = Container.getContainerStream(cfg, new FileInputStream(cfg.inputFile));
            Trace.log("main", "Input stream opened");
        } catch (FileNotFoundException e) {
            Trace.log("main", "Input file not found");
            return;
        } catch (IOException e) {
            Trace.log("main", "Can not open file: " + e.getMessage());
            return;
        }
        IsoFile file = new IsoFile(cfg, in);

        if (file.messages.size() > 0 ) {
            if (!cfg.nodump) {
                System.out.println(file.asText());
            } else {
                System.out.println(String.format("%d messages parsed", file.messages.size()));
            }
        } else {
            System.out.println("No messages parsed");
        }


    }
}
