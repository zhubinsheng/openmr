package com.jscheng.scamera.render;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.GLES30;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dyman.easyshow3d.bean.ModelObject;
import com.jscheng.scamera.util.GlesUtil;

import static com.jscheng.scamera.util.LogUtil.TAG;

/**
 * Created By Chengjunsen on 2018/8/31
 * 统一管理所有的RenderDrawer 和 FBO
 */
public class RenderDrawerGroups {
    private int mInputTexture;
    private int mFrameBuffer;
    private OriginalRenderDrawer mOriginalDrawer;
    private WaterMarkRenderDrawer mWaterMarkDrawer;
    private DisplayRenderDrawer mDisplayDrawer;
    private RecordRenderDrawer mRecordDrawer;

    private Stl3DRenderDrawer stl3DRenderDrawer;

    public RenderDrawerGroups(Context context) {
        this.mOriginalDrawer = new OriginalRenderDrawer();
        this.mWaterMarkDrawer = new WaterMarkRenderDrawer(context);
        this.stl3DRenderDrawer = new Stl3DRenderDrawer(context);
        this.mDisplayDrawer = new DisplayRenderDrawer();
        this.mRecordDrawer = new RecordRenderDrawer(context);
        this.mFrameBuffer = 0;
        this.mInputTexture = 0;
    }

    public void setInputTexture(int texture) {
        this.mInputTexture = texture;
    }

    public void bindFrameBuffer(int textureId) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffer);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textureId, 0);
    }

    public void unBindFrameBuffer() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    public void deleteFrameBuffer() {
        GLES30.glDeleteFramebuffers(1, new int[]{mFrameBuffer}, 0);
        GLES30.glDeleteTextures(1, new int[]{mInputTexture}, 0);
    }

    public void create() {
        this.mOriginalDrawer.create();
        this.mWaterMarkDrawer.create();
        this.stl3DRenderDrawer.create();
        this.mDisplayDrawer.create();
        this.mRecordDrawer.create();
    }

    public void surfaceChangedSize(int width, int height) {
        mFrameBuffer = GlesUtil.createFrameBuffer();
        mOriginalDrawer.surfaceChangedSize(width, height);
        mWaterMarkDrawer.surfaceChangedSize(width, height);
        stl3DRenderDrawer.surfaceChangedSize(width, height);
        mDisplayDrawer.surfaceChangedSize(width, height);
        mRecordDrawer.surfaceChangedSize(width, height);

        this.mOriginalDrawer.setInputTextureId(mInputTexture);
        int textureId = this.mOriginalDrawer.getOutputTextureId();
        mWaterMarkDrawer.setInputTextureId(textureId);
        stl3DRenderDrawer.setInputTextureId(textureId);
        mDisplayDrawer.setInputTextureId(textureId);
        mRecordDrawer.setInputTextureId(textureId);
    }

    public void drawRender(BaseRenderDrawer drawer, boolean useFrameBuffer) {
        if (useFrameBuffer) {
            bindFrameBuffer(drawer.getOutputTextureId());
        }
        drawer.draw();
        if (useFrameBuffer) {
            unBindFrameBuffer();
        }
    }

    public void draw() {
        if (mInputTexture == 0 || mFrameBuffer == 0) {
            Log.e(TAG, "draw: mInputTexture or mFramebuffer or list is zero");
            return;
        }
        drawRender(mOriginalDrawer, true);
        if (modelObject != null){
            drawRender(stl3DRenderDrawer, true);
        }
        // 绘制顺序会控制着 水印绘制哪一层
        drawRender(mWaterMarkDrawer, true);

        drawRender(mDisplayDrawer, false);
//        drawRender(mWaterMarkDrawer, true);
//        drawRender(mRecordDrawer, false);
    }

    public void startRecord() {
        mRecordDrawer.startRecord();
    }

    public void stopRecord() {
        mRecordDrawer.stopRecord();
    }

    private ModelObject modelObject;
    public void setModelObject(ModelObject modelObject) {
        this.modelObject = modelObject;
        stl3DRenderDrawer.setModelObject(modelObject);
    }

    public boolean onTouchEvent(MotionEvent e) {
        return stl3DRenderDrawer.onTouchEvent(e);
    }

    public View.OnClickListener getOnClickListener() {
        return stl3DRenderDrawer.getOnClickListener();
    }
}
