package com.github.bradmartin.xsensor;

import android.hardware.Sensor;

public interface XSensorListener {
    void onSensorChanged(String jsonData);

    void onAccuracyChanged(Sensor sensor, int accuracy);
}