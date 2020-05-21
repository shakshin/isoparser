package com.shakshin.isoparser;

import com.shakshin.isoparser.configuration.Configuration;

public class Trace {
    public static Configuration cfg = null;
    public static void log(String who, String msg) {
        if (cfg == null)
            cfg = Configuration.get();

        if (!cfg.trace) return;

        System.out.println(String.format("[%s]: %s", who, msg));

    };
}
