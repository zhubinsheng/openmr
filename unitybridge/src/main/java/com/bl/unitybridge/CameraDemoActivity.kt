//package com.bl.unitybridge
//
//import android.os.Build
//import android.os.Bundle
//import android.view.*
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.bl.unityhook.rtsp.CustomRtspServerDisplay
//import com.pedro.encoder.input.video.CameraOpenException
//import com.pedro.rtsp.utils.ConnectCheckerRtsp
//import java.io.File
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.*
//
//class CameraDemoActivity : AppCompatActivity(), ConnectCheckerRtsp, View.OnClickListener,
//    SurfaceHolder.Callback {
//
//  private lateinit var rtspServerCamera1: CustomRtspServerDisplay
//  private lateinit var button: Button
//  private lateinit var bRecord: Button
//
//  private lateinit var surfaceView: SurfaceView
//  private lateinit var switch_camera: Button
//  private lateinit var tv_url: TextView
//
//  private var currentDateAndTime = ""
//  private lateinit var folder: File
//
//  override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//    setContentView(R.layout.activity_camera_demo)
//    folder = File(getExternalFilesDir(null)!!.absolutePath + "/rtmp-rtsp-stream-client-java")
//    button = findViewById(R.id.b_start_stop)
//    button.setOnClickListener(this)
//    bRecord = findViewById(R.id.b_record)
//    bRecord.setOnClickListener(this)
//
//    switch_camera = findViewById(R.id.switch_camera)
//    switch_camera.setOnClickListener(this)
//    surfaceView = findViewById(R.id.surfaceView)
//    tv_url = findViewById(R.id.tv_url)
//    rtspServerCamera1 = CustomRtspServerDisplay(this,false, this, 1935)
//    surfaceView.holder.addCallback(this)
//
//  }
//
//  override fun onNewBitrateRtsp(bitrate: Long) {
//
//  }
//
//  override fun onConnectionSuccessRtsp() {
//    runOnUiThread {
//      Toast.makeText(this@CameraDemoActivity, "Connection success", Toast.LENGTH_SHORT).show()
//    }
//  }
//
//  override fun onConnectionFailedRtsp(reason: String) {
//    runOnUiThread {
//      Toast.makeText(this@CameraDemoActivity, "Connection failed. $reason", Toast.LENGTH_SHORT)
//          .show()
//      rtspServerCamera1.stopStream()
//      button.setText(R.string.start_button)
//    }
//  }
//
//  override fun onConnectionStartedRtsp(rtspUrl: String) {
//  }
//
//  override fun onDisconnectRtsp() {
//    runOnUiThread {
//      Toast.makeText(this@CameraDemoActivity, "Disconnected", Toast.LENGTH_SHORT).show()
//    }
//  }
//
//  override fun onAuthErrorRtsp() {
//    runOnUiThread {
//      Toast.makeText(this@CameraDemoActivity, "Auth error", Toast.LENGTH_SHORT).show()
//      rtspServerCamera1.stopStream()
//      button.setText(R.string.start_button)
//      tv_url.text = ""
//    }
//  }
//
//  override fun onAuthSuccessRtsp() {
//    runOnUiThread {
//      Toast.makeText(this@CameraDemoActivity, "Auth success", Toast.LENGTH_SHORT).show()
//    }
//  }
//
//  override fun onClick(view: View) {
//    when (view.id) {
//      R.id.b_start_stop -> if (!rtspServerCamera1.isStreaming) {
//        if (rtspServerCamera1.isRecording || rtspServerCamera1.prepareVideo()) {
//          button.setText(R.string.stop_button)
//          val surface = rtspServerCamera1.startStream("")
//
//          Thread{
//            EncodeAndMuxTest().testEncodeVideoToMp4(surface)
//          }.start()
//          rtspServerCamera1.startServer()
//          tv_url.text = rtspServerCamera1.getEndPointConnection()
//        } else {
//          Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT)
//              .show()
//        }
//      } else {
//        button.setText(R.string.start_button)
//        rtspServerCamera1.stopStream()
//        tv_url.text = ""
//      }
//      R.id.switch_camera -> try {
////        rtspServerCamera1.switchCamera()
//      } catch (e: CameraOpenException) {
//        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
//      }
//
//      R.id.b_record -> {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//          if (!rtspServerCamera1.isRecording) {
//            try {
//              if (!folder.exists()) {
//                folder.mkdir()
//              }
//              val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
//              currentDateAndTime = sdf.format(Date())
//              if (!rtspServerCamera1.isStreaming) {
//                if (rtspServerCamera1.prepareAudio() && rtspServerCamera1.prepareVideo()) {
////                  rtspServerCamera1.startRecord(folder.absolutePath + "/" + currentDateAndTime + ".mp4")
//                  bRecord.setText(R.string.stop_record)
//                  Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
//                } else {
//                  Toast.makeText(
//                    this, "Error preparing stream, This device cant do it",
//                    Toast.LENGTH_SHORT
//                  ).show()
//                }
//              } else {
////                rtspServerCamera1.startRecord(folder.absolutePath + "/" + currentDateAndTime + ".mp4")
//                bRecord.setText(R.string.stop_record)
//                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
//              }
//            } catch (e: IOException) {
//              rtspServerCamera1.stopRecord()
//              bRecord.setText(R.string.start_record)
//              Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
//            }
//          } else {
//            rtspServerCamera1.stopRecord()
//            bRecord.setText(R.string.start_record)
//            Toast.makeText(
//              this, "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
//              Toast.LENGTH_SHORT
//            ).show()
//          }
//        } else {
//          Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...", Toast.LENGTH_SHORT).show()
//        }
//      }
//      else -> {
//      }
//    }
//  }
//
//  override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
//  }
//
//  override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
////    rtspServerCamera1.startPreview()
//  }
//
//  override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//      if (rtspServerCamera1.isRecording) {
//        rtspServerCamera1.stopRecord()
//        bRecord.setText(R.string.start_record)
//        Toast.makeText(this, "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath, Toast.LENGTH_SHORT).show()
//        currentDateAndTime = ""
//      }
//    }
//    if (rtspServerCamera1.isStreaming) {
//      rtspServerCamera1.stopStream()
//      button.text = resources.getString(R.string.start_button)
//      tv_url.text = ""
//    }
////    rtspServerCamera1.stopPreview()
//  }
//}
