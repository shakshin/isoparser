package com.shakshin.isoparser;

import com.shakshin.isoparser.configuration.Configuration;

public class Trace {
    public static Configuration cfg = null;
    public static void log(String who, String msg) {
        if (cfg == null)
            cfg = Configuration.get();

        if (cfg != null && !cfg.trace) return;

        System.out.println(String.format("[%s]: %s", who, msg));

    };

    public static void error(String who, String msg) {
        System.err.println(String.format("[error] [%s]: %s", who, msg));
    }
}
