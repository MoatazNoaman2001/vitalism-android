//
// Created by Mo3taz kayad on 2/14/2023.
//

#include "FaceDetectorYNCreated.h"


void visualize(Mat input, int frame, Mat &faces, double fps, int thickness = 2)
{
    std::string fpsString = cv::format("FPS : %.2f", (float)fps);
    if (frame >= 0)
    for (int i = 0; i < faces.rows; i++)
    {
        // Draw bounding box
        rectangle(input, Rect2i(int(faces.at<float>(i, 0)), int(faces.at<float>(i, 1)), int(faces.at<float>(i, 2)), int(faces.at<float>(i, 3))), Scalar(0, 255, 0), thickness);
        // Draw landmarks
        circle(input, Point2i(int(faces.at<float>(i, 4)), int(faces.at<float>(i, 5))), 12, Scalar(255, 0, 0), thickness);
        circle(input, Point2i(int(faces.at<float>(i, 6)), int(faces.at<float>(i, 7))), 12, Scalar(0, 0, 255), thickness);
        circle(input, Point2i(int(faces.at<float>(i, 8)), int(faces.at<float>(i, 9))), 12, Scalar(0, 255, 0), thickness);
        circle(input, Point2i(int(faces.at<float>(i, 10)), int(faces.at<float>(i, 11))), 12, Scalar(255, 0, 255), thickness);
        circle(input, Point2i(int(faces.at<float>(i, 12)), int(faces.at<float>(i, 13))), 12, Scalar(0, 255, 255), thickness);
    }
    putText(input, fpsString, Point(0, 15), FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0, 255, 0), 2);
}




FaceDetectorYNCreated::FaceDetectorYNCreated(const string& fd_model ,string fr_model , int width , int height){

    // Initialize FaceDetectorYNCreated
    detector = FaceDetectorYN::create(fd_model, "", Size(320, 320), score_threshold, nmsThreshold, topK);

    this->frameHeight = height * scale;
    this->frameWidth = width * scale;

    detector->setInputSize(Size(this->frameWidth, this->frameHeight));
}


void FaceDetectorYNCreated::FindAndSelectFaceWithCorners(const Mat& frame){
    Mat faces;
    resize(frame , frame ,Size( frameWidth , frameHeight));

    tm.start();
    detector->detect(frame , faces);
    tm.stop();

    if(faces.empty())
        return;
    visualize(frame, nFrames, faces, tm.getFPS());
    nFrames++;
}