package com.bl.unityhook.render.render;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.bl.unityhook.render.util.GlesUtil;

/**
 * Created By Chengjunsen on 2018/8/31
 * 统一管理所有的RenderDrawer 和 FBO
 */
public class RenderDrawerGroups {
    private final String TAG = getClass().getName();

    private int mInputTexture;
    private int mFrameBuffer;
    private OriginalRenderDrawer mOriginalDrawer;
    private WaterMarkRenderDrawer mWaterMarkDrawer;
    private RecordRenderDrawer mRecordDrawer;

    public RenderDrawerGroups(Context context) {
        this.mOriginalDrawer = new OriginalRenderDrawer();
        this.mWaterMarkDrawer = new WaterMarkRenderDrawer(context);
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
        this.mRecordDrawer.create();
    }

    public void surfaceChangedSize(int width, int height) {
        mFrameBuffer = GlesUtil.createFrameBuffer();
        mOriginalDrawer.surfaceChangedSize(width, height);
        mWaterMarkDrawer.surfaceChangedSize(width, height);
        mRecordDrawer.surfaceChangedSize(width, height);

        this.mOriginalDrawer.setInputTextureId(mInputTexture);
        int textureId = this.mOriginalDrawer.getOutputTextureId();
        mWaterMarkDrawer.setInputTextureId(textureId);
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
            Log.d(TAG, "draw: mInputTexture is zero" + mInputTexture);
            Log.d(TAG, "draw: mFramebuffer  is zero" + mFrameBuffer);
            return;
        }
        drawRender(mOriginalDrawer, true);
        drawRender(mWaterMarkDrawer, true);
//        drawRender(mRecordDrawer, false);
    }

    public void startRecord() {
        mRecordDrawer.startRecord();
    }

    public void stopRecord() {
        mRecordDrawer.stopRecord();
    }
}
