package graphicsexample.com.colormanagementexample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import android.view.View;
import java.util.concurrent.atomic.AtomicBoolean;

import graphicsexample.com.colormanagementexample.surfaceview.CMGLSurfaceView;
import graphicsexample.com.colormanagementexample.textureview.CMGLProducerThread;
import graphicsexample.com.colormanagementexample.textureview.CMTextureViewGLRenderer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class MainActivity extends Activity  implements TextureView.SurfaceTextureListener {
    private GLSurfaceView glSurfaceView;
    private TextureView mTextureView;
    private CMTextureViewGLRenderer mRenderer;
    private Thread mProducerThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setColorMode(ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT);
        glSurfaceView = new CMGLSurfaceView(this);
        setContentView(glSurfaceView);

        // mTextureView = new TextureView(this);
        // mTextureView.setSurfaceTextureListener(this);
        // setContentView(mTextureView);

        // mRenderer = new CMTextureViewGLRenderer(this);

        // getWindow().setColorMode(ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT);
        // setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceView != null) {
            glSurfaceView.onResume();
        }
    }

    @Override
    protected  void onPause() {
        super.onPause();
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mRenderer != null) {
            mRenderer.setViewport(width, height);
            mProducerThread = new CMGLProducerThread(surface, mRenderer, new AtomicBoolean(true));
            mProducerThread.start();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mRenderer != null) {
            mRenderer.resize(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mProducerThread = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void changeColorMode(View view) {
        int colorMode = getWindow().getColorMode();
        Log.e("HAHA", "Current color mode: " + colorMode);
        if (colorMode == ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT) {
            getWindow().setColorMode(ActivityInfo.COLOR_MODE_DEFAULT);
        } else if (colorMode == ActivityInfo.COLOR_MODE_DEFAULT) {
            getWindow().setColorMode(ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT);
        }
        Log.e("HAHA", "Toggle to color mode: " + colorMode);
    }
}
