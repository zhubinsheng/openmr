package com.bl.unityhook.render.util;
/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2021 saki t_saki@serenegiant.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GLUtils {
	private static final boolean DEBUG = false;	// set false on production
	private static final String TAG = GLUtils.class.getSimpleName();

	private GLUtils() {
		// インスタンス化を防ぐためにデフォルトコンストラクタをprivateに
	}

	private static int sSupportedGLVersion = -1;

	/**
	 * 対応しているSurfaceかどうかを確認
	 * Surface/SurfaceHolder/SurfaceTexture/SurfaceViewならtrue
	 * @param surface
	 * @return
	 */
	public static boolean isSupportedSurface(@Nullable final Object surface) {
		return ((surface instanceof Surface)
			|| (surface instanceof SurfaceHolder)
			|| (surface instanceof SurfaceTexture)
			|| (surface instanceof SurfaceView));
	}

	/**
	 * 対応しているGL|ESのバージョンを取得
	 * XXX GLES30はAPI>=18以降なんだけどAPI=18でもGLコンテキスト生成に失敗する端末があるのでAP1>=21に変更
	 * API>=21でGL_OES_EGL_image_external_essl3に対応していれば3, そうでなければ2を返す
	 * @return
	 */

	/**
	 * モデルビュー変換行列に左右・上下反転をセット
	 * @param mvp
	 * @param mirror
	 */

	/**
	 * 現在のモデルビュー変換行列をxy平面で指定した角度回転させる
	 * @param mvp
	 * @param degrees
	 */
	public static void rotate(final float[] mvp, final int degrees) {
		if ((degrees % 180) != 0) {
			Matrix.rotateM(mvp, 0, degrees, 0.0f, 0.0f, 1.0f);
		}
	}

	/**
	 * モデルビュー変換行列にxy平面で指定した角度回転させた回転行列をセットする
	 * @param mvp
	 * @param degrees
	 */
	public static void setRotation(final float[] mvp, final int degrees) {
		Matrix.setIdentityM(mvp, 0);
		if ((degrees % 180) != 0) {
			Matrix.rotateM(mvp, 0, degrees, 0.0f, 0.0f, 1.0f);
		}
	}
}
