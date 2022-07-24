#include <opencv2/opencv.hpp>
#include <vector>
#include <jni.h>
#include<android/log.h>

using namespace std;
using namespace cv;

#define TAG "pnp-jni" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

jlong getNativeObj(JNIEnv *env, jclass jcInfo, jobject jobj, const char string[], const char string1[]) {
    jfieldID jfi = env->GetFieldID(jcInfo, string, string1);
    jlong jl = env->GetLongField(jobj, jfi);
    return jl;
}

extern "C"
JNIEXPORT void JNICALL
Java_org_opencv_pnp_PnpBridge_findObjects(JNIEnv *env, jobject thiz, jlong native_obj) {

    // Read input image
    cv::Mat im = cv::imread("/sdcard/aaaaa/headPose.jpg");

    // 2D image points. If you change the image, you need to change vector
    std::vector<cv::Point2d> image_points;
    image_points.push_back( cv::Point2d(359, 391) );    // Nose tip
    image_points.push_back( cv::Point2d(399, 561) );    // Chin
    image_points.push_back( cv::Point2d(337, 297) );     // Left eye left corner
    image_points.push_back( cv::Point2d(513, 301) );    // Right eye right corner
    image_points.push_back( cv::Point2d(345, 465) );    // Left Mouth corner
    image_points.push_back( cv::Point2d(453, 469) );    // Right mouth corner

    // 3D model points.
    std::vector<cv::Point3d> model_points;
    model_points.push_back(cv::Point3d(0.0f, 0.0f, 0.0f));               // Nose tip
    model_points.push_back(cv::Point3d(0.0f, -330.0f, -65.0f));          // Chin
    model_points.push_back(cv::Point3d(-225.0f, 170.0f, -135.0f));       // Left eye left corner
    model_points.push_back(cv::Point3d(225.0f, 170.0f, -135.0f));        // Right eye right corner
    model_points.push_back(cv::Point3d(-150.0f, -150.0f, -125.0f));      // Left Mouth corner
    model_points.push_back(cv::Point3d(150.0f, -150.0f, -125.0f));       // Right mouth corner

    // Camera internals
    double focal_length = im.cols; // Approximate focal length.
    Point2d center = cv::Point2d(im.cols/2,im.rows/2);
    cv::Mat camera_matrix = (cv::Mat_<double>(3,3) << focal_length, 0, center.x, 0 , focal_length, center.y, 0, 0, 1);
    cv::Mat dist_coeffs = cv::Mat::zeros(4,1,cv::DataType<double>::type); // Assuming no lens distortion

    cout << "Camera Matrix " << endl << camera_matrix << endl ;
    // Output rotation and translation
    cv::Mat rotation_vector; // Rotation in axis-angle form
    cv::Mat translation_vector;

    // Solve for pose
    cv::solvePnP(model_points, image_points, camera_matrix, dist_coeffs, rotation_vector, translation_vector);

    // Project a 3D point (0, 0, 1000.0) onto the image plane.
    // We use this to draw a line sticking out of the nose

    vector<Point3d> nose_end_point3D;
    vector<Point2d> nose_end_point2D;
    nose_end_point3D.push_back(Point3d(0,0,1000.0));

    projectPoints(nose_end_point3D, rotation_vector, translation_vector, camera_matrix, dist_coeffs, nose_end_point2D);

    for(int i=0; i < image_points.size(); i++)
    {
        circle(im, image_points[i], 3, Scalar(0,0,255), -1);
    }

    cv::line(im,image_points[0], nose_end_point2D[0], cv::Scalar(255,0,0), 2);

    cout << "Rotation Vector " << endl << rotation_vector << endl;
    cout << "Translation Vector" << endl << translation_vector << endl;

    cout <<  nose_end_point2D << endl;

    // Display image.
    cv::imwrite("/sdcard/aaaaa/headPose-result.jpg", im);

}