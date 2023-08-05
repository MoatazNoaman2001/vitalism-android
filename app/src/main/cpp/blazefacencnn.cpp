////
//// Created by Mo3taz kayad on 3/20/2023.
////
//
//
//
//#include "face.h"
//#include <opencv2/core.hpp>
//#include <opencv2/imgproc.hpp>
//#include <opencv2/imgcodecs.hpp>
//#include <opencv2/gapi/video.hpp>
//
//static Face* g_blazeface = 0;
//static ncnn::Mutex lock;
//
//static int draw_unsupported(cv::Mat& rgb)
//{
//    const char text[] = "unsupported";
//
//    int baseLine = 0;
//    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 1.0, 1, &baseLine);
//
//    int y = (rgb.rows - label_size.height) / 2;
//    int x = (rgb.cols - label_size.width) / 2;
//
//    cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
//                  cv::Scalar(255, 255, 255), -1);
//
//    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
//                cv::FONT_HERSHEY_SIMPLEX, 1.0, cv::Scalar(0, 0, 0));
//
//    return 0;
//}
//
//void init(){
//    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel");
//    const char* modeltypes[] =
//            {
//                    "blazeface",
//                    "blazeface",
//                    "blazeface"
//            };
//    const int target_sizes[] =
//            {
//                    192,
//                    320,
//                    640
//            };
//
//    const char* modeltype = modeltypes[1];
//    int target_size = target_sizes[1];
//    bool use_gpu = false;
//
//    // reload
//    {
//        ncnn::MutexLockGuard g(lock);
//
//        if (use_gpu && ncnn::get_gpu_count() == 0)
//        {
//            // no gpu
//            delete g_blazeface;
//            g_blazeface = 0;
//        }
//        else
//        {
//            if (!g_blazeface)
//                g_blazeface = new Face;
////            g_blazeface->load(mgr, modeltype,target_size, use_gpu);
//        }
//    }
//}
//void on_image_render(cv::Mat& rgb)
//{
//    {
//        ncnn::MutexLockGuard g(lock);
//        if (g_blazeface)
//        {
//            std::vector<Object> faceobjects;
//            g_blazeface->detect(rgb, faceobjects);
//
//            g_blazeface->draw(rgb, faceobjects);
//        }
//        else
//        {
//            draw_unsupported(rgb);
//        }
//    }
//}
//
