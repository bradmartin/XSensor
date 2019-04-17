package com.github.bradmartin.xsensor;

import java.util.HashMap;

public class LiteDataBag {

    /**
     * Sensor type as int.
     */
    public int s;

    /**
     * Sensor event timestamp.
     */
    public long ts;

    /**
     * Returns the seconds since Epoch
     */
    public long t = System.currentTimeMillis() / 1000;

    /**
     * Hashmap for storing the sensor data.
     */
    public HashMap d;

    LiteDataBag() {
        // no-args constructor
    }

}
