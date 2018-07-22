package com.soyoung.battle.field;

public class Version implements Comparable<Version> {

    public static final Version CURRENT = new Version(1);

    public final int id;
    public final byte major;
    public final byte minor;
    public final byte revision;
    public final byte build;


    Version(int id) {
        this.id = id;
        this.major = (byte) ((id / 1000000) % 100);
        this.minor = (byte) ((id / 10000) % 100);
        this.revision = (byte) ((id / 100) % 100);
        this.build = (byte) (id % 100);
    }

    @Override
    public int compareTo(Version other) {
        return Integer.compare(this.id, other.id);
    }

    public static String displayVersion(final Version version, final boolean isSnapshot) {
        return version + (isSnapshot ? "-SNAPSHOT" : "");
    }

    public static Version fromId(int id) {
        return new Version(1001001);
    }


    public boolean isRelease() {
        return build == 99;
    }
}
