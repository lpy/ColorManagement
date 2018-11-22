package graphicsexample.com.colormanagementexample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.ColorSpace.Named;
import android.graphics.ImageDecoder;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.opengl.GLUtils;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class TextureHelper {
    private static final String TAG = "TextureHelper";

    public static int loadTexture(Context context, int resourceId) {
        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            return 0;
        }
        Bitmap bitmap = null;

        if (Build.VERSION.SDK_INT >= 28) {
            Log.e(TAG, "Use ImageDecoder API");
            ImageDecoder.Source source = ImageDecoder.createSource(context.getResources(), resourceId);
            try {
                bitmap = ImageDecoder.decodeBitmap(source, new ImageDecoder.OnHeaderDecodedListener() {
                    @Override
                    public void onHeaderDecoded(ImageDecoder imageDecoder,
                                                ImageDecoder.ImageInfo imageInfo,
                                                ImageDecoder.Source source) {
                        if (Build.VERSION.SDK_INT >= 28) {
                            Log.e(TAG, "Image color space: " + imageInfo.getColorSpace().getName());
                            imageDecoder.setTargetColorSpace(ColorSpace.get(Named.DISPLAY_P3));
                            imageDecoder.setMutableRequired(true);
                        }
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "HAHA??? " + e.toString());
                e.printStackTrace();
            }
        } else {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inPreferredColorSpace = ColorSpace.get(Named.DISPLAY_P3);
            bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        }

        if (bitmap == null) {
            Log.e(TAG, "HAHA?");
            GLES20.glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }
        Log.e(TAG, "bitmap color space " + bitmap.getColorSpace() + " " + bitmap.getConfig());
        {
            int color = bitmap.getPixel(5, 5);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            Log.e(TAG, "DDEBUG color: (" + red + ", " + green + ", " + blue + ")");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES31.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES31.GL_RGBA,
                bitmap.getWidth(), bitmap.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        int error = GLES31.glGetError();
        if (error != 0) {
            Log.e(TAG, "SOMETHING IS WRONG with glTexImage2D " + error);
        }
        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE);
        error = GLES31.glGetError();
        if (error != 0) {
            Log.e(TAG, "SOMETHING IS WRONG with GLUtils.texSubImage2D " + error);
        }

        // GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, bitmap, 0);
        bitmap.recycle();
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureObjectIds[0];
    }
}