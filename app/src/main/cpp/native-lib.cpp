#include <jni.h>
#include <string>
#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/gapi/video.hpp>
#include <android/bitmap.h>
#include <dlib/image_io.h>
#include <dlib/image_processing.h>
#include <dlib/image_processing/generic_image.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/opencv/cv_image.h>
#include <dlib/opencv.h>
#include <opencv2/video/tracking.hpp>
#include "RPPG.h"
//#include "face.h"

#include "FaceDetectorYNCreated.h"


#define JNI_METHOD(NAME) \
    Java_com_dev_anzalone_luca_facelandmarks_Native_##NAME

#define KERNEL_SIZE 5 // 3, 5, 7, 9

#define NV21 17
#define YV12 842094169
#define YUV_420_888 35
#define PYRAMIDS 3
#define MAX_FRAME_COUNT 5

using namespace std;

// global variables:
dlib::shape_predictor shape_predictor;
std::mutex _mutex;
int imageFormat = NV21;

#define LOG_TAG "Heartbeat::NativeLib"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))


using namespace cv;
using namespace std;
// -------------------------------------------------------------------------------------------------
// -- Lucas-Kanade Optical Flow Tracker
// -------------------------------------------------------------------------------------------------
namespace LK {
    // variables
    int frameCount = 0;
    bool isTracking = false;
    cv::Mat prev_img;
    vector<cv::Point2f> prev_pts;
    vector<cv::Point2f> next_pts;
    cv::TermCriteria criteria(cv::TermCriteria::COUNT | cv::TermCriteria::EPS, 25, 0.01);
    cv::Size ROI(20, 20);

    /** Initialize tracking with the current frame and detected landmarks */
    void start(cv::Mat &mat, dlib::full_object_detection &pts) {
        // release stuff..
        prev_img.release();
        prev_img = mat;
        prev_pts.clear();
        next_pts.clear();

        // consider the new points
        for (unsigned long i = 0; i < pts.num_parts(); i++) {
            auto pt = pts.part(i);
            prev_pts.push_back(cv::Point2f(pt.x(), pt.y()));
        }

        // reset count
        frameCount = 0;
        isTracking = true;
    }

    /** tracking points in the next captured frame */
    vector<cv::Point2f> track(cv::Mat &frame) {
        vector<uchar> status;
        vector<float> err;
        vector<cv::Point2f> tracked;

        // get the new points from the old one
        cv::calcOpticalFlowPyrLK(prev_img, frame, prev_pts, next_pts, status, err,
                                 ROI, PYRAMIDS, criteria);

        for (int i = 0; i < status.size(); ++i) {
            if (status[i] == 0) {
                // flow not found: take the old point
                tracked.push_back(prev_pts[i]);
            } else {
                // flow found: take the new point
                tracked.push_back(next_pts[i]);
            }
        }

        // switch the previous points and image with the current
        swap(prev_img, frame);
        swap(prev_pts, tracked);
        next_pts.clear();

        // increase tracking frame count
        if (frameCount++ > MAX_FRAME_COUNT) {
            isTracking = false;
        }

        return prev_pts;
    }
}
// -------------------------------------------------------------------------------------------------


void rotateMat(cv::Mat &mat, int rotation) {
    if (rotation == 90) { // portrait
        cv::transpose(mat, mat);
        cv::flip(mat, mat, -1);
    } else if (rotation == 0) { // landscape-left
        cv::flip(mat, mat, 1);
    } else if (rotation == 180) { // landscape-right
        cv::flip(mat, mat, 0);
    }
}

extern "C"
JNIEXPORT void JNICALL
JNI_METHOD(setImageFormat)(JNIEnv *env, jclass, jint format) {
    imageFormat = format;
}



extern "C"
JNIEXPORT void JNICALL
Java_com_example_livenativerppg_models_mainRun_vw_MainActivity_ProcessSkinNatively(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jlong frm,
                                                                                   jlong dst) {
    Mat hsv = Mat(), ycbcr = Mat(), globalMask = Mat();

    Mat &f = *(Mat *) frm;
    Mat &d = *(Mat *) dst;
    cv::cvtColor(f, hsv, COLOR_BGR2HSV);
    cv::inRange(hsv, Scalar(0, 15, 0), Scalar(17, 170, 255), hsv);
    morphologyEx(hsv, hsv, MORPH_OPEN, Mat::ones(3, 3, CV_8U));

    cvtColor(f, ycbcr, COLOR_BGR2YCrCb);
    inRange(ycbcr, Scalar(0, 135, 85), Scalar(255, 180, 135), ycbcr);
    morphologyEx(ycbcr, ycbcr, MORPH_OPEN, Mat::ones(3, 3, CV_8U));

    bitwise_and(ycbcr, hsv, globalMask);
    medianBlur(globalMask, globalMask, 3);
    morphologyEx(globalMask, globalMask, MORPH_OPEN, Mat::ones(3, 3, CV_8U));

    bitwise_not(hsv, hsv);
    bitwise_not(ycbcr, ycbcr);
    bitwise_not(globalMask, globalMask);

    Size size = f.size();
    for (unsigned int i = 0; i < size.height; i++) {
        for (unsigned int j = 0; j < size.width; j++) {
            if (globalMask.at<Vec3f>(i, j)[0] == 0) {
                f.at<Vec3f>(i, j) = {0, 0, 0};
            }
        }
    }
    f.copyTo(d);
//    rotate(d, d, ROTATE_90_CLOCKWISE);
}

//--------------------------------------------------------------------------------------------------
//-- LANDMARK DETECTION
//--------------------------------------------------------------------------------------------------
extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_example_livenativerppg_models_mainRun_vw_MainActivity_detectLandmarks(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jbyteArray yuvFrame,
                                                                               jint rotation,
                                                                               jint width,
                                                                               jint height,
                                                                               jint left, jint top,
                                                                               jint right,
                                                                               jint bottom) {
    // copy content of frame into image
    jbyte *data = env->GetByteArrayElements(yuvFrame, 0);

    // convert yuv-frame to cv::Mat
    cv::Mat yuvMat(height + height / 2, width, CV_8UC1, (unsigned char *) data);
    cv::Mat grayMat(height, width, CV_8UC1);

    // to grayscale
    if (imageFormat == NV21)
        cv::cvtColor(yuvMat, grayMat, cv::COLOR_YUV2GRAY_NV21);  // was CV_YUV2GRAY_NV21
    else if (imageFormat == YV12)
        cv::cvtColor(yuvMat, grayMat, cv::COLOR_YUV2GRAY_YV12);  // was CV_YUV2GRAY_YV12

    // adjust rotation according to phone orientation
    rotateMat(grayMat, rotation);

    // crop face for enhancements
    cv::Rect faceROI(left, top, right - left, bottom - top);
    cv::Mat face = grayMat(faceROI);

    // apply filters
    cv::medianBlur(face, face, KERNEL_SIZE);  // remove noise
    cv::equalizeHist(face, face);  // improve contrast

    if (!LK::isTracking) {
        // -- DETECT LANDMARKS -- //

        // cv::mat to dlib::image
        dlib::cv_image<unsigned char> image(grayMat);

        // detect landmark points
        _mutex.lock();
        dlib::rectangle region(left, top, right, bottom);
        dlib::full_object_detection points = shape_predictor(image, region);
        _mutex.unlock();

        // result
        auto num_points = points.num_parts();
        auto len = (jsize) (num_points * sizeof(short)); // num_points * 2

        jlong buffer[len];
        jlongArray result = env->NewLongArray(len);

        // copy points in the buffer
        auto k = 0;
        for (unsigned long i = 0l; i < num_points; ++i) {
            dlib::point p = points.part(i);
            buffer[k++] = p.x();
            buffer[k++] = p.y();
        }

        // set the content of buffer into result array
        env->SetLongArrayRegion(result, 0, len, buffer);

        // free mem
        env->ReleaseByteArrayElements(yuvFrame, data, 0);

        // uncomment to enable tracking for the next frames
//        LK::start(grayMat, points);

        return result;

    } else {
        // -- COMPUTE LK-OPTICAL FLOW --
        auto trackedPts = LK::track(grayMat);

        // result
        auto num_points = trackedPts.size();
        jsize len = (jsize) (num_points * sizeof(short)); // num_points * 2

        jlong buffer[len];
        jlongArray result = env->NewLongArray(len);

        // copy tracked points in the buffer
        auto k = 0;
        for (unsigned long i = 0l; i < num_points; ++i) {
            auto p = trackedPts[i];
            buffer[k++] = static_cast<jlong>(p.x);
            buffer[k++] = static_cast<jlong>(p.y);
        }

        // set the content of buffer into result array
        env->SetLongArrayRegion(result, 0, len, buffer);

        // free mem
        env->ReleaseByteArrayElements(yuvFrame, data, 0);

        return result;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_livenativerppg_utils_Native_loadModel(JNIEnv *env, jclass clazz,
                                                       jstring detectorPath) {
    try {
        const char *path = env->GetStringUTFChars(detectorPath, JNI_FALSE);

        _mutex.lock();
        // cause the later initialization of the tracking
        LK::isTracking = false;

        // load the shape predictor
        dlib::deserialize(path) >> shape_predictor;
        _mutex.unlock();

        env->ReleaseStringUTFChars(detectorPath, path); //free mem
    } catch (dlib::serialization_error &e) {
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_livenativerppg_utils_Native_setImageFormat(JNIEnv *env, jclass clazz,
                                                            jint format) {
    imageFormat = format;
}
extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_example_livenativerppg_utils_Native_detectLandmarks(JNIEnv *env, jclass clazz,
                                                             jbyteArray yuvFrame,
                                                             jint rotation, jint width, jint height,
                                                             jint left, jint top, jint right,
                                                             jint bottom) {
    // copy content of frame into image
    jbyte *data = env->GetByteArrayElements(yuvFrame, 0);

    // convert yuv-frame to cv::Mat
    cv::Mat yuvMat(height + height / 2, width, CV_8UC1, (unsigned char *) data);
    cv::Mat grayMat(height, width, CV_8UC1);

    // to grayscale
    if (imageFormat == NV21)
        cv::cvtColor(yuvMat, grayMat, cv::COLOR_YUV2GRAY_NV21);  // was CV_YUV2GRAY_NV21
    else if (imageFormat == YV12)
        cv::cvtColor(yuvMat, grayMat, cv::COLOR_YUV2GRAY_YV12);  // was CV_YUV2GRAY_YV12

    // adjust rotation according to phone orientation
    rotateMat(grayMat, rotation);

    // crop face for enhancements
    cv::Rect faceROI(left, top, right - left, bottom - top);
    cv::Mat face = grayMat(faceROI);

    // apply filters
    cv::medianBlur(face, face, KERNEL_SIZE);  // remove noise
    cv::equalizeHist(face, face);  // improve contrast

    if (!LK::isTracking) {
        // -- DETECT LANDMARKS -- //

        // cv::mat to dlib::image
        dlib::cv_image<unsigned char> image(grayMat);

        // detect landmark points
        _mutex.lock();
        dlib::rectangle region(left, top, right, bottom);
        dlib::full_object_detection points = shape_predictor(image, region);
        _mutex.unlock();

        // result
        auto num_points = points.num_parts();
        auto len = (jsize) (num_points * sizeof(short)); // num_points * 2

        jlong buffer[len];
        jlongArray result = env->NewLongArray(len);

        // copy points in the buffer
        auto k = 0;
        for (unsigned long i = 0l; i < num_points; ++i) {
            dlib::point p = points.part(i);
            buffer[k++] = p.x();
            buffer[k++] = p.y();
        }

        // set the content of buffer into result array
        env->SetLongArrayRegion(result, 0, len, buffer);

        // free mem
        env->ReleaseByteArrayElements(yuvFrame, data, 0);

        // uncomment to enable tracking for the next frames
//        LK::start(grayMat, points);

        return result;

    } else {
        // -- COMPUTE LK-OPTICAL FLOW --
        auto trackedPts = LK::track(grayMat);

        // result
        auto num_points = trackedPts.size();
        jsize len = (jsize) (num_points * sizeof(short)); // num_points * 2

        jlong buffer[len];
        jlongArray result = env->NewLongArray(len);

        // copy tracked points in the buffer
        auto k = 0;
        for (unsigned long i = 0l; i < num_points; ++i) {
            auto p = trackedPts[i];
            buffer[k++] = static_cast<jlong>(p.x);
            buffer[k++] = static_cast<jlong>(p.y);
        }

        // set the content of buffer into result array
        env->SetLongArrayRegion(result, 0, len, buffer);

        // free mem
        env->ReleaseByteArrayElements(yuvFrame, data, 0);

        return result;
    }

}

void GetJStringContent(JNIEnv *AEnv, jstring AStr, std::string &ARes) {
    if (!AStr) {
        ARes.clear();
        return;
    }
    const char *s = AEnv->GetStringUTFChars(AStr, NULL);
    ARes = s;
    AEnv->ReleaseStringUTFChars(AStr, s);
}

void rethrow_cpp_exception_as_java_exception(JNIEnv *env) {
    try {
        throw;
    }
    catch (const exception &e) {
        jclass jc = env->FindClass("org/opencv/core/CvException");
        if (jc) env->ThrowNew(jc, e.what());
        /* if null => NoClassDefFoundError already thrown */
    }
    catch (const std::bad_alloc &e) {
        /* OOM exception */
        jclass jc = env->FindClass("java/lang/OutOfMemoryError");
        if (jc) env->ThrowNew(jc, e.what());
    }
    catch (const std::ios_base::failure &e) {
        /* IO exception */
        jclass jc = env->FindClass("java/io/IOException");
        if (jc) env->ThrowNew(jc, e.what());
    }
    catch (const std::exception &e) {
        /* unknown exception */
        jclass jc = env->FindClass("java/lang/Error");
        if (jc) env->ThrowNew(jc, e.what());
    }
    catch (...) {
        /* Oops I missed identifying this exception! */
        jclass jc = env->FindClass("java/lang/Error");
        if (jc) env->ThrowNew(jc, "unexpected exception");
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_livenativerppg_component_natives_RPPG__1initialise(JNIEnv *env, jobject thiz) {
    jlong result = 0;
    try {
        result = (jlong) new RPPG();
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code.");
    }
    return result;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_livenativerppg_component_natives_RPPG__1load(JNIEnv *env, jobject thiz, jlong self,
                                                             jobject jlistener, jint jalgorithm,
                                                             jint jwidth, jint jheight,
                                                             jdouble jtimeBase, jint jdownsample,
                                                             jdouble jsamplingFrequency,
                                                             jdouble jrescanFrequency,
                                                             jint jminSignalSize,
                                                             jint jmaxSignalSize, jstring jlogPath,
                                                             jstring jclassifierPath,
                                                             jboolean jlog, jboolean jgui , jboolean isBP,
                                                             jdouble Wei,
                                                             jdouble Hei,
                                                             jfloat Agg,
                                                             jfloat Q
                                                             ) {
    bool log = jlog;
    bool gui = jgui;
    std::string logPath, classifierPath;
    try {
        GetJStringContent(env, jlogPath, logPath);
        GetJStringContent(env, jclassifierPath, classifierPath);
        ((RPPG *) self)->load(jlistener, env, jalgorithm, jwidth, jheight, jtimeBase, jdownsample,
                              jsamplingFrequency, jrescanFrequency, jminSignalSize, jmaxSignalSize,
                              logPath, classifierPath, log, gui , isBP, Wei ,Hei , Agg , Q);
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code.");
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_livenativerppg_component_natives_RPPG__1processFrame(JNIEnv *env, jobject thiz,
                                                                     jlong self, jlong frame_rgb,
                                                                     jlong frame_gray,
                                                                     jdouble jtime) {
    try {
        int time = jtime;
        ((RPPG *) self)->processFrame(*((cv::Mat *) frame_rgb), *((cv::Mat *) frame_gray),
                                      time);
//        on_image_render(*((cv::Mat *) frame_rgb));
    } catch (...) {
        rethrow_cpp_exception_as_java_exception(env);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_livenativerppg_component_natives_RPPG__1processFrameWithNoFD(JNIEnv *env, jobject thiz,
                                                                              jlong self, jlong frame_rgb,
                                                                              jlong frame_gray,
                                                                              jlong jMask,
                                                                              jdouble jtime) {
    try {
        int time = jtime;
        ((RPPG *) self)->processFrameWithNoFD(*((cv::Mat *) frame_rgb), *((cv::Mat *) frame_gray), *((cv::Mat *) jMask),
                                      time);
    } catch (...) {
        rethrow_cpp_exception_as_java_exception(env);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_livenativerppg_component_natives_RPPG__1exit(JNIEnv *env, jobject thiz,
                                                             jlong self) {
    try {
        ((RPPG *) self)->exit(env);
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code.");
    }
}

std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_livenativerppg_models_faceDetectYNCreated_ui_FeceDetectorYNCreatedActivity__1init(
        JNIEnv *env, jobject thiz, jstring fd_model, jstring fr_model, jint width, jint height) {
    jlong self = 0;
    try {
        string fd_model_str = jstring2string(env , fd_model);
        string fr_model_str = jstring2string(env,fr_model);

        self = (jlong) new FaceDetectorYNCreated(fd_model_str, fr_model_str, width, height);
    }catch (...){
        rethrow_cpp_exception_as_java_exception(env);
    }
    return self;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_livenativerppg_models_faceDetectYNCreated_ui_FeceDetectorYNCreatedActivity_FaceDetectAndSelectFrame(
        JNIEnv *env, jobject thiz, jlong self, jlong fram_addr) {
    try {
        ((FaceDetectorYNCreated *) self)->FindAndSelectFaceWithCorners(*(Mat *) fram_addr);
    }catch (...){
        rethrow_cpp_exception_as_java_exception(env);
    }
}



