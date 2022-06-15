package com.shakshin.isoparser.containers;

import java.io.InputStream;

public class BaseInputStream extends CleanInputStream {
    protected BaseInputStream(CleanInputStream in) {
        super(in);
    }

    @Override
    public Long getOffset() {
        return ((CleanInputStream) in).getOffset();
    }
}
