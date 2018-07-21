package com.soyoung.battle.field.monitor.jvm;

import com.soyoung.battle.field.common.unit.ByteSizeValue;

import java.lang.management.ManagementPermission;
import java.util.Map;

public class JvmInfo {

    private static JvmInfo INSTANCE;

    public static JvmInfo jvmInfo() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new ManagementPermission("monitor"));
            sm.checkPropertyAccess("*");
        }
        return INSTANCE;
    }

    private final long pid;
    private final String version;
    private final String vmName;
    private final String vmVersion;
    private final String vmVendor;
    private final long startTime;
    private final long configuredInitialHeapSize;
    private final long configuredMaxHeapSize;
    private final Mem mem;
    private final String[] inputArguments;
    private final String bootClassPath;
    private final String classPath;
    private final Map<String, String> systemProperties;
    private final String[] gcCollectors;
    private final String[] memoryPools;
    private final String onError;
    private final String onOutOfMemoryError;
    private final String useCompressedOops;
    private final String useG1GC;
    private final String useSerialGC;

    private JvmInfo(long pid, String version, String vmName, String vmVersion, String vmVendor, long startTime,
                    long configuredInitialHeapSize, long configuredMaxHeapSize,Mem mem, String[] inputArguments, String bootClassPath,
                    String classPath, Map<String, String> systemProperties, String[] gcCollectors, String[] memoryPools, String onError,
                    String onOutOfMemoryError, String useCompressedOops, String useG1GC, String useSerialGC) {
        this.pid = pid;
        this.version = version;
        this.vmName = vmName;
        this.vmVersion = vmVersion;
        this.vmVendor = vmVendor;
        this.startTime = startTime;
        this.configuredInitialHeapSize = configuredInitialHeapSize;
        this.configuredMaxHeapSize = configuredMaxHeapSize;
        this.mem = mem;
        this.inputArguments = inputArguments;
        this.bootClassPath = bootClassPath;
        this.classPath = classPath;
        this.systemProperties = systemProperties;
        this.gcCollectors = gcCollectors;
        this.memoryPools = memoryPools;
        this.onError = onError;
        this.onOutOfMemoryError = onOutOfMemoryError;
        this.useCompressedOops = useCompressedOops;
        this.useG1GC = useG1GC;
        this.useSerialGC = useSerialGC;
    }


    public String version() {
        return this.version;
    }

    public Mem getMem() {
        return this.mem;
    }

    /**
     * The process id.
     */
    public long pid() {
        return this.pid;
    }

    public static class Mem  {

        private final long heapInit;
        private final long heapMax;
        private final long nonHeapInit;
        private final long nonHeapMax;
        private final long directMemoryMax;

        public Mem(long heapInit, long heapMax, long nonHeapInit, long nonHeapMax, long directMemoryMax) {
            this.heapInit = heapInit;
            this.heapMax = heapMax;
            this.nonHeapInit = nonHeapInit;
            this.nonHeapMax = nonHeapMax;
            this.directMemoryMax = directMemoryMax;
        }

//        public Mem(StreamInput in) throws IOException {
//            this.heapInit = in.readVLong();
//            this.heapMax = in.readVLong();
//            this.nonHeapInit = in.readVLong();
//            this.nonHeapMax = in.readVLong();
//            this.directMemoryMax = in.readVLong();
//        }

//        @Override
//        public void writeTo(StreamOutput out) throws IOException {
//            out.writeVLong(heapInit);
//            out.writeVLong(heapMax);
//            out.writeVLong(nonHeapInit);
//            out.writeVLong(nonHeapMax);
//            out.writeVLong(directMemoryMax);
//        }

        public ByteSizeValue getHeapInit() {
            return new ByteSizeValue(heapInit);
        }

        public ByteSizeValue getHeapMax() {
            return new ByteSizeValue(heapMax);
        }

        public ByteSizeValue getNonHeapInit() {
            return new ByteSizeValue(nonHeapInit);
        }

        public ByteSizeValue getNonHeapMax() {
            return new ByteSizeValue(nonHeapMax);
        }

        public ByteSizeValue getDirectMemoryMax() {
            return new ByteSizeValue(directMemoryMax);
        }
    }
}
