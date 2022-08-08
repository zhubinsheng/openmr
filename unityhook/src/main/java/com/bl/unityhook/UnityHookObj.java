package com.bl.unityhook;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bl.unityhook.render.record.VideoEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Unity调用Android回调的单例对象
 * <li/> WARNING! 不要轻易变动包名、路径、单例函数、函数签名等。此处为函数名约束，同Unity通讯用！！
 */
public class UnityHookObj {

    public static VideoEncoder.DataCallback mDataCallback;

    private static final AtomicReference<UnityHookObj> atomicRef = new AtomicReference<>();

    private static final String TAG = "UnityHookObj";

    private UnityHookObj() { /* private empty implement */}

    public static UnityHookObj getInstance() {
        while (true) {
            if (atomicRef.get() == null) {
                UnityHookObj instance = new UnityHookObj();
                atomicRef.compareAndSet(null, instance);
            }
            return atomicRef.get();
        }
    }

    private final Handler sMainHandler = new Handler(Looper.getMainLooper());

    public String getInitCalibrationParams() {
        CalibrationParams calibrationParams = new CalibrationParams();
        calibrationParams.imageSizeHeight = 1080;
        calibrationParams.imageSizeWidth = 1080;
        calibrationParams.FOV = 8;
        calibrationParams.aspectRatio = 1;
        calibrationParams.nearPlaneDistance = 1;
        calibrationParams.farPlaneDistance = 1;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("imageSizeHeight", 1080);
            jsonObject.put("imageSizeWidth", 1080);
            jsonObject.put("FOV", 8);
            jsonObject.put("aspectRatio", 1);
            jsonObject.put("nearPlaneDistance", 1);
            jsonObject.put("farPlaneDistance", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private float poseX = 0;
    private float poseY = 0;
    private float poseZ = 0;

    public void setCalibrationParams(float poseX, float poseY, float poseZ) {
        this.poseX = poseX;
        this.poseY = poseY;
        this.poseZ = poseZ;
    }

    public String getCalibrationParams() {
//        poseX=poseX+0.01f;
//        poseY=poseY+0.01f;
//        poseZ=poseZ+0.01f;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("poseX", poseX);
            jsonObject.put("poseY", poseY);
            jsonObject.put("poseZ", poseZ);
            jsonObject.put("rotationX", 0);
            jsonObject.put("rotationY", 70);
            jsonObject.put("rotationZ", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public boolean sendSurfaceTextureId(int textureId) {
        Log.d(TAG, "sendSurfaceTextureId:" + textureId);
        return true;
    }

    public boolean sendCaptureVideo(byte[] stream) {
        Log.d(TAG, "sendCaptureVideo:" + stream.length);
        return true;
    }

    public void sendControlHandleCoordinate(String json) {
        Log.d(TAG, "sendControlHandleCoordinate:" + json.length() + "\r\n" + json);
        // {"poseX":0.14069510996341706,"poseY":-0.23511286079883576,"poseZ":0.11220527440309525,
        // "rotationX":-0.13770142197608949,"rotationY":-0.11295188218355179,"rotationZ":0.07818758487701416}
        try {
            JSONObject jsonObject = new JSONObject(json);

            Log.d(TAG, "sendControlHandleCoordinate:" + jsonObject.get("poseX"));
            Log.d(TAG, "sendControlHandleCoordinate:" + jsonObject.get("rotationX"));
            mDataCallback.controlHandleCoordinateData(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        return true;
    }
}
