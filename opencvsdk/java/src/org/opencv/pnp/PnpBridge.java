package org.opencv.pnp;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Rect2d;

public class PnpBridge {
    private static final String TAG = PnpBridge.class.getSimpleName();

    private Mat mat = new Mat();

    static {
        System.loadLibrary("pnp");
    }

    public void buildTemplate(Mat mRgba) {
        findObjects(1);
    }

    public void shower(Mat mRgba) {

    }

    private native void findObjects(long nativeObj);

}
