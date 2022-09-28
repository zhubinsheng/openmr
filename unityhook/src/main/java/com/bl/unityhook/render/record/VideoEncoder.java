package com.bl.unityhook.render.record;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created By Chengjunsen on 2018/9/20
 */
public class VideoEncoder implements Runnable{

    protected final Object mSync = new Object();

    /**
     * Flag to request stop capturing
     */
    protected volatile boolean mRequestStop;

    @Override
    public void run() {
        mRequestStop = false;
        drainEncoder();
    }

    public interface DataCallback{
        void encodedData(ByteBuffer encodedData, MediaCodec.BufferInfo mBufferInfo);

        void outputFormatData(byte[] sps, byte[] pps);

        void controlHandleCoordinateData(JSONObject jsonObject);
    }

    public static DataCallback mDataCallback;

    private static final String TAG = "VideoEncoder";

    private static final int FRAME_RATE = 60;
    private static final int IFRAME_INTERVAL = 1;

    private Surface mInputSurface;
    private MediaMuxer mMuxer;
    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo;
    private int mTrackIndex;
    private boolean mMuxerStarted;

    public VideoEncoder(int width, int height, File outputFile)
            throws IOException {
        int bitRate = height * width * 3 * 8 * FRAME_RATE / 64 ;
        mBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        Log.d(TAG, "format: " + format);


        //Profile越高，就说明采用了越高级的压缩特性。
        //Level越高，视频的码率、分辨率、fps越高
//		format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
        format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel52);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);

        //BITRATE_MODE_CQ: 表示完全不控制码率，尽最大可能保证图像质量
        //BITRATE_MODE_CBR: 表示编码器会尽量把输出码率控制为设定值
        //BITRATE_MODE_VBR: 表示编码器会根据图像内容的复杂度（实际上是帧间变化量的大小）来动态调整输出码率，图像复杂则码率高，图像简单则码率低；
        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);


        mEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mEncoder.createInputSurface();
        mEncoder.start();
        mMuxer = new MediaMuxer(outputFile.toString(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        mTrackIndex = -1;
        mMuxerStarted = false;

        new Thread(this, getClass().getSimpleName()).start();
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void release() {
        Log.d(TAG, "releasing encoder objects");
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }

    public boolean frameAvailableSoon() {
//    	if (DEBUG) Log.v(TAG, "frameAvailableSoon");
        synchronized (mSync) {
            if (mRequestStop) {
                return false;
            }
            mSync.notifyAll();
        }
        return true;
    }

    public void stopRecording() {
        synchronized (mSync) {
            mRequestStop = true;	// for rejecting newer frame
            mEncoder.signalEndOfInputStream();
            mSync.notifyAll();
            // We can not know when the encoding and writing finish.
            // so we return immediately after request to avoid delay of caller thread
        }
    }

    public void drainEncoder() {
        final int TIMEOUT_USEC = 10000;

        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "MediaCodec.INFO_TRY_AGAIN_LATER");
                // no output available yet
                if (!mRequestStop) {
                    synchronized (mSync) {
                        try {
                            mSync.wait();
                        } catch (final InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
//                    break;
                } else {
                    Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.d(TAG, "MediaCodec.INFO_OUTPUT_FORMAT_CHANGED");
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;

                byte[] SPS = newFormat.getByteBuffer("csd-0").array();
                byte[] PPS = newFormat.getByteBuffer("csd-1").array();
                Log.d(TAG," onOutputFormatChanged  SPS  "+ bytesToHex(SPS));
                Log.d(TAG," onOutputFormatChanged  PPS  "+ bytesToHex(PPS));
                if(mDataCallback != null){
                    mDataCallback.outputFormatData(SPS, PPS);
                }

            } else if (encoderStatus < 0) {
                Log.d(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } else {
                ByteBuffer encodedData = mEncoder.getOutputBuffer(encoderStatus);
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    if(mDataCallback != null){
                        mDataCallback.encodedData(encodedData, mBufferInfo);
                    }
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" + mBufferInfo.presentationTimeUs);
                } else {
                    Log.d(TAG, "drainEncoder mBufferInfo: " + mBufferInfo.size);
                }
                mEncoder.releaseOutputBuffer(encoderStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!mRequestStop) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        Log.d(TAG, "end of stream reached");
                        release();
                    }
                    break;
                }
            }
        }
    }

    /**
     * 字节数组转Hex
     * @param bytes 字节数组
     * @return Hex
     */
    public static String bytesToHex(byte[] bytes)
    {
        StringBuffer sb = new StringBuffer();
        if (bytes != null && bytes.length > 0)
        {
            for (int i = 0; i < bytes.length; i++) {
                String hex = byteToHex(bytes[i]);
                sb.append(hex);
            }
        }
        return sb.toString();
    }

    /**
     * Byte字节转Hex
     * @param b 字节
     * @return Hex
     */
    public static String byteToHex(byte b)
    {
        String hexString = Integer.toHexString(b & 0xFF);
        //由于十六进制是由0~9、A~F来表示1~16，所以如果Byte转换成Hex后如果是<16,就会是一个字符（比如A=10），通常是使用两个字符来表示16进制位的,
        //假如一个字符的话，遇到字符串11，这到底是1个字节，还是1和1两个字节，容易混淆，如果是补0，那么1和1补充后就是0101，11就表示纯粹的11
        if (hexString.length() < 2)
        {
            hexString = new StringBuilder(String.valueOf(0)).append(hexString).toString();
        }
        return hexString.toUpperCase();
    }
}
