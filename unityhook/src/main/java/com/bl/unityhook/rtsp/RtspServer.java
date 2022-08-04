package com.bl.unityhook.rtsp;

import android.content.Context;
import android.util.Log;
import android.view.Surface;

import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

import java.util.concurrent.atomic.AtomicReference;

public class RtspServer {

    private static final String TAG = "RtspServer";

    private static final AtomicReference<RtspServer> atomicRef = new AtomicReference<>();

    private RtspServer() { /* private empty implement */}

    public static RtspServer getInstance() {
        while (true) {
            if (atomicRef.get() == null) {
                RtspServer instance = new RtspServer();
                atomicRef.compareAndSet(null, instance);
            }
            return atomicRef.get();
        }
    }

    private ConnectCheckerRtsp connectCheckerRtsp = new ConnectCheckerRtsp() {

        @Override
        public void onNewBitrateRtsp(long l) {
            Log.d(TAG, "onNewBitrateRtsp" + l);
        }

        @Override
        public void onDisconnectRtsp() {
            Log.d(TAG, "onDisconnectRtsp" );

        }

        @Override
        public void onConnectionSuccessRtsp() {
            Log.d(TAG, "onConnectionSuccessRtsp" );

        }

        @Override
        public void onConnectionStartedRtsp(String s) {
            Log.d(TAG, "onConnectionStartedRtsp" + s);

        }

        @Override
        public void onConnectionFailedRtsp(String s) {
            Log.d(TAG, "onConnectionFailedRtsp" + s);

        }

        @Override
        public void onAuthSuccessRtsp() {
            Log.d(TAG, "onAuthSuccessRtsp" );

        }

        @Override
        public void onAuthErrorRtsp() {
            Log.d(TAG, "onAuthErrorRtsp" );

        }
    };

    private RtspDisplay displayBase;

    public void prepareStreamRtp() {
        stopStream();
        displayBase = new RtspDisplay(null, false, connectCheckerRtsp);

//        displayBase.getGlInterface().setForceRender(true);
    }

    //rtsp://0.0.0.0:12389/live/99
    public Surface startStreamRtp(String endpoint, int width, int height, int i) {
        if (!displayBase.isStreaming()) {
            if (displayBase.prepareVideo(width, height, i) && displayBase.prepareAudio()) {
                return displayBase.startStream(endpoint);
            }
        } else {
            Log.e(TAG, "You are already streaming");
        }
        return null;
    }

    public void stopStream() {
        if ((displayBase != null && displayBase.isStreaming())) {
            displayBase.stopStream();
        }
    }

    public void startCameraRtspServer(Context context) {
        RtspCamera1 rtspCamera1 = new RtspCamera1(context, connectCheckerRtsp);
        if ( rtspCamera1.prepareVideo()) {
            rtspCamera1.startStream("rtsp://111.229.8.130:28554/live/99");
        } else {
            /**This device cant init encoders, this could be for 2 reasons: The encoder selected doesnt support any configuration setted or your device hasnt a H264 or AAC encoder (in this case you can see log error valid encoder not found)*/
        }
        rtspCamera1.stopStream();
    }
}
