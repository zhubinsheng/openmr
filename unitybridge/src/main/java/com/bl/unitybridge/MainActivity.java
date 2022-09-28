package com.bl.unitybridge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.bl.unityhook.GLTexture;
import com.bl.unityhook.UnityHookObj;
import com.bl.unityhook.render.record.VideoEncoder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pedro.rtpstreamer.displayexample.DisplayService;
import com.unity3d.player.UnityPlayerActivity;

import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE_STREAM = 179; //random num
    private final int REQUEST_CODE_RECORD = 180; //random num

    private SimpleServer server;

    private HandlerThread handlerThread;
    private Handler handler;

    private String bytesToHex(byte[] bytes) {
        String hex = new BigInteger(1, bytes).toString(16);
        return hex;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DisplayService displayService = DisplayService.Companion.getINSTANCE();
        if (displayService != null && !displayService.isStreaming() && !displayService.isRecording()) {
            //stop service only if no streaming or recording
            stopService(new Intent(this, DisplayService.class));
        }
        try {
            server.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && (requestCode == REQUEST_CODE_STREAM
                || requestCode == REQUEST_CODE_RECORD && resultCode == Activity.RESULT_OK)) {
            DisplayService displayService = DisplayService.Companion.getINSTANCE();
            if (displayService != null) {
                String endpoint = "rtsp://111.229.8.130:18554/mystream";
                displayService.prepareStreamRtp(endpoint, resultCode, data);
                displayService.startStreamRtp(endpoint);
            }
        } else {
            Toast.makeText(this, "No permissions available", Toast.LENGTH_SHORT).show();

        }
    }

    public void onClick() {
        DisplayService displayService = DisplayService.Companion.getINSTANCE();
        if (displayService != null) {
                if (!displayService.isStreaming()) {
                    startActivityForResult(displayService.sendIntent(), REQUEST_CODE_STREAM);
                } else {
                    displayService.stopStream();
                }
        }
    }

    private TextView textView;
    private VideoEncoder.DataCallback vd = new VideoEncoder.DataCallback() {
        @Override
        public void encodedData(ByteBuffer encodedData, MediaCodec.BufferInfo mBufferInfo) {
            sendVideoStream(encodedData, mBufferInfo);
        }

        @Override
        public void outputFormatData(byte[] sps, byte[] pps) {
            readyOutputFormatData(sps, pps);
        }

        @Override
        public void controlHandleCoordinateData(JSONObject jsonObject) {
            sendControlHandleCoordinateButton(jsonObject);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GLTexture.getInstance().setContext(getApplicationContext());
        VideoEncoder.mDataCallback = vd;
        UnityHookObj.mDataCallback = vd;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, UnityPlayerActivity.class));
            }
        }, 3000);

/*
        startActivity(new Intent(this, CameraDemoActivity.class));

        DisplayService displayService = DisplayService.Companion.getINSTANCE();
        //No streaming/recording start service
        if (displayService == null) {
            startService(new Intent(this, DisplayService.class));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onClick();
            }
        }, 1000);
*/

        if(Build.VERSION.SDK_INT>=23) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }

        textView = findViewById(R.id.ipStr);

        handlerThread = new HandlerThread("work-thread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String ipAddress = getIpAddress("wlan0");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("IP: "+ipAddress);
                    }
                });
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setMsgCallback();
                        }
                    }, 500);
                    startWebSocketServer();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

/*        handler.post(new Runnable() {
            @Override
            public void run() {
                RtspServer.getInstance().startCameraRtspServer(MainActivity.this);
                RtspServer.getInstance().prepareStreamRtp();
                RtspServer.getInstance().startStreamRtp("rtsp://0.0.0.0:12389/live/99");
            }
        });*/



    }


    private void setMsgCallback() {
        server.setMsgCallback(new SimpleServer.MsgCallback() {
/*            @Override
            public void onMessage(WebSocket conn, String message) {
                try {
                    JSONObject jsonObject = new JSONObject(message);
                    int msgType = jsonObject.get("MsgType");
                    switch (){

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*/

            @Override
            public void onMessage(WebSocket conn, ByteBuffer message) {
                try {
                    MsgProto.Msg msgProto = MsgProto.Msg.parseFrom(message.array());


                    if (msgProto.getType().getNumber() == MsgProto.Msg.MsgType.CalibrationParams_VALUE){
                        LogUtils.d(msgProto.getCalibrationParams());

                        UnityHookObj.getInstance().setFov(
                                msgProto.getCalibrationParams().getXFov(), msgProto.getCalibrationParams().getYFov());
                    }

                    if (msgProto.getType().getNumber() == MsgProto.Msg.MsgType.PhonePosture_VALUE){
                        LogUtils.d(msgProto.getPhonePosture());
                        UnityHookObj.getInstance().setCalibrationParams(
                                msgProto.getPhonePosture().getPosX(),
                                msgProto.getPhonePosture().getPosY(),
                                msgProto.getPhonePosture().getPosZ());

                        UnityHookObj.getInstance().setCalibrationParams2(
                                msgProto.getPhonePosture().getPitchAngle(),
                                msgProto.getPhonePosture().getYawAngle(),
                                msgProto.getPhonePosture().getRollAngle());

                    }

                    if (msgProto.getType().getNumber() == MsgProto.Msg.MsgType.VideoOutputFormat_VALUE){
                        sendOutputFormatData(mSps);
                        sendOutputFormatData(mPps);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void startWebSocketServer() {
        String host = "0.0.0.0";
        int port = 12388;

        server = new SimpleServer(new InetSocketAddress(host, port));
        server.run();
    }

    public boolean sendMsg(byte[] msg) {
        Log.i("sendMsg", Arrays.toString(msg));
        server.broadcast(msg);
        return true;
    }

    private byte[] mSps;
    private byte[] mPps;

    public void readyOutputFormatData(byte[] sps, byte[] pps){
        this.mSps = sps;
        this.mPps = pps;
    }

    public void sendOutputFormatData(byte[] b) {
        if (b != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MsgProto.Msg msgProto = MsgProto.Msg
                            .newBuilder()
                            .setType(MsgProto.Msg.MsgType.VideoOutputFormat)
                            .setVideoStream(ByteString.copyFrom(b))
                            .build();
                    sendMsg(msgProto.toByteArray());
                }
            });
        }
    }

    public void sendVideoStream(ByteBuffer encodedData, MediaCodec.BufferInfo mBufferInfo) {
        encodedData.position(mBufferInfo.offset);
        encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
        byte[] b = new byte[mBufferInfo.size];
        encodedData.get(b);
        handler.post(new Runnable() {
            @Override
            public void run() {
                MsgProto.Msg msgProto = MsgProto.Msg
                        .newBuilder()
                        .setType(MsgProto.Msg.MsgType.VideoStream)
                        .setVideoStream(ByteString.copyFrom(b))
                        .build();
                sendMsg(msgProto.toByteArray());
            }
        });
    }

    public void sendControlHandleCoordinateButton(JSONObject jsonObject) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                MsgProto.Msg msgProto = null;
                try {
                    msgProto = MsgProto.Msg
                            .newBuilder()
                            .setType(MsgProto.Msg.MsgType.ControlHandleCoordinate)
                            .setControlHandleCoordinate
                                    (
                                    MsgProto.ControlHandleCoordinate
                                            .newBuilder()
                                            .setPosX( ((Double) jsonObject.get("poseX")).floatValue() )
                                            .setPosY( ((Double) jsonObject.get("poseY")).floatValue() )
                                            .setPosZ( ((Double) jsonObject.get("poseZ")).floatValue() )
                                            .setPitchAngle( ((Double) jsonObject.get("rotationX")).floatValue() )
                                            .setYawAngle( ((Double) jsonObject.get("rotationY")).floatValue() )
                                            .setRollAngle( ((Double) jsonObject.get("rotationZ")).floatValue() )
                                    )
                            .build();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendMsg(msgProto.toByteArray());
            }
        });
    }

    public void sendVideoStreamButton(View view) {
        for (int i = 0; i < 10; i++) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MsgProto.Msg msgProto = MsgProto.Msg
                            .newBuilder()
                            .setType(MsgProto.Msg.MsgType.VideoStream)
                            .setVideoStream(ByteString.copyFrom("1234567890qazesxrdctfvgbyuhnijm".getBytes()))
                            .build();

                    sendMsg(msgProto.toByteArray());
                }
            });
        }
    }


    /**
     * Get Ip address 自动获取IP地址
     *
     * @throws SocketException
     */
    public static String getIpAddress(String ipType) {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();

                Log.e("getIpAddress", ": 开机获取ip="+ni.getName() );
                if (ni.getName().equals(ipType)) {

                    Enumeration<InetAddress> ias = ni.getInetAddresses();
                    while (ias.hasMoreElements()) {

                        ia = ias.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;// skip ipv6
                        }
                        String ip = ia.getHostAddress();

                        // 过滤掉127段的ip地址
                        if (!"127.0.0.1".equals(ip)) {
                            hostIp = ia.getHostAddress();
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.d("getIpAddress", "手机IP地址get the IpAddress--> " + hostIp + "");
        return hostIp;
    }
}