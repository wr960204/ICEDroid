package com.example.app1;

public class fingerprintjni {

    static {
        System.loadLibrary("app1");
    }

    public native String fingerprint();

    public native String netfp();

    public native String check();

    public native String mapscheck();

    public native String getAllAppNamesJNI();

}
