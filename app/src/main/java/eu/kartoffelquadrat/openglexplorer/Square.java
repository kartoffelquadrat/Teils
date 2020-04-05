package eu.kartoffelquadrat.openglexplorer;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Max on 11/29/16.
 * <p>
 * Defines shape's corner points, stores them in a buffer (custom openGL thingy)
 */
public class Square {

    private final int mProgram;

    // OpenGl specific String code for vertex shader...
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    // ... and fragment shader
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    // 0,0,0 (XYZ) is screen center
    // 1,1,0 TR corner
    // -1,-1,0 BL corner
    // Order of points is important (clockwise or counterclockwise), because it defines which is front and which is back side.
    float squareCoords[] = new float[12];
    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    private ShortBuffer drawListBuffer;


    // Set color with red, green, blue and alpha (opacity) values
    //float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};
    // Color is computed using relative drift to coordinate origins
    float color[] = {0,0,0,1.0f};
    float centerX;
    float centerY;

    // Variables required for the draw method
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public Square(float c1X, float c1Y, float c2X, float c2Y, float c3X, float c3Y, float c4X, float c4Y, float maxCenterX, float maxCenterY) {
        squareCoords[0] = c1X;
        squareCoords[1] = c1Y;
        squareCoords[2] = 0;
        squareCoords[3] = c2X;
        squareCoords[4] = c2Y;
        squareCoords[5] = 0;
        squareCoords[6] = c3X;
        squareCoords[7] = c3Y;
        squareCoords[8] = 0;
        squareCoords[9] = c4X;
        squareCoords[10] = c4Y;
        squareCoords[11] = 0;

        centerX = (c1X+c2X)/2.0f;
        centerY = (c1Y+c3Y)/2.0f;
        //Note (arithmetically maxCenter would need a multiplicator of 2.0 to lead to colors between 0f and 1.0f. However I find slighly less intense color more appealing, so i increased the factor slightly)
        // Same for the motivation of chosing 0.9f instead of 1.0f on the third color
        color[0] = (float) (0.5f + (centerX / (2.5f*maxCenterX)));
        color[2] = (float) (0.5f + (centerY / (2.5f*maxCenterY)));
        color[1] = 0.9f-((color[0]+color[2])/2.0f);

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(squareCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);


        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);



        // The drawn object (This triangle) needs to claim shaders for vertex and fragment, so it can be drawn
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program ( = one openGL procedure responsible for drawing this shape)
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    // Since each shape may have different drawing comportments, the shapes themselves define
    public void draw(float[] mvpMatrix) {

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}