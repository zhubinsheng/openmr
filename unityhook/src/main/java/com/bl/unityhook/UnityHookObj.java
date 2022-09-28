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

     /*jsonObject.put("imageSizeWidth", 1920);
    jsonObject.put("imageSizeHeight", 1442);
    jsonObject.put("xFov", 78.78722158745441);
    jsonObject.put("yFov", 61.59041647706971);
    jsonObject.put("aspectRatio", 1920 / 1442);
    jsonObject.put("nearPlaneDistance", 0.01);
    jsonObject.put("farPlaneDistance", 1000);*/
    private float xFOV = 78;
    private float yFOV = 61;

    public void setFov(float x, float y) {
        this.xFOV = x;
        this.yFOV = y;
    }

    public String getInitCalibrationParams() {
        CalibrationParams calibrationParams = new CalibrationParams();
        calibrationParams.imageSizeHeight = 1442;
        calibrationParams.imageSizeWidth = 1920;
        calibrationParams.xFOV = 60;
        calibrationParams.yFOV = 60;

        calibrationParams.aspectRatio = 1920f / 1442f;
        calibrationParams.nearPlaneDistance = 0.01f;
        calibrationParams.farPlaneDistance = 1000;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("imageSizeHeight", 1442);
            jsonObject.put("imageSizeWidth", 1920);
            jsonObject.put("xFOV", xFOV);
            jsonObject.put("yFOV", yFOV);

            jsonObject.put("aspectRatio", 1920 / 1442);
            jsonObject.put("nearPlaneDistance", 0.01);
            jsonObject.put("farPlaneDistance", 1000);
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

    private float rotationX = 0;
    private float rotationY = 70;
    private float rotationZ = 0;

    public void setCalibrationParams2(float rotationX, float rotationY, float rotationZ) {
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
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
            jsonObject.put("rotationX", rotationX);
            jsonObject.put("rotationY", rotationY);
            jsonObject.put("rotationZ", rotationZ);
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
            if (mDataCallback != null){
                mDataCallback.controlHandleCoordinateData(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        return true;
    }
}
