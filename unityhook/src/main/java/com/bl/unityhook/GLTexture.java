package com.bl.unityhook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.bl.unityhook.render.render.RecordRenderDrawer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicReference;

public class GLTexture {

    private static final AtomicReference<GLTexture> atomicRef = new AtomicReference<>();

    private GLTexture() { /* private empty implement */}

    public static GLTexture getInstance() {
        while (true) {
            if (atomicRef.get() == null) {
                GLTexture instance = new GLTexture();
                atomicRef.compareAndSet(null, instance);
            }
            return atomicRef.get();
        }
    }

    private static final String TAG = "GLTexture";
    private static final String imageFilePath = "/data/data/com.bl.unitybridge/files/image.jpg";

    private Context context;
    public void setContext(Context applicationContext) {
        context = applicationContext;
    }

    private int mTextureID = 0;
    private int mTextureWidth = 0;
    private int mTextureHeight = 0;

    SurfaceTexture mCameraInputSurface;
    SurfaceTexture mOutputSurfaceTexture;
    int mOutputTex[];

    private volatile EGLContext mSharedEglContext;
    private volatile EGLConfig mSharedEglConfig;

    private EGLDisplay mEGLDisplay;
    private EGLContext mEglContext;
    private EGLSurface mEglSurface;

    public int getStreamTextureWidth() {
        //Log.d(TAG,"mTextureWidth = "+ mTextureWidth);
        return mTextureWidth;
    }
    public int getStreamTextureHeight() {
        //Log.d(TAG,"mTextureHeight = "+ mTextureHeight);
        return mTextureHeight;
    }
    public int getStreamTextureID() {
        Log.d(TAG,"getStreamTextureID sucess = "+ mTextureID);
        return mTextureID;
    }

    private void glLogE(String msg) {
        Log.e(TAG, msg + ", err=" + GLES20.glGetError());
    }

    // 被unity调用
    public void setupOpenGL() {
        Log.d(TAG, "setupOpenGL called by Unity ");
    }

    static void sendImage(int width, int height) {
        long star1t = System.currentTimeMillis();

        ByteBuffer rgbaBuf = ByteBuffer.allocateDirect(width * height * 4);
        rgbaBuf.order(ByteOrder.LITTLE_ENDIAN);
        rgbaBuf.position(0);

        //实测种发现通过离屏渲染到fbo上，然后glReadPixels效率比直接读取要快了2倍多。
        GLES30.glReadPixels(0, 0, width, height,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, rgbaBuf);

        reverseBuf(rgbaBuf, width, height);

        saveRgb2Bitmap(rgbaBuf, "/data/data/com.bl.unitybridge/files"
                + "/gl_dump_" + width + "_" + height + System.currentTimeMillis() + ".png", width, height);

    }

    private static void reverseBuf(ByteBuffer buf, int width, int height)
    {
        long ts = System.currentTimeMillis();
        int i = 0;
        byte[] tmp = new byte[width * 4];
        while (i++ < height / 2)
        {
            buf.get(tmp);
            System.arraycopy(buf.array(), buf.limit() - buf.position(), buf.array(), buf.position() - width * 4, width * 4);
            System.arraycopy(tmp, 0, buf.array(), buf.limit() - buf.position(), width * 4);
        }
        buf.rewind();
        Log.d(TAG, "reverseBuf took " + (System.currentTimeMillis() - ts) + "ms");
    }

    static void saveRgb2Bitmap(Buffer buf, String filename, int width, int height) {
        Log.d("TryOpenGL", "Creating " + filename);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename));
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            handler.postDelayed(this, 33);
            if (mRecordDrawer != null){
                mRecordDrawer.draw();
            }
        }
    };

    private Handler handler;
    private int mCameraTextureId;

    private RecordRenderDrawer mRecordDrawer;

    public void updateTime() {
        runnable.run();
    }

    public boolean sendCaptureVideo(int[] ints) {
        mCameraTextureId = ints[1];
        handler = new Handler();

        Runnable initRunnable = new Runnable() {
            @Override
            public void run() {
                mRecordDrawer = new RecordRenderDrawer(context);
                mRecordDrawer.create();
                mRecordDrawer.surfaceChangedSize(1080, 1920);
                mRecordDrawer.setInputTextureId(mCameraTextureId);
            }
        };
        initRunnable.run();
//        handler.post(initRunnable);
//        handler.postDelayed(runnable, 500);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mRecordDrawer.stopRecord();
//            }
//        }, 55000);
        return true;
    }

}