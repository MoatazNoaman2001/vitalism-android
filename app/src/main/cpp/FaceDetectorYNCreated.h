//
// Created by Mo3taz kayad on 2/14/2023.
//
#ifndef LIVENATIVERPPG_FACEDETECTORYNCREATED_H
#define LIVENATIVERPPG_FACEDETECTORYNCREATED_H

#include <opencv2/dnn.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/objdetect.hpp>
#include <iostream>
#include <jni.h>

using namespace std;
using namespace cv;

class FaceDetectorYNCreated{

public:
    FaceDetectorYNCreated(const string& fd_model ,string fr_model , int width , int height);
    void FindAndSelectFaceWithCorners(const Mat& frame);

private:
    TickMeter tm;
    float score_threshold = 0.9;
    float nmsThreshold = 0.3;
    int topK = 5000;
    float scale = 1;
    int frameHeight;
    int frameWidth;
    int nFrames = 0;
    Ptr<FaceDetectorYN> detector;
    double cosine_similar_thresh = 0.363;
    double l2norm_similar_thresh = 1.128;
};



#endif //LIVENATIVERPPG_FACEDETECTORYNCREATED_H
