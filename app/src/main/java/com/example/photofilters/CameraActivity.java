package com.example.photofilters;

import android.hardware.Camera;
import android.view.SurfaceView;

public class CameraActivity extends Home {

    private Camera mCamera;
    private SurfaceView preview;


    @Override
    protected void onPause() {
        super.onPause();
        // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }



    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
