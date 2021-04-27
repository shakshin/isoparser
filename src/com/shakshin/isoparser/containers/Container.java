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
    public static InputStream getContainerStream(Configuration cfg, InputStream raw) throws IOException {
        switch (cfg.container) {
            case NONE:
                return raw;
            case RDW:
                return new RDWInputStream(raw, cfg.mainframe);
            case MC1014:
                return new RDWInputStream(new IPMBlockedInputStream(raw), cfg.mainframe);
            case MCPREEDIT:
                return new IPMPreEditInputStream(raw);
            default:
                Trace.error("Container","Unsupported container: " + cfg.container);
                return null;
        }
    }
}
