package com.soyoung.battle.field.bootstrap;

import com.soyoung.battle.field.*;
import com.soyoung.battle.field.cli.EnvironmentAwareCommand;
import com.soyoung.battle.field.cli.ExitCodes;
import com.soyoung.battle.field.cli.Terminal;
import com.soyoung.battle.field.cli.UserException;
import com.soyoung.battle.field.monitor.jvm.JvmInfo;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.PathConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

class Battlefield extends EnvironmentAwareCommand {

    final Logger logger = LogManager.getLogger(Battlefield.class);

    private final OptionSpecBuilder versionOption;
    private final OptionSpecBuilder daemonizeOption;
    private final OptionSpec<Path> pidfileOption;
    private final OptionSpecBuilder quietOption;

    // visible for testing
    Battlefield() {
        super("starts battlefield", () -> {}); // we configure logging later so we override the base class from configuring logging
        versionOption = parser.acceptsAll(Arrays.asList("V", "version"),
                "Prints battlefield version information and exits");
        daemonizeOption = parser.acceptsAll(Arrays.asList("d", "daemonize"),
                "Starts battlefield in the background")
                .availableUnless(versionOption);
        pidfileOption = parser.acceptsAll(Arrays.asList("p", "pidfile"),
                "Creates a pid file in the specified path on start")
                .availableUnless(versionOption)
                .withRequiredArg()
                .withValuesConvertedBy(new PathConverter());
        quietOption = parser.acceptsAll(Arrays.asList("q", "quiet"),
                "Turns off standard output/error streams logging in console")
                .availableUnless(versionOption)
                .availableUnless(daemonizeOption);
    }


    /**
     * Main entry point for starting battlefield
     */
    public static void main(final String[] args) throws Exception {


        final Battlefield battlefield = new Battlefield();
        int status = main(args, battlefield,Terminal.DEFAULT);

    }


    static int main(final String[] args, final Battlefield battlefield,Terminal terminal) throws Exception {
        return battlefield.main(args,terminal);
    }

    @Override
    protected void execute(Terminal terminal, OptionSet options, String env) throws UserException {
        if (options.nonOptionArguments().isEmpty() == false) {
            throw new UserException(ExitCodes.USAGE, "Positional arguments not allowed, found " + options.nonOptionArguments());
        }
        if (options.has(versionOption)) {
            terminal.println("Version: " + Version.displayVersion(Version.CURRENT, Build.CURRENT.isSnapshot())
                    + ", Build: " + Build.CURRENT.shortHash() + "/" + Build.CURRENT.date()
                    + ", JVM: " + JvmInfo.jvmInfo().version());
            return;
        }

        final boolean daemonize = options.has(daemonizeOption);
        final Path pidFile = pidfileOption.value(options);
        final boolean quiet = options.has(quietOption);

        try {
            init();
        } catch (Exception e) {
            throw new UserException(ExitCodes.CONFIG, e.getMessage());
        }
    }


    void init() throws Exception {
        logger.info("init >>>>");
        try {
            Bootstrap.init();
        } catch (Exception e) {
            // format exceptions to the console in a special way
            // to avoid 2MB stacktraces from guice, etc.
            throw new Exception(e);
        }
    }

    /**
     * Required method that's called by Apache Commons procrun when
     * running as a service on Windows, when the service is stopped.
     *
     * http://commons.apache.org/proper/commons-daemon/procrun.html
     *
     * NOTE: If this method is renamed and/or moved, make sure to
     * update battlefield-service.bat!
     */
    static void close(String[] args) throws IOException {
        Bootstrap.stop();
    }
}
