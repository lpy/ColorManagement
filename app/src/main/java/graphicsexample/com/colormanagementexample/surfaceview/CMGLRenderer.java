package graphicsexample.com.colormanagementexample.surfaceview;

import android.content.Context;
import android.graphics.ColorSpace;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import graphicsexample.com.colormanagementexample.R;
import graphicsexample.com.colormanagementexample.utils.ShaderHelper;
import graphicsexample.com.colormanagementexample.utils.ShaderSourceReader;
import graphicsexample.com.colormanagementexample.utils.TextureHelper;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class CMGLRenderer implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private static final float[] VERTEX_DATA = {
        // (x, y, s, t)
        0f, 0f, 0.5f, 0.5f,
        -0.8f, -0.6f, 0f, 1.0f,
        0.8f, -0.6f, 1f, 1.0f,
        0.8f, 0.6f, 1f, 0.0f,
        -0.8f, 0.6f, 0f, 0.0f,
        -0.8f, -0.6f, 0f, 1.0f,
    };

    private static final String U_MATRIX = "u_Matrix";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";

    private static final String A_POSITION = "a_Position";
    private static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    private final Context context;

    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    private final FloatBuffer floatBuffer;
    private int uMatrixLocation;
    private int uTextureUnitLocation;
    private int aPositionLocation;
    private int aTextureCoordinatesLocation;
    private int texture;
    private int program;

    public CMGLRenderer(Context context) {
        this.context = context;

        floatBuffer = ByteBuffer
                .allocateDirect(VERTEX_DATA.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX_DATA);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        program = ShaderHelper.buildProgram(ShaderSourceReader.readShaderFromSource(context, R.raw.vertex_shader),
                ShaderSourceReader.readShaderFromSource(context, R.raw.fragment_shader));

        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = GLES20.glGetUniformLocation(program, U_TEXTURE_UNIT);

        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation = GLES20.glGetAttribLocation(program, A_TEXTURE_COORDINATES);

        texture = TextureHelper.loadTexture(context, R.drawable.android_p3);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        perspectiveM(projectionMatrix, 0, 45, (float)width / (float)height, 10f, 0f);
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0f, 0f, -3.5f);
        // rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);
        final float[] tmp = new float[16];
        multiplyMM(tmp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(tmp, 0, projectionMatrix, 0, tmp.length);
    }

    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(program);

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(uTextureUnitLocation, 0);

        // Bind position data.
        floatBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT, false,
                STRIDE, floatBuffer);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        floatBuffer.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aTextureCoordinatesLocation, TEXTURE_COORDINATES_COMPONENT_COUNT, GLES20.GL_FLOAT, false,
                STRIDE, floatBuffer);
        GLES20.glEnableVertexAttribArray(aTextureCoordinatesLocation);

        floatBuffer.position(0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);
    }
}
