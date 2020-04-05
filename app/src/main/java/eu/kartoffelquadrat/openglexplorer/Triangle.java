package eu.kartoffelquadrat.openglexplorer;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Max on 11/29/16.
 * <p>
 * Defines shape's corner points, stores them in a buffer (custom openGL thingy)
 */
public class Triangle {

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
    float triangleCoords[] = new float[9];

    // Set color with red, green, blue and alpha (opacity) values
    private float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    private float centerX;

    private float centerY;
    // Variables required for the draw method
    private int mPositionHandle;

    private int mColorHandle;
    private int mMVPMatrixHandle;
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    public Triangle(float c0X, float c0Y, float c1X, float c1Y, float c2X, float c2Y, float r, float g, float b) {
        triangleCoords[0] = c0X;
        triangleCoords[1] = c0Y;
        triangleCoords[2] = 0;
        triangleCoords[3] = c1X;
        triangleCoords[4] = c1Y;
        triangleCoords[5] = 0;
        triangleCoords[6] = c2X;
        triangleCoords[7] = c2Y;
        triangleCoords[8] = 0;
        color[0] = r;
        color[1] = g;
        color[2] = b;
        centerX = (c0X+c1X) / 2.0f;
        centerY = (float)(c0Y - ((c1X-c0X)/(Math.sqrt(3)*2.0f)));

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);


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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public float[] getColor() {
        return color;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public void setColor(float r, float g, float b) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
    }
}