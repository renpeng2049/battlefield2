package com.soyoung.battle.field.cli;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public abstract class Command implements Closeable {


    /** A description of the command, used in the help output. */
    protected final String description;

    private final Runnable beforeMain;

    /** The option parser for this command. */
    protected final OptionParser parser = new OptionParser();


    private final OptionSpec<Void> helpOption = parser.acceptsAll(Arrays.asList("h", "help"), "show help").forHelp();
    private final OptionSpec<Void> silentOption = parser.acceptsAll(Arrays.asList("s", "silent"), "show minimal output");
    private final OptionSpec<Void> verboseOption =
            parser.acceptsAll(Arrays.asList("v", "verbose"), "show verbose output").availableUnless(silentOption);


    /**
     * Construct the command with the specified command description and runnable to execute before main is invoked.
     *
     * @param description the command description
     * @param beforeMain the before-main runnable
     */
    public Command(final String description, final Runnable beforeMain) {
        this.description = description;
        this.beforeMain = beforeMain;
    }

    private Thread shutdownHookThread;



    /** Parses options for this command from args and executes it. */
    public final int main(String[] args,Terminal terminal) throws Exception {

        terminal.println(">>>>>>>");
        if(addShutdownHook()){

            shutdownHookThread = new Thread(() -> {
                try {
                    this.close();
                } catch (final IOException e) {
                    try (
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw)) {
                        e.printStackTrace(pw);
                        terminal.println(sw.toString());
                    } catch (final IOException impossible) {
                        // StringWriter#close declares a checked IOException from the Closeable interface but the Javadocs for StringWriter
                        // say that an exception here is impossible
                        throw new AssertionError(impossible);
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHookThread);
        }
        beforeMain.run();


        try {
            mainWithoutErrorHandling(args, terminal);
        } catch (OptionException e) {
            printHelp(terminal);
            terminal.println(Terminal.Verbosity.SILENT, "ERROR: " + e.getMessage());
            return ExitCodes.USAGE;
        } catch (UserException e) {
            if (e.exitCode == ExitCodes.USAGE) {
                printHelp(terminal);
            }
            terminal.println(Terminal.Verbosity.SILENT, "ERROR: " + e.getMessage());
            return e.exitCode;
        }
        terminal.println(">>>>>>>>2");
        return ExitCodes.OK;

    }



    void mainWithoutErrorHandling(String[] args, Terminal terminal) throws Exception {
        final OptionSet options = parser.parse(args);

        if (options.has(helpOption)) {
            printHelp(terminal);
            return;
        }

        if (options.has(silentOption)) {
            terminal.setVerbosity(Terminal.Verbosity.SILENT);
        } else if (options.has(verboseOption)) {
            terminal.setVerbosity(Terminal.Verbosity.VERBOSE);
        } else {
            terminal.setVerbosity(Terminal.Verbosity.NORMAL);
        }

        execute(terminal, options);
    }


    /** Prints a help message for the command to the terminal. */
    private void printHelp(Terminal terminal) throws IOException {
        terminal.println(description);
        terminal.println("");
        printAdditionalHelp(terminal);
        parser.printHelpOn(terminal.getWriter());
    }

    /** Prints additional help information, specific to the command */
    protected void printAdditionalHelp(Terminal terminal) {}



    /**
     * Executes this command.
     *
     * Any runtime user errors (like an input file that does not exist), should throw a {@link }. */
    protected abstract void execute(Terminal terminal, OptionSet options) throws Exception;


    /**
     * Return whether or not to install the shutdown hook to cleanup resources on exit. This method should only be overridden in test
     * classes.
     *
     * @return whether or not to install the shutdown hook
     */
    protected boolean addShutdownHook() {
        return true;
    }

    @SuppressForbidden(reason = "Allowed to exit explicitly from #main()")
    protected static void exit(int status) {
        System.exit(status);
    }

    @Override
    public void close() throws IOException {

    }
}
