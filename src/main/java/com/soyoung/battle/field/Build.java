package com.soyoung.battle.field;


import java.net.URL;
import java.security.CodeSource;

/**
 * Information about a build of battlefield.
 */
public class Build {
    /**
     * The current build of battlefield. Filled with information scanned at
     * startup from the jar.
     */
    public static final Build CURRENT;

    static {
        final String shortHash;
        final String date;
        final boolean isSnapshot;

        final String esPrefix = "battlefield-" + Version.CURRENT;
        final URL url = getBattlefieldCodeSourceLocation();
        final String urlStr = url == null ? "" : url.toString();
        if (urlStr.startsWith("file:/") && (urlStr.endsWith(esPrefix + ".jar") || urlStr.endsWith(esPrefix + "-SNAPSHOT.jar"))) {

            shortHash = "Unknown";
            date = "Unknown";
            isSnapshot = true;
        } else {
            // not running from the official battlefield jar file (unit tests, IDE, uber client jar, shadiness)
            shortHash = "Unknown";
            date = "Unknown";
            final String buildSnapshot = System.getProperty("build.snapshot");
            if (buildSnapshot != null) {
                try {
                    Class.forName("com.carrotsearch.randomizedtesting.RandomizedContext");
                } catch (final ClassNotFoundException e) {
                    // we are not in tests but build.snapshot is set, bail hard
                    throw new IllegalStateException("build.snapshot set to [" + buildSnapshot + "] but not running tests");
                }
            } else {
            }
            isSnapshot = true;
        }
        if (shortHash == null) {
            throw new IllegalStateException("Error finding the build shortHash. " +
                    "Stopping battlefield now so it doesn't run in subtly broken ways. This is likely a build bug.");
        }
        if (date == null) {
            throw new IllegalStateException("Error finding the build date. " +
                    "Stopping battlefield now so it doesn't run in subtly broken ways. This is likely a build bug.");
        }

        CURRENT = new Build(shortHash, date, isSnapshot);
    }

    private final boolean isSnapshot;

    /**
     * The location of the code source for battlefield
     *
     * @return the location of the code source for battlefield which may be null
     */
    static URL getBattlefieldCodeSourceLocation() {
        final CodeSource codeSource = Build.class.getProtectionDomain().getCodeSource();
        return codeSource == null ? null : codeSource.getLocation();
    }

    private final String shortHash;
    private final String date;

    public Build(String shortHash, String date, boolean isSnapshot) {
        this.shortHash = shortHash;
        this.date = date;
        this.isSnapshot = isSnapshot;
    }

    public String shortHash() {
        return shortHash;
    }

    public String date() {
        return date;
    }


    public boolean isSnapshot() {
        return isSnapshot;
    }

    @Override
    public String toString() {
        return "[" + shortHash + "][" + date + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Build build = (Build) o;

        if (isSnapshot != build.isSnapshot) {
            return false;
        }
        if (!shortHash.equals(build.shortHash)) {
            return false;
        }
        return date.equals(build.date);

    }

    @Override
    public int hashCode() {
        int result = (isSnapshot ? 1 : 0);
        result = 31 * result + shortHash.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }
}

