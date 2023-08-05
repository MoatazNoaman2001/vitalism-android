# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("livenativerppg")

#project path
set(PROJECT_PATH D:\\collage\\LiveNativeRPPG)

# Configure import libs: MAKE SURE TO HAVE A 'cppLibs' DIRECTORY"
set(LIB_DIR ${PROJECT_PATH}/app/src/main/cppLibs)

set(OpenCV_STATIC on)
set(OpenCV_DIR $ENV{OPENCV_ANDROID}/sdk/native/jni)
find_package(OpenCV REQUIRED)


set(ncnn_DIR ${LIB_DIR}/ncnn/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)


set(DLIB_PATH ${LIB_DIR}/dlib)
add_library(dlib SHARED IMPORTED)

# sets the location of the prebuilt dlib .so
set_target_properties( dlib
        PROPERTIES IMPORTED_LOCATION
        ${DLIB_PATH}/lib/${ANDROID_ABI}/libdlib.so )

include_directories(${DLIB_PATH}/include)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


add_library( # Sets the name of the library.
        livenativerppg

        # Sets the library as a shared library.
        SHARED

        FaceDetectorYNCreated.h
        FaceDetectorYNCreated.cpp

        RPPG.cpp
        RPPG.h

        opencv.cpp
        opencv.h
        # Provides a relative path to your source file(s).
        native-lib.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)
find_library(jnigraphics-lib jnigraphics)
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        livenativerppg

        # Links the target library to the log library
        # included in the NDK.4
        ${OpenCV_LIBS}
        ${jnigraphics-lib}
        ${dlib}
        ${log-lib})


add_library(blazefacencnn SHARED

        blazefacencnn.cpp)

target_link_libraries(blazefacencnn ncnn ${OpenCV_LIBS})