package eu.kartoffelquadrat.openglexplorer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.widget.Toast;


import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import eu.kartoffelquadrat.openglexplorer.model.Model;

/**
 * Registers user interaction, triggers model and UI changes
 * Created by Max on 11/29/16.
 */
public class RasterSurfaceView extends GLSurfaceView {

    // remembers if the UI is currently responsive or not
    private boolean blocked = true;

    // The renderer does the actual work.
    private final MyGLRenderer mRenderer;
    private boolean renderModeDirty = true; //if dirty, then updates only appear when manually triggered -> no animations
    private final Model model;

    // Rumbler to be used for haptic feedback on user interaction
    Vibrator rumbler;

    // Reference to the countdowner that can be notified on user interaction.
    CountDowner countDowner;

    // CountDowner won't be triggered if the same influencer is repeatedly used (no impact on brute-force attacks and makes it easier to think.)
    private int previouslyUsedInfluencer = -1;

    private boolean initialTouchReceived = false;

    private final AzimuthPitchRollBuffer gyroBuffer;

    public RasterSurfaceView(Context context, Model model, Vibrator rumbler, AzimuthPitchRollBuffer gyroBuffer, CountDowner countDowner) {
        super(context);

        // Save model, so it can be modified on user interaction
        this.model = model;
        this.rumbler = rumbler;
        this.countDowner = countDowner;
        this.gyroBuffer = gyroBuffer;

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        // Enable Anti-Aliasing
        setEGLConfigChooser(new MyConfigChooser());

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(model, gyroBuffer);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        //  -> No rendering until requestRender() called manually.
        //(needs to be deactivated when animations are enabled)
        setRenderMode(renderModeDirty ? GLSurfaceView.RENDERMODE_WHEN_DIRTY : GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    // ToDo: Remove this method + calls from this method. Dynamic camera changes, based on gyro values require a continuous rendering anyways...
    public void setRenderModeDirty(boolean dirty) {
        //renderModeDirty = dirty;
        //setRenderMode(renderModeDirty ? GLSurfaceView.RENDERMODE_WHEN_DIRTY : GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public MyGLRenderer getRenderer() {
        return mRenderer;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if (!initialTouchReceived) {
            launchInitialAnimation();
            return true;
        }

        // Completely ignore interaction if game is already finished
        if (model.isSolved())
            return true;

        // Don't handle interactions that are not roughly in the area of the influencers
        if (e.getY() < (getHeight()) * 0.8)
            return true;

        // Don't handle interaction if we're currently still treating one.
        if (blocked)
            return true;


        // Don't react on anything that is not an ordinary tap
        if (e.getAction() != MotionEvent.ACTION_DOWN)
            return true;

        // Figure out which influencer has been touched
        int touchedInfluencer = (int) e.getX() / (getWidth() / 6);

        // Don't handle interactions if there are no more moves left
        if (countDowner.isOver() && (touchedInfluencer != previouslyUsedInfluencer)) {
            Toast.makeText(getContext(), "Game Over!",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        // Tick down only if influencer is other than the one that was used before
        if (touchedInfluencer != previouslyUsedInfluencer) {
            previouslyUsedInfluencer = touchedInfluencer;
            countDowner.tickDown();
        }

        // Provide haptic and optical user feedback
        // Get instance of Vibrator from current Context
        rumbler.vibrate(10);
        return incrementInfluencer(touchedInfluencer);
    }

    private boolean incrementInfluencer(int influencerId) {

        blocked = true;

        // Update the clicked influencers contribution value
        model.getInfluencers().advance(influencerId);
        // Update model according to changed influencers (no worries, the UI has a backup to create a smooth transition to the modified model)
        model.getInfluencers().influence(model.getBoard());

        // trigger a temporary animation
        setRenderModeDirty(false);

        // Tell renderer to render using animation-matrices, not static matrices
        getRenderer().setAnimationActive(true);
        requestRender();

        // Launch async task stopping the animation after a while.
        AsyncTask animationStopper = new AnimationStopperTask();
        animationStopper.execute(this);

        // Display congratulations message in case of win
        if (model.isSolved()) {
            Toast.makeText(getContext(), "You made it!",
                    Toast.LENGTH_SHORT).show();

            // Tell CountDowner to persist score if its a highscore
            countDowner.persist();
        }
        return true;
    }

    public void unBlock() {
        blocked = false;
    }

    public void launchInitialAnimation() {
        initialTouchReceived = true;

        // Initialize CountDowner
        countDowner.init();

        // Trigger initial animation
        model.getInfluencers().influence(model.getBoard());

        // trigger a temporary animation
        setRenderModeDirty(false);

        // Tell renderer to render using animation-matrices, not static matrices
        getRenderer().setAnimationActive(true);
        requestRender();

        // Launch async task stopping the animation after a while.
        AsyncTask animationStopper = new AnimationStopperTask();
        animationStopper.execute(this);
    }

    /**
     * Config for enabling Anti aliasing - see here:
     * http://stackoverflow.com/questions/27035893/antialiasing-in-opengl-es-2-0
     */
    class MyConfigChooser implements GLSurfaceView.EGLConfigChooser {
        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int attribs[] = {
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_SAMPLES, 4,  // This is for 4x MSAA.
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts);

            if (configCounts[0] == 0) {
                // Failed! Error handling.
                return null;
            } else {
                return configs[0];
            }
        }
    }
}
