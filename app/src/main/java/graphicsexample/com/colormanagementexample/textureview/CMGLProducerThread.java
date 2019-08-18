package graphicsexample.com.colormanagementexample.textureview;

import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import android.util.Log;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

public class CMGLProducerThread extends Thread {
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int EGL_OPENGL_ES2_BIT = 0x0004;
    private static final int EGL_OPENGL_ES3_BIT_KHR = 0x0040;
    private static final int EGL_GL_COLORSPACE_KHR = 0x309D;
    private static final int EGL_GL_COLORSPACE_SRGB_KHR = 0x3089;
    private static final int EGL_GL_COLORSPACE_LINEAR_KHR = 0x308A;
    private static final int EGL_GL_COLORSPACE_DISPLAY_P3_EXT = 0x3363;
    private static final int EGL_GL_COLORSPACE_DISPLAY_P3_LINEAR_EXT = 0x3362;
    private static final int EGL_GL_COLORSPACE_DISPLAY_P3_PASSTHROUGH_EXT = 0x3490;
    private static String TAG = "CMGLProducerThread";

    private AtomicBoolean mShouldRender;
    private SurfaceTexture mSurfaceTexture;
    private GLRenderer mRenderer;

    private EGL10 mEgl;
    private EGLDisplay mEglDisplay = EGL10.EGL_NO_DISPLAY;
    private EGLContext mEglContext = EGL10.EGL_NO_CONTEXT;
    private EGLSurface mEglSurface = EGL10.EGL_NO_SURFACE;
    private GL mGL;

    public interface GLRenderer {
        void drawFrame();
    }

    public CMGLProducerThread(SurfaceTexture surfaceTexture, GLRenderer renderer,
                              AtomicBoolean shouldRender) {
        mSurfaceTexture = surfaceTexture;
        mRenderer = renderer;
        mShouldRender = shouldRender;
    }

    private void initGL() {
        mEgl = (EGL10)EGLContext.getEGL();

        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed : " +
                    GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        int []version = new int[2];
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed : " +
                    GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        int []configAttribs = {
                EGL10.EGL_BUFFER_SIZE, 32,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT_KHR,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE
        };

        int []numConfigs = new int[1];
        EGLConfig []configs = new EGLConfig[1];
        if (!mEgl.eglChooseConfig(mEglDisplay, configAttribs, configs, 1, numConfigs)) {
            throw new RuntimeException("eglChooseConfig failed : " +
                    GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        int []contextAttribs = {
                EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL10.EGL_NONE
        };
        mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, contextAttribs);

        int surfaceAttribs[] = {
                EGL_GL_COLORSPACE_KHR, EGL_GL_COLORSPACE_DISPLAY_P3_PASSTHROUGH_EXT,
                EGL10.EGL_NONE
        };

        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], mSurfaceTexture, surfaceAttribs);
        if (mEglSurface == EGL10.EGL_NO_SURFACE || mEglContext == EGL10.EGL_NO_CONTEXT) {
            int error = mEgl.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                throw new RuntimeException("eglCreateWindowSurface returned  EGL_BAD_NATIVE_WINDOW. ");
            }
            throw new RuntimeException("eglCreateWindowSurface failed : " +
                    GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed : " +
                    GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        mGL = mEglContext.getGL();
    }

    private void destroyGL() {
        mEgl.eglDestroyContext(mEglDisplay, mEglContext);
        mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
        mEglContext = EGL10.EGL_NO_CONTEXT;
        mEglSurface = EGL10.EGL_NO_SURFACE;
    }

    public void run() {
        initGL();

        if (mRenderer != null) {
            ((CMTextureViewGLRenderer)mRenderer).initGL();
        }

        while (mShouldRender != null && mShouldRender.get()) {
            if (mRenderer != null) {
                mRenderer.drawFrame();
            }
            mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);

            try {
                sleep(5);
            } catch(InterruptedException e) {
            }
        }

        destroyGL();
    }
}
