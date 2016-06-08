package net.wohlfart.pluto;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

public final class Logging {

    static {
        LoggerService.logTime(true);
        LoggerService.simpleClassNames(true);
        ROOT = LoggerService.forClass(Pluto.class);
    }

    public static Logger ROOT = LoggerService.forClass(Pluto.class);

    private Logging() {
        // no instances
    }

}
