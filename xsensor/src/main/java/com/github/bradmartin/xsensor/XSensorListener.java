package com.github.bradmartin.xsensor;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;

public interface XSensorListener extends SensorEventListener {
    void onSensorChanged(String jsonData);

    void onAccuracyChanged(Sensor sensor, int accuracy);
}