package com.jscheng.scamera.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;
import android.view.MotionEvent;

import com.dyman.easyshow3d.bean.BaseBuilderObject;
import com.dyman.easyshow3d.bean.ModelObject;
import com.dyman.easyshow3d.utils.LoadUtil;
import com.dyman.easyshow3d.utils.MatrixState;
import com.jscheng.scamera.APP;
import com.jscheng.scamera.R;
import com.jscheng.scamera.util.GlesUtil;
import com.jscheng.scamera.util.LogUtil;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created By Chengjunsen on 2018/8/29
 */
public class Stl3DRenderDrawer extends BaseRenderDrawer{
    private int mInputTextureId;

    public ModelRenderer mRenderer;

    public Stl3DRenderDrawer(Context context) {}

    /**
     *  触摸事件回调方法，支持动作：单指旋转，双指缩放
     */
    public boolean onTouchEvent(MotionEvent e) {
        float y = e.getY();
        float x = e.getX();
        Log.d(TAG, "touchMode: " + touchMode);

        switch (e.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                if (touchMode ==TOUCH_NONE && e.getPointerCount() == 1){
                    touchMode = TOUCH_DRAG;
                    mPreviousX = e.getX();
                    mPreviousY = e.getY();
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (e.getPointerCount() >= 2){
                    pinchStartDistance = getPinchDistance(e);
                    if (pinchStartDistance >= 50f){
                        touchMode = TOUCH_ZOOM;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchMode == TOUCH_ZOOM && pinchStartDistance > 0){
                    changeScale = getPinchDistance(e) / pinchStartDistance;
                    wholeScale = changeScale * previousScale;
                } else if(touchMode == TOUCH_DRAG){
                    float dy = y - mPreviousY;//计算触控笔Y位移
                    float dx = x - mPreviousX;//计算触控笔X位移
                    mRenderer.yAngle += dx * TOUCH_SCALE_FACTOR;//设置沿x轴旋转角度
                    mRenderer.zAngle += dy * TOUCH_SCALE_FACTOR;//设置沿z轴旋转角度
                }
//                requestRender();
                mPreviousY = y;//记录触控笔位置
                mPreviousX = x;//记录触控笔位置
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (touchMode == TOUCH_ZOOM){
                    touchMode = TOUCH_NONE;
                    previousScale = wholeScale;//记录缩放倍数
                }
                break;

            case MotionEvent.ACTION_UP:
                if (touchMode == TOUCH_DRAG){ touchMode = TOUCH_NONE; }
                break;
        }
        return true;
    }

    /**
     *  计算两指间的距离
     * @param event
     * @return
     */
    private float getPinchDistance(MotionEvent event) {
        float x=0;
        float y=0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public void setInputTextureId(int textureId) {
        this.mInputTextureId = textureId;
    }

    @Override
    public int getOutputTextureId() {
        return mInputTextureId;
    }

    @Override
    protected void onCreated() {
        mRenderer = new ModelRenderer();
        mRenderer.onSurfaceCreated();
    }

    @Override
    protected void onChanged(int width, int height) {
        mRenderer.onSurfaceChanged(width, height);
    }

    @Override
    public void draw() {
        mRenderer.onDrawFrame();
    }

    @Override
    protected void onDraw() {
    }

    @Override
    protected String getVertexSource() {
        return "";
    }

    @Override
    protected String getFragmentSource() {
        return "";
    }

    private ModelObject modelObject;
    public void setModelObject(ModelObject modelObject) {
        this.modelObject = modelObject;
        mRenderer.setModelObject(modelObject);
    }

    private static final String TAG = "ModelView";

    public final float TOUCH_SCALE_FACTOR = 180.0f/320;//角度缩放比例
    public float mPreviousY;//上次的触控位置Y坐标
    public float mPreviousX;//上次的触控位置X坐标
    public float wholeScale = 1f;//整体的缩放比例
    public float previousScale = 1f;//上次的缩放比例
    public float changeScale = 1f;//缩放改变的比例
    public float pinchStartDistance = 0.0f;

    /**
     * 触摸模式相关
     */
    public static final int TOUCH_NONE = 0;//无
    public static final int TOUCH_ZOOM = 1;//缩放
    public static final int TOUCH_DRAG = 2;//拖拽
    public int touchMode = TOUCH_NONE;

    /**
     * 打印进度计算相关
     */
    public float printProgress = 0;

    /**
     *  初始化模型缩放倍数，使其完全显示在屏幕上
     * @param modelObject
     */
    private void initModelScale(ModelObject modelObject) {
        float maxSize = modelObject.maxX - modelObject.minX;
        if (maxSize < modelObject.maxY-modelObject.minY){
            maxSize = modelObject.maxY-modelObject.minY;
        }
        if (maxSize < modelObject.maxZ-modelObject.minZ){
            maxSize = modelObject.maxZ-modelObject.minZ;
        }
        if (maxSize > 20f) {    //大于20f，缩小模型
            wholeScale = 18f/maxSize;
        } else if(maxSize < 10f) {  //小于10f，放大模型
            wholeScale = 15f/maxSize;
        }
        Log.d(TAG, "wholeScale: " + wholeScale);
    }

    protected void clearFrame() {
        // 清除深度缓冲与颜色缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    }

    /**
     * 渲染器，真正绘制模型的类
     */
    class ModelRenderer{
        public float yAngle;
        public float zAngle;
        private ModelObject modelObject;

        public void setModelObject(ModelObject modelObject) {
            initModelScale(modelObject);
            this.modelObject = modelObject;
        }

        public void onSurfaceCreated() {
            // 设置平模背景色RGBA
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            // 打开深度检测
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            // 打开背面剪裁
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            // 初始化变换矩阵
            MatrixState.setInitStack();
            // 初始化光源位置
            MatrixState.setLightLocation(60, 15 ,30);
        }

        public void onSurfaceChanged(/*GL10 gl10, */int width, int height) {
            // 设置视窗大小及位置
            GLES20.glViewport(0, 0, width, height);
            // 计算GLSurfaceView的宽高比
            float ratio = (float) width/height;
            // 调用次方法计算产生透视投影矩阵
            MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 2, 100);
            // 调用此方法产生摄像机9参数位置矩阵
            MatrixState.setCamera(0,0,0, 0f,0f,-1f, 0f,1.0f,0.0f);
        }

        public void onDrawFrame() {
            if (modelObject == null) {
                return;
            }

            // 画3D模型
            MatrixState.pushMatrix();
            MatrixState.translate(0, -2f, -25f);
            MatrixState.rotate(modelObject.xRotateAngle, 0, 0, 1);
            MatrixState.rotate(yAngle + modelObject.yRotateAngle, 0, 1, 0);
            MatrixState.rotate(zAngle + modelObject.zRotateAngle, 1, 0, 0);
            MatrixState.scale(
                    wholeScale * modelObject.printScale,
                    wholeScale * modelObject.printScale,
                    wholeScale * modelObject.printScale); // 修改整体显示的大小
            //  绘制模型
            if (modelObject.drawWay == ModelObject.DRAW_MODEL){
                modelObject.drawSelf(APP.context);
                //  绘制带进度显示的模型
            }
            MatrixState.popMatrix();
        }
    }
}
