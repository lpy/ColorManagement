package graphicsexample.com.colormanagementexample;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;

import java.util.concurrent.atomic.AtomicBoolean;

import graphicsexample.com.colormanagementexample.surfaceview.CMGLSurfaceView;
import graphicsexample.com.colormanagementexample.textureview.CMGLProducerThread;
import graphicsexample.com.colormanagementexample.textureview.CMTextureViewGLRenderer;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private GLSurfaceView glSurfaceView;
    private TextureView mTextureView;
    private CMTextureViewGLRenderer mRenderer;
    private Thread mProducerThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // glSurfaceView = new CMGLSurfaceView(this);
        // setContentView(glSurfaceView);

        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(this);
        setContentView(mTextureView);

        mRenderer = new CMTextureViewGLRenderer(this);
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
}
