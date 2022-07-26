package com.bl.unityhook;

public class CalibrationParams {
    public int imageSizeWidth;
    public int imageSizeHeight;
    public float FOV;
    public float aspectRatio;
    public float nearPlaneDistance;
    public float farPlaneDistance;

    public class Posture{
        public float poseX;
        public float poseY;
        public float poseZ;
        public float rotationX;
        public float rotationY;
        public float rotationZ;
    }
}
