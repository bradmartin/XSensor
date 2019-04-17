package com.github.bradmartin.xsensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

public class XSensors implements SensorEventListener {

    private static String TAG = "XSensor";

    private Handler mSensorHandler;
    private XSensorListener mListener;
    private SensorManager mSensorManager;

    private boolean useLiteDataBagForSensorData;

    /*
     * Constructor. Since the class does not extend Application or Activity, we need
     * to context of the running process/app.
     */
    public XSensors(Context ctx, boolean liteData) {
        // if liteData argument is provided then the sensor event data will use the LiteDataBag
        // if not then we use the HeavyDataBag to return in the sensor changed events
        useLiteDataBagForSensorData = liteData;

        // Get the SensorManager
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        Log.d(TAG, "SensorManager: " + this.mSensorManager);
        // Setup our background thread for sensors
        HandlerThread mSensorThread = new HandlerThread("XSensor Thread", Process.THREAD_PRIORITY_BACKGROUND);
        mSensorThread.start();
        mSensorHandler = new Handler(mSensorThread.getLooper()); // Blocks until looper is prepared
        Log.d(TAG, "XSensors HandlerThread: " + mSensorThread);
        Log.d(TAG, "XSensors Handler: " + mSensorHandler);
    }

    public void setListener(XSensorListener mListener) {
        this.mListener = mListener;
        Log.d(TAG, "Listener: " + this.mListener);
    }

    public static boolean supportsFIFO(Sensor sensor) {
        return sensor.getFifoMaxEventCount() != 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {
            LiteDataBag liteDataBag = null;
            HeavyDataBag heavyDataBag = null;
            HashMap<String, Float> sensorData = new HashMap<>();

            int sensorType = event.sensor.getType();
            // depending on the the sensor type set the result to return in the listener
            if (sensorType == Sensor.TYPE_ACCELEROMETER
                    || sensorType == Sensor.TYPE_LINEAR_ACCELERATION
                    || sensorType == Sensor.TYPE_GRAVITY
                    || sensorType == Sensor.TYPE_GYROSCOPE
                    || sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                sensorData.put("x", event.values[0]);
                sensorData.put("y", event.values[1]);
                sensorData.put("z", event.values[2]);
            } else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
                sensorData.put("x", event.values[0]);
                sensorData.put("y", event.values[1]);
                sensorData.put("z", event.values[2]);
                sensorData.put("cos", event.values[3]);
                sensorData.put("heading_accuracy", event.values[4]);
            } else if (sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                sensorData.put("x", event.values[0]);
                sensorData.put("y", event.values[1]);
                sensorData.put("z", event.values[2]);
                sensorData.put("cos", event.values[3]);
            } else if (sensorType == Sensor.TYPE_STATIONARY_DETECT) {
                sensorData.put("stationary", event.values[0]);
            } else if (sensorType == Sensor.TYPE_PROXIMITY) {
                sensorData.put("proximity", event.values[0]);
            } else if (sensorType == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) {
                sensorData.put("state", event.values[0]);
            } else if (sensorType == Sensor.TYPE_HEART_RATE) {
                sensorData.put("heart_rate", event.values[0]);
            } else if (sensorType == Sensor.TYPE_HEART_BEAT) {
                sensorData.put("confidence", event.values[0]);
            } else if (sensorType == Sensor.TYPE_LIGHT) {
                sensorData.put("light_level", event.values[0]);
            } else if (sensorType == Sensor.TYPE_PRESSURE) {
                sensorData.put("pressure", event.values[0]);
            } else if (sensorType == Sensor.TYPE_RELATIVE_HUMIDITY) {
                sensorData.put("humidity", event.values[0]); // Relative ambient air humidity in percent
            } else if (sensorType == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                sensorData.put("temp", event.values[0]); // ambient (room) temperature in degree Celsius
            } else if (sensorType == Sensor.TYPE_POSE_6DOF) {
                sensorData.put("x", event.values[0]);
                sensorData.put("y", event.values[1]);
                sensorData.put("z", event.values[2]);
                sensorData.put("cos", event.values[3]);
                sensorData.put("x_translation", event.values[4]);
                sensorData.put("y_translation", event.values[5]);
                sensorData.put("z_translation", event.values[6]);
                sensorData.put("x_rotation", event.values[7]);
                sensorData.put("y_rotation", event.values[8]);
                sensorData.put("z_rotation", event.values[9]);
                sensorData.put("cos_rotation", event.values[10]);
                sensorData.put("x_delta_translation", event.values[11]);
                sensorData.put("y_delta_translation", event.values[12]);
                sensorData.put("z_delta_translation", event.values[13]);
                sensorData.put("sequence_number", event.values[14]);
            }

            if (useLiteDataBagForSensorData) {
                liteDataBag = new LiteDataBag();
                liteDataBag.s = event.sensor.getType();
                liteDataBag.ts = event.timestamp;
                liteDataBag.d = sensorData;
            } else {
                heavyDataBag = new HeavyDataBag();
                heavyDataBag.sensor = event.sensor.getStringType();
                heavyDataBag.timestamp = event.timestamp;
                heavyDataBag.data = sensorData;
            }

            Gson gson = new Gson();
            String result;
            if (useLiteDataBagForSensorData) {
                result = gson.toJson(liteDataBag);
            } else {
                result = gson.toJson(heavyDataBag);
            }

            // send the data to the XSensorListener
            mListener.onSensorChanged(result);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // send the values to the XSensorListener
        mListener.onAccuracyChanged(sensor, accuracy);
    }

    /**
     * Starts the sensor with the provided sensor reporting delay.
     *
     * @param sensor      [int] - The sensor type.
     * @param sensorDelay [int] - The sensory reporting delay.
     * @return [boolean] - Return true if sensor is registered with the listener.
     */
    public Sensor startSensor(int sensor, int sensorDelay) {
        if (mSensorManager == null) { // BOO NO SENSOR MANAGER
            Log.d(TAG, "XSensors does not have the SensorManager. Will return null since no sensor can be registered.");
            return null;
        }

        // here we have the SensorManager so try to get the sensor requested
        Sensor defaultSensor = mSensorManager.getDefaultSensor(sensor);
        // if we have the sensor then register the listener
        if (defaultSensor != null) {
            mSensorManager.registerListener(this, defaultSensor, sensorDelay, mSensorHandler);
            return defaultSensor; // YAY SENSOR REGISTERED
        } else {
            Log.d(TAG, "SensorManager unable to get the default sensor for type: " + sensor);
            return null; // BOO SENSOR NOT REGISTERED
        }
    }

    public Sensor startSensorWithReportLatency(int sensor, int sensorDelay, int maxReportLatencyUs) {
        if (mSensorManager == null) { // BOO NO SENSOR MANAGER
            Log.d(TAG, "XSensors does not have the SensorManager. Will return null since no sensor can be registered.");
            return null;
        }

        // here we have the SensorManager so try to get the sensor requested
        Sensor defaultSensor = mSensorManager.getDefaultSensor(sensor);
        // if we have the sensor then register the listener
        if (defaultSensor != null) {
            // make sure the sensor uses a FIFO if not then register it on the background thread
            if (defaultSensor.getFifoMaxEventCount() == 0) {
                Log.d(TAG, "Registering " + defaultSensor.getStringType() + " with the background thread since it does not support FIFO.");
                mSensorManager.registerListener(this, defaultSensor, sensorDelay, mSensorHandler);
            } else {
                mSensorManager.registerListener(this, defaultSensor, sensorDelay, maxReportLatencyUs);
            }
            return defaultSensor; // YAY SENSOR REGISTERED
        } else {
            Log.d(TAG, "SensorManager unable to get the default sensor for type: " + sensor);
            return null; // BOO SENSOR NOT REGISTERED
        }
    }

    /**
     * Stops the sensor by unregistering.
     *
     * @param sensor [Sensor] - The sensor to unregister.
     */
    public void stopSensor(Sensor sensor) {
        if (sensor != null) {
            mSensorManager.unregisterListener(this, sensor);
        }
    }

    public List<Sensor> getDeviceSensors() {
        if (mSensorManager != null) {
            return mSensorManager.getSensorList(Sensor.TYPE_ALL);
        } else {
            return null;
        }
    }

    /**
     * Flushes the FIFO of all the sensors registered for this listener.
     *
     * @return boolean
     */
    public boolean flush() {
        if (mListener != null) {
            Log.d(TAG, "Flushing the event data for listener: " + mListener);
            return this.mSensorManager.flush(this.mListener);
        } else {
            Log.w(TAG, "No sensor listener set, so unable to flush the data.");
            return false;
        }
    }

}