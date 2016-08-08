package net.wohlfart.pluto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Logging {

    static {
        //LoggerService.logTime(true);
        //LoggerService.simpleClassNames(true);
    }

    public static final Logger ROOT = LoggerFactory.getLogger(Pluto.class);

    private Logging() {
        // no instances
    }

}
