package graphicsexample.com.colormanagementexample.surfaceview;

import android.content.Context;
import android.opengl.GLSurfaceView;

import graphicsexample.com.colormanagementexample.CMEGLWindowSurfaceFactory;
import graphicsexample.com.colormanagementexample.CMEglConfigChooser;
import graphicsexample.com.colormanagementexample.surfaceview.CMGLRenderer;

public class CMGLSurfaceView extends GLSurfaceView {

    public CMGLSurfaceView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(3);

        setEGLWindowSurfaceFactory(new CMEGLWindowSurfaceFactory());
        setEGLConfigChooser(new CMEglConfigChooser());

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(new CMGLRenderer(context));

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
