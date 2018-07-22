package com.soyoung.battle.field.env;

import com.soyoung.battle.field.common.io.PathUtils;
import com.soyoung.battle.field.common.setting.Setting;
import com.soyoung.battle.field.common.setting.Settings;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Environment {

    public static final Setting<String> PATH_HOME_SETTING = Setting.simpleString("path.home", Setting.Property.NodeScope);
    public static final Setting<List<String>> PATH_DATA_SETTING =
            Setting.listSetting("path.data", Collections.emptyList(), Function.identity(), Setting.Property.NodeScope);
    public static final Setting<String> PATH_LOGS_SETTING =
            new Setting<>("path.logs", "", Function.identity(), Setting.Property.NodeScope);
    public static final Setting<List<String>> PATH_REPO_SETTING =
            Setting.listSetting("path.repo", Collections.emptyList(), Function.identity(), Setting.Property.NodeScope);
    public static final Setting<String> PATH_SHARED_DATA_SETTING = Setting.simpleString("path.shared_data", Setting.Property.NodeScope);
    public static final Setting<String> PIDFILE_SETTING = Setting.simpleString("pidfile", Setting.Property.NodeScope);


    private final Settings settings;

    private final Path configFile;

    private final Path logsFile;

    private final Path dataFile;

    /** Path to the temporary file directory used by the JDK */
    private final Path tmpFile = PathUtils.get(System.getProperty("java.io.tmpdir"));


    public Environment(Settings settings, Path configPath){

        final Path homeFile;
        if (PATH_HOME_SETTING.exists(settings)) {
            homeFile = PathUtils.get(PATH_HOME_SETTING.get(settings)).normalize();
        } else {
            throw new IllegalStateException(PATH_HOME_SETTING.getKey() + " is not configured");
        }

        if (configPath != null) {
            configFile = configPath.normalize();
        } else {
            configFile = homeFile.resolve("config");
        }

        List<String> dataPaths = PATH_DATA_SETTING.get(settings);

        // this is trappy, Setting#get(Settings) will get a fallback setting yet return false for Settings#exists(Settings)
        if (PATH_LOGS_SETTING.exists(settings)) {
            logsFile = PathUtils.get(PATH_LOGS_SETTING.get(settings)).normalize();
        } else {
            logsFile = homeFile.resolve("logs");
        }

        dataFile = homeFile.resolve("data");

        Settings.Builder finalSettings = Settings.builder().put(settings);
        finalSettings.put(PATH_HOME_SETTING.getKey(), homeFile);
        if (PATH_DATA_SETTING.exists(settings)) {
            finalSettings.putList(PATH_DATA_SETTING.getKey(), dataPaths);
        }
        finalSettings.put(PATH_LOGS_SETTING.getKey(), logsFile.toString());
        this.settings = finalSettings.build();

    }


    public Path configFile() {
        return configFile;
    }

    public Path logsFile() {
        return logsFile;
    }

    public Path dataFile() {
        return dataFile;
    }


    /** Path to the default temp directory used by the JDK */
    public Path tmpFile() {
        return tmpFile;
    }

    /**
     * The settings used to build this environment.
     */
    public Settings settings() {
        return this.settings;
    }
}
