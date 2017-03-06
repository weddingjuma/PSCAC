// camera.cpp
// Control various functions using camera.
// Executed by child process

#include <opencv2/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/bgsegm.hpp>
#include <iostream>

#include "BackgroundMask.h"
#include "Detectors.h"
#include "CamDef.h"

#include "../communication/SigDef.h"

using namespace cv; // openCV

int takeRoad(void)
{
    // Connect camera
    // VideoCapture vc(0);
    // vc.set(CV_CAP_PROP_FRAME_WIDTH, 640);
    // vc.set(CV_CAP_PROP_FRAME_HEIGHT, 480);

    VideoCapture vc( CamDef::sampleVideo ); // Load test video
    if (!vc.isOpened()) {
        std::cerr << "ERROR : Cannot open the camera" << std::endl;
        return false;
    }


    // Background recognition and removal
    BackgroundMask bgMask;
    bgMask.printProperties();
    bgMask.setRecognizeNumFrames(120);  // Default : 120 ( BackgroundSubtractorGMG's default value )
    bgMask.setAccumulateNumFrames(200); // Default : 200
    bgMask.setLearningRate(0.1); // Default : 0.025
    UMat mask = bgMask.createBackgroundMask(vc);


    UMat img, fgimg; // using OpenCL ( UMat )
    PedestriansDetector pe_Detector;
    VehiclesDetector car_Detector;


    std::cout << "Start Detection ..." << std::endl;
    while (1) {
        vc >> img; // Put the captured image in img
        if (img.empty())  {
            std::cerr << "ERROR : Unable to load frame" << std::endl;
            break;
        }

        bgMask.locateForeground(img, fgimg);

        // Detect pedestrians and vehicle
        pe_Detector.detect(fgimg);
        if( pe_Detector.isFound() ) {
            sendSignalToParentProcess(SigDef::SIG_FOUND_HUMAN);
        }
        car_Detector.detect(fgimg);
        if( car_Detector.isFound() ) {
            sendSignalToParentProcess(SigDef::SIG_FOUND_CAR);
        }


        imshow( CamDef::originalVideo, img );  // show original image
        imshow( CamDef::mask, mask );  // show background mask
        imshow( CamDef::resultVideo, fgimg );  // show image

        if( waitKey( CamDef::DELAY ) == CamDef::ESC ) {  // ESC(27) -> break
            std::cout << "Closing the program ..." << std::endl;
            break;
        }
    }

    vc.release();

    destroyAllWindows();

    return true;
}