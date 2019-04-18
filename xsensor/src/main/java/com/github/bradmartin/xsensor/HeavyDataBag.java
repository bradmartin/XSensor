package com.github.bradmartin.xsensor;

import java.util.HashMap;

class HeavyDataBag {
    /**
     * Sensor type as string.
     */
    public String sensor;

    /**
     * Sensor event timestamp.
     */
    public long timestamp;

    /**
     * Returns the seconds since Epoch
     */
    public long time = System.currentTimeMillis() / 1000;

    public HashMap data;

    HeavyDataBag() {
        // no-args constructor
    }
}
