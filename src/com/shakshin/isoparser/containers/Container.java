package com.shakshin.isoparser.containers;

import com.shakshin.isoparser.Trace;
import com.shakshin.isoparser.configuration.Configuration;
import com.shakshin.isoparser.containers.mastercard.IPMBlockedInputStream;
import com.shakshin.isoparser.containers.mastercard.IPMPreEditInputStream;

import java.io.IOException;
import java.io.InputStream;

/*
ISO 8583 parser
Original code by Sergey V. Shakshin (rigid.mgn@gmail.com)

Container (layout) selector
 */

public class Container {
    public static CleanInputStream getContainerStream(Configuration cfg, InputStream raw) throws IOException {
        CleanInputStream clean = new CleanInputStream(raw);

        switch (cfg.container) {
            case NONE:
                return clean;
            case RDW:
                return new RDWInputStream(clean, cfg.mainframe);
            case MC1014:
                return new RDWInputStream(new IPMBlockedInputStream(clean), cfg.mainframe);
            case MCPREEDIT:
                return new IPMPreEditInputStream(clean);
            default:
                Trace.error("Container","Unsupported container: " + cfg.container);
                return null;
        }
    }
}
