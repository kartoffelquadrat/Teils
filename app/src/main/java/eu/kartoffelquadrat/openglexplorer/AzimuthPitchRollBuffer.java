package eu.kartoffelquadrat.openglexplorer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Buffer to invert to notification chain and poll orientation values when needed instead of receiving notifications on updates.
 * Created by Maximilian Schiedermeier on 20/05/17.
 */
public class AzimuthPitchRollBuffer implements SensorEventListener {

    // depth of history (amount of previous values to be stored per sensor axis)
    // Increasing size leads to a lazier but more stable perspective adaption of the rendered scene
    private final int HISTORY_DEPTH=64;

    // We use a linear summation of biased weights where the bias per factor decreases proportional to its decay.
    // To reach a summation of the linear combination factors to exactly 1 we divide them by their total sum.
    private final float linearCompensator = (float) (2.0/ (HISTORY_DEPTH * (HISTORY_DEPTH+1)));

    // stores bufferedValues triplets in a queue. triple dimensions are for XYZ, queues itself represents the history. (queues is initialized with entirely flat-held-device sensor values.)
    // polling from this buffer class returns a wighted historic value. -> should not be a 2d array, but rather some sort of queue. -> done using pointer on current index and rotating replacement.
    private Queue<Float[]> bufferedAngles;
    private final SensorManager mySensorManager;
    private final Sensor myOrientationSensor;

    // queue gets smooths directly on each modification. To increase poll performance we buffer the smoothed results in an extra triple
    private float[] smoothedCurrentValues =new float[]{0f,0f,0f};

    public AzimuthPitchRollBuffer(Context context)
    {
        // Note : Queue must be thread save, because we asynchronously write to and read from it.
        bufferedAngles = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < HISTORY_DEPTH; i++) {
            bufferedAngles.add(new Float[]{0f,0f,0f});
        }
        mySensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        myOrientationSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    // TODO: this is dumb: better smooth only once and save intermediate result on arrival , then poll most recent smoothed result.
    public float getAzimuth() { return smoothedCurrentValues[0]; }

    public float getPitch() { return smoothedCurrentValues[1]; }

    public float getRoll() { return smoothedCurrentValues[2]; }

    /**
     * To be called from onPause method of encompassing activity
     */
    public void deactivate()
    {
        mySensorManager.unregisterListener(this);
    }

    /**
     * To be called from onResume method of encompassing activity
     */
    public void reactivate()
    {
        mySensorManager.registerListener(this, myOrientationSensor, SensorManager.SENSOR_ORIENTATION);
    }

    /**
     * Buffers new values sensor values at queue tail and removes queue head.
     * Must READ documentation: https://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix%28float[],%20float[],%20float[],%20float[]%29
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        bufferedAngles.remove();
        bufferedAngles.add(ArrayUtils.toObject(sensorEvent.values));
        smoothedCurrentValues = smoothHistory(bufferedAngles);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    /**
     * Applies a weighted smoothing on a series of stored float values.
     * The current solution uses a linear wighting, proportional to each positions actuality.
     * @param history for all axes, the index of the axis to be used to generate a smoothed value
     * @return the smoothed value
     */
    private float[] smoothHistory(Queue<Float[]> history)
    {
        // Must work on copy of queue, because it might be modified while I am iterating on it.
        ArrayList<Float[]> historyAsArrayList = new ArrayList<>(history);

        // this is a stub
        //return historyAsArrayList.get(historyAsArrayList.size()-1)[axis].floatValue();

        // All values in queue get weighted proportionally to their position)
        // Old values are located at queue beginning so the bias is ascending.
        float weightedResult[] = new float[3]; // we have 3 sensor axes
        int bias = 0;

        // Then we simply iterate over the queue and sum up biased queue values.
        Iterator<Float[]> historyIterator = historyAsArrayList.iterator();
        while(historyIterator.hasNext()) {
            bias++;
            float[] currentTriple = ArrayUtils.toPrimitive(historyIterator.next());

            // do smoothing on all sensor dimensions simultaneously
            for (int i = 0; i < weightedResult.length; i++) {
                weightedResult[i] += linearCompensator * bias * currentTriple[i];
            }
        }
        return weightedResult;
    }
}
