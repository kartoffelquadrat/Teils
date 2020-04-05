package eu.kartoffelquadrat.openglexplorer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import eu.kartoffelquadrat.openglexplorer.model.Cell;
import eu.kartoffelquadrat.openglexplorer.model.Model;

/**
 * This class draws all the openGL-driven objects: The board and the influencers. It also takes care
 * of the animated movements.
 * Created by Max on 11/29/16.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private final int BASEANGLE = 60;
    private boolean animationActive = false;
    private final double cellGapX = 0.02;
    private final double cellGapY = 0.02;
    private final double cellSizeX = 0.6;
    private final double cellSizeY = 0.6;
    private final int boardDimensions = 4;
    private final float maxCenterX = (float) ((cellSizeX + cellGapX) * ((boardDimensions / 2.0) - 0.5));
    private final float maxCenterY = (float) ((cellSizeY + cellGapY) * ((boardDimensions / 2.0) - 0.5));
    private final float INFLUENCER_SIZE_X = 0.3f;
    private final int INFLUENCER_DIMENSIONS = 6;
    private final float INLUENCER_GAP_X = 0.05f;
    private final float INFLUENCER_OFFSET_Y = -1.5f;

    // Board is the 2D array of GRAPHICAL cell elements (not the model).
    private Square[][] board = new Square[boardDimensions][];
    {
        for (int y = 0; y < boardDimensions; y++) {
            board[y] = new Square[boardDimensions];
        }
    }

    // Influencers are an array of GRAPHICAL triangle elements
    private Triangle[] influencers = new Triangle[INFLUENCER_DIMENSIONS];

    // Reference to the DATA object to be represented graphically
    private final Model model;

    // The second model is to remember previous states during a running animation.
    private Model oldModel;

    // board matrices
    private final float[] boardMVPMatrix = new float[16];
    private final float[] boardProjectionViewMatrix = new float[16];
    private final float[] boardViewMatrix = new float[16];
    private float[] shiftedCellRotationMatrix = new float[16];

    // influencer matrices
    private final float[] influencerMVPMatrix = new float[16];
    private final float[] influencerProjectionViewMatrix = new float[16];
    private final float[] influencerViewMatrix = new float[16];

    // Primary greyscale colors for elements outside of actual board (so far only influencers)
    // The 4th value is the invisible value only used at game start (blank model is initialized that way)
    private float[] keyColors = new float[]{0.15f, 0.5f, 0.8f, 0.0f};

    // Saves the moment an animation was triggered. Required to synchronize animations and compute animation progress for ongoing frames.
    long animationSyncTime;

    // Buffer for most recent gyro sensor values (needed for dynamic perspective adaptions)
    private final AzimuthPitchRollBuffer gyroBuffer;

    public MyGLRenderer(Model model, AzimuthPitchRollBuffer gyroBuffer) {
        this.model = model;
        this.gyroBuffer = gyroBuffer;

        // We initialize the old model as a completely blank model to get some animation at app launch (squares sliding into place)
        this.oldModel = new Model();
    }

    public void setAnimationActive(boolean animationActive) {
        // Use activation of animation flag to reset clock (synchronizes animation beginning)
        if (animationActive)
            animationSyncTime = SystemClock.uptimeMillis();
        else
            oldModel = model.deepCopy();

        // must be called after backing up the model, for the onSurface created method will be implicitly called statically (is to say woth animations disabled) AFTER the last animated frame.
        this.animationActive = animationActive;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Initialize board, as pattern of flat squares.
        double cellsOffSetX = -((cellSizeX + cellGapX) * boardDimensions - cellGapX) / 2.0;
        double cellsOffSetY = -((cellSizeY + cellGapY) * boardDimensions - cellGapY) / 2.0;
        for (int y = 0; y < boardDimensions; y++) {
            for (int x = 0; x < board[y].length; x++) {
                float minX = (float) (cellsOffSetX + x * (cellSizeX + cellGapX));
                float maxX = (float) (minX + cellSizeX);
                float minY = (float) (cellsOffSetY + y * (cellSizeY + cellGapY));
                float maxY = (float) (minY + cellSizeY);
                board[y][x] = new Square(minX, maxY, maxX, maxY, maxX, minY, minX, minY, maxCenterX, maxCenterY);
            }
        }

        // Initialize Influencers as row of flat triangles
        float influencerFloorY = (float) (INFLUENCER_SIZE_X / (Math.sqrt(3) * 2));
        float influencerCeilY = (float) (INFLUENCER_SIZE_X * Math.sqrt(3) / 2.0 - influencerFloorY);
        double influencersOffsetX = ((INFLUENCER_SIZE_X + INLUENCER_GAP_X) * INFLUENCER_DIMENSIONS - INLUENCER_GAP_X) / 2.0; //should already be leftest rim?
        for (int x = 0; x < INFLUENCER_DIMENSIONS; x++) {
            float influencerLeftRim = (float) (influencersOffsetX - (x * (INLUENCER_GAP_X + INFLUENCER_SIZE_X)));

            // Use triangle geometry to calculate static corner positions
            influencers[x] = new Triangle(influencerLeftRim, -influencerFloorY + INFLUENCER_OFFSET_Y, influencerLeftRim - INFLUENCER_SIZE_X, -influencerFloorY + INFLUENCER_OFFSET_Y, influencerLeftRim - (INFLUENCER_SIZE_X / 2.0f), influencerCeilY + INFLUENCER_OFFSET_Y, 0f, 0f, 0f);
        }
    }

    public void onDrawFrame(GL10 unused) {
        // Clear surface and redraw board and influencers
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawBoard();
        drawInfluencers();
    }

    private void drawInfluencers() {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(influencerViewMatrix, 0, 0, 0, -6, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(influencerMVPMatrix, 0, influencerProjectionViewMatrix, 0, influencerViewMatrix, 0);

        //Set colors and draw all influencers
        for (int i = 0; i < INFLUENCER_DIMENSIONS; i++) {

            // Look up previous and current state
            int state = model.getInfluencers().getStates()[i];
            int oldState = oldModel.getInfluencers().getStates()[i];

            // not all influencers need to turn, compare to previous value to find out
            int targetAngle = BASEANGLE * 2 * (oldState == state ? 0 : 1);

            // Combine the rotation matrix with the projection and camera view
            float[] shiftedInfluencerRotatioMatrix = getShiftedRotationMatrix(influencers[i].getCenterX(), influencers[i].getCenterY(), 0, targetAngle, animationActive, true);
            Matrix.multiplyMM(shiftedInfluencerRotatioMatrix, 0, influencerMVPMatrix, 0, shiftedInfluencerRotatioMatrix, 0);

            // Set colors and draw influencers
            // compute smooth color transition if required
            float color = keyColors[oldState];
            if (state != oldModel.getInfluencers().getStates()[i] && animationActive) {
                long time = (SystemClock.uptimeMillis() - animationSyncTime);
                color = keyColors[oldState] + (Math.min(1.0f, (time / (float) OpenGlActivity.ANIMATION_DURATION)) * (keyColors[state] - keyColors[oldState]));
            }

            influencers[i].setColor(color, color, color);
            influencers[i].draw(shiftedInfluencerRotatioMatrix);
        }
    }

    private void drawBoard() {

        // Prepare gyro sensor data
        float roll = gyroBuffer.getRoll();
        roll = Math.max(roll, -90);
        roll = Math.min(roll, 90);
        // experimental, version that does not translate pitch into virtual representation, but involves buffering.
        float virtualPitch = (float) (-gyroBuffer.getPitch()*Math.PI/180.0);
        //float virtualPitch = getVirtualPitch(gyroBuffer.getPitch());

        // Set the camera position, based on gyro data (View matrix)
        // Last 9 params are:
        //(xyz) camera-position
        //(xyz) target coordinates for camera orientation -> (0,0,0)
        //(xyz) turn/orientation of camera -> (0,1,0) means "up" is in direction of the y axis.
        Matrix.setLookAtM(boardViewMatrix, 0, roll/60, (float)(-7*Math.sin(virtualPitch)), (float)(-7*Math.cos(virtualPitch)), 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(boardMVPMatrix, 0, boardProjectionViewMatrix, 0, boardViewMatrix, 0);

        // Matrices themselves are float[]. But we need a 2D grid of them, because each cell needs its own matrix
        float[][][] projectionMatrix;
        projectionMatrix = new float[boardDimensions][boardDimensions][];

        // Create matrix for each cell (dynamically activates animations of required)
        for (int y = 0; y < boardDimensions; y++) {
            for (int x = 0; x < boardDimensions; x++) {

                // Create an individual rotation transformation for clockwise rotation in Y for each cell
                // Argument for the shifter rotation is the cells xy-center position
                shiftedCellRotationMatrix = getShiftedRotationMatrix(board[y][x].centerX, 0, oldModel.getBoard().getCellValue(x, y) * 60, computeAnimationAngle(x, y), animationActive, false);

                // Combine the rotation matrix with the projection and camera view
                // Note that the mMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                float[] cellCustomMatrix = new float[16];
                Matrix.multiplyMM(cellCustomMatrix, 0, boardMVPMatrix, 0, shiftedCellRotationMatrix, 0);
                projectionMatrix[y][x] = cellCustomMatrix;
            }
        }

        // Objects must be drawn in order according to decreasing distance.
        for (int y = boardDimensions - 1; y >= 0; y--) {
            for (int x = 0; x < boardDimensions; x++) {
                board[y][x].draw(projectionMatrix[y][x]);
            }
        }
    }

    // Utility class that compiles the OpenGl shading language code so we can user it in the OpenGl Es environment.
    // This method will be used by the Triangle class so it can generate the ids (?) for vertex and fragment shader
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Creates a rotation matrix for spins around axises other than departing from the origin
     *
     * @param x           as the horizontal offset for the rotation center
     * @param offsetAngle as the base angle the cell has (according to the old model).
     * @param amount      as the angle by which the cell shall be advanced during the animation (set to 0 if no animation needed).
     * @param animated    as a flag to toggle an animated rotation.
     * @return
     */
    private float[] getShiftedRotationMatrix(float x, float y, float offsetAngle, float amount, boolean animated, boolean zAxis) {
        // Create matrix for shift by x and y (places object centered around origin)
        float[] shiftMatrix = new float[16];
        Matrix.setIdentityM(shiftMatrix, 0);
        Matrix.translateM(shiftMatrix, 0, -x, -y, 0);

        float angle = offsetAngle;
        if (animated) {
            // Create a rotation transformation for animated clockwise rotation in Y
            long time = (SystemClock.uptimeMillis() - animationSyncTime);
            if (amount > 0)
                angle = offsetAngle + Math.min(amount / 1000 * 2 * smoothAnimationAdvancement((int) time, OpenGlActivity.ANIMATION_DURATION, 1), amount); // explanation: we want to reach an angle of 60 degrees within the given sync time. 500 * 2 * 0.060 = 60 degrees (of 360)
            else
                angle = offsetAngle + Math.max(amount / 1000 * 2 * smoothAnimationAdvancement((int) time, OpenGlActivity.ANIMATION_DURATION, 1), amount); // explanation: we want to reach an angle of 60 degrees within the given sync time. 500 * 2 * 0.060 = 60 degrees (of 360)
        }
        // last three values are xyz for rotation axis as vector originating 0,0,0(!)
        // to get rotations around other than origin, use extra method at class end.
        if (zAxis)
            Matrix.setRotateM(shiftedCellRotationMatrix, 0, angle, 0, 0, 1);
        else
            Matrix.setRotateM(shiftedCellRotationMatrix, 0, angle, 0, -1, 0);

        // Combine matrices
        float[] shiftRotateMatrix = new float[16];
        Matrix.multiplyMM(shiftRotateMatrix, 0, shiftedCellRotationMatrix, 0, shiftMatrix, 0);

        // Create matrix to shift back by x and y (moves object back from origin to its initial position)
        float[] shiftBackMatrix = new float[16];
        Matrix.setIdentityM(shiftBackMatrix, 0);
        Matrix.translateM(shiftBackMatrix, 0, x, y, 0);

        // Combine matrices
        float[] shiftRotateShiftMatrix = new float[16];
        Matrix.multiplyMM(shiftRotateShiftMatrix, 0, shiftBackMatrix, 0, shiftRotateMatrix, 0);
        return shiftRotateShiftMatrix;
    }

    /**
     * Smoothes out animation advancement by not using a linear, but sinosiodly delayed/speed-up timing
     *
     * @param progress    as the current animation progress (usually the time in ms)
     * @param maxDuration how long the animation last
     * @param level       how hard the inital / end delay is (higher value, harder delays)
     * @return
     */
    private int smoothAnimationAdvancement(int progress, int maxDuration, int level) {
        if (level == 0)
            return progress;
        return (int) ((-Math.cos((Math.PI * smoothAnimationAdvancement(progress, maxDuration, level - 1) / maxDuration)) + 1) * maxDuration * 0.5f);
    }

    private float computeAnimationAngle(int x, int y) {
        int oldValue = oldModel.getBoard().getCellValue(x, y);
        int newValue = model.getBoard().getCellValue(x, y);

        if (newValue == oldValue)
            return 0f;
        return -(((newValue - oldValue + 3) % 3) - 1.5f) * 2.0f * BASEANGLE;
    }

    /**
     * Creates the frustumM Matrices
     *
     * @param unused
     * @param width
     * @param height
     */
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        Matrix.frustumM(boardProjectionViewMatrix, 0, -ratio, ratio, -1, 1, 3, 11);

        // this projection matrix is applied to object coordinates
        Matrix.frustumM(influencerProjectionViewMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    /**
     * Converts absolute pitch value as determined by handhelds sensors to a virtual value, that describes the camera's pitch relative from the scene orbit at distance 7.
     * absolute pitch has value -90 when handheld is vertical and 0 when handheld is flat
     */
    private float getVirtualPitch(float absolutePitch)
    {
        // First of all we limit the value range to a reasonable range (nobody watches his phone while upside down??? -> what if in Bed) -> // ToDo: check what can be done about that...
        float virtualPitch = Math.max(absolutePitch, -90);
        virtualPitch = Math.min(virtualPitch, 0);

        // Base pitch ange in degrees. If held at 45 degrees this is the virtual cameras pitch.
        // 0 would be flat from above, 90 would be from the side
        int basePitchAngle = 52;

        // Next we want to shrink the total range, so natural trembling of hands does not shake things up so much
        virtualPitch += basePitchAngle; // center around 0
        virtualPitch = virtualPitch / 3; //shrink
        virtualPitch -= basePitchAngle; //shift back

        //return SHRINKED, POSITIVE range in RADIONS
        return (float)(-virtualPitch*Math.PI/180.0);
    }
}