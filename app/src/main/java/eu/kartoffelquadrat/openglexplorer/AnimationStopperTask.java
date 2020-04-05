package eu.kartoffelquadrat.openglexplorer;

import android.os.AsyncTask;

/**
 * Created by Max on 12/8/16.
 */
public class AnimationStopperTask extends AsyncTask {

    /**
     * @param objects: The GlSurfaceView object at 0
     * @return null.
     */
    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            Thread.sleep(OpenGlActivity.ANIMATION_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // toggle render mode back to manual.
        RasterSurfaceView surfaceView = ((RasterSurfaceView) objects[0]);
        surfaceView.setRenderModeDirty(true);

        // manually trigger view update, displaying everything in a static way.
        surfaceView.getRenderer().setAnimationActive(false);

        /* Explicitly requesting a rendered frame would not be absolutely not necessary, for the last animated frame is in theory identical to a later manually added static frame
         * But the smoothing-algorithm used for the animated movements leads lo slight rounding errors, so drawing an additional frame without the precision loss caused by the animation will hide that effect (it is visible, but not very much -> rare slightly mistilted squares after animations)
         */
        surfaceView.requestRender();

        // Re-Enable user interactions (were blocked during the animated transition)
        surfaceView.unBlock();

        return null;
    }
}
