package com.coeater.android.customCamera;

class Histogram {
    private final long handle;

    private Histogram(long handle) {
        this.handle = handle;
    }

    public static Histogram createCounts(String name, int min, int max, int bucketCount) {
        return new Histogram(nativeCreateCounts(name, min, max, bucketCount));
    }

    public static Histogram createEnumeration(String name, int max) {
        return new Histogram(nativeCreateEnumeration(name, max));
    }

    public void addSample(int sample) {
        nativeAddSample(this.handle, sample);
    }

    private static long nativeCreateCounts(String var0, int var1, int var2, int var3){ return 0; }

    private static long nativeCreateEnumeration(String var0, int var1){ return 0; }

    private static void nativeAddSample(long var0, int var2){ }
}