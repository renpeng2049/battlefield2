package com.soyoung.battle.field.cli;

import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.env.Environment;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.KeyValuePair;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class EnvironmentAwareCommand extends Command {

    private final OptionSpec<KeyValuePair> settingOption;

    /**
     * Construct the command with the specified command description and runnable to execute before main is invoked. Commands constructed
     * with this constructor must take ownership of configuring logging.
     *
     * @param description the command description
     * @param beforeMain the before-main runnable
     */
    public EnvironmentAwareCommand(final String description, final Runnable beforeMain) {
        super(description, beforeMain);
        this.settingOption = parser.accepts("E", "Configure a setting").withRequiredArg().ofType(KeyValuePair.class);
    }

    @Override
    protected void execute(Terminal terminal, OptionSet options) throws Exception {
        final Map<String, String> settings = new HashMap<>();
        for (final KeyValuePair kvp : settingOption.values(options)) {
            if (kvp.value.isEmpty()) {
                throw new UserException(ExitCodes.USAGE, "setting [" + kvp.key + "] must not be empty");
            }
            if (settings.containsKey(kvp.key)) {
                final String message = String.format(
                        Locale.ROOT,
                        "setting [%s] already set, saw [%s] and [%s]",
                        kvp.key,
                        settings.get(kvp.key),
                        kvp.value);
                throw new UserException(ExitCodes.USAGE, message);
            }
            settings.put(kvp.key, kvp.value);
        }

        putSystemPropertyIfSettingIsMissing(settings, "path.data", "bf.path.data");
        putSystemPropertyIfSettingIsMissing(settings, "path.home", "bf.path.home");
        putSystemPropertyIfSettingIsMissing(settings, "path.logs", "bf.path.logs");

        execute(terminal, options, createEnv(settings));
    }

    /** Create an {@link Environment} for the command to use. Overrideable for tests. */
    protected Environment createEnv(final Map<String, String> settings) throws UserException {
        final String bfPathConf = System.getProperty("bf.path.conf");
        if (bfPathConf == null) {
            throw new UserException(ExitCodes.CONFIG, "the system property [bf.path.conf] must be set");
        }

        return InternalSettingsPreparer.prepareEnvironment(Settings.EMPTY, settings, getConfigPath(bfPathConf));
    }

    /** Ensure the given setting exists, reading it from system properties if not already set. */
    private static void putSystemPropertyIfSettingIsMissing(final Map<String, String> settings, final String setting, final String key) {
        final String value = System.getProperty(key);
        if (value != null) {
            if (settings.containsKey(setting)) {
                final String message =
                        String.format(
                                Locale.ROOT,
                                "duplicate setting [%s] found via command-line [%s] and system property [%s]",
                                setting,
                                settings.get(setting),
                                value);
                throw new IllegalArgumentException(message);
            } else {
                settings.put(setting, value);
            }
        }
    }

    @SuppressForbidden(reason = "need path to construct environment")
    private static Path getConfigPath(final String pathConf) {
        return Paths.get(pathConf);
    }

    /** Execute the command with the initialized {@link }. */
    protected abstract void execute(Terminal terminal, OptionSet options, Environment env) throws Exception;

}
