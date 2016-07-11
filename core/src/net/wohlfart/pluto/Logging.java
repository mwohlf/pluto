package net.wohlfart.pluto;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

public final class Logging {

    static {
        LoggerService.logTime(true);
        LoggerService.simpleClassNames(true);
    }

    public static final Logger ROOT = LoggerService.forClass(Pluto.class);

    private Logging() {
        // no instances
    }

}
