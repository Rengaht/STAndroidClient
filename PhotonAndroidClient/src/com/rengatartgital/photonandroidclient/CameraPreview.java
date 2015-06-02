package com.rengatartgital.photonandroidclient;

import java.io.IOException;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    
	private final String TAG="STLog";
	
	private SurfaceHolder mHolder;
    private Camera mCamera;
    private Size optimal_size;
    private Rect frame_size;
    
    public CameraPreview(Context context, Camera camera,Size set_optimal,Rect set_frame){
        super(context);
        mCamera = camera;
        optimal_size=set_optimal;
        frame_size=set_frame;
        
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
    	
    	Log.i(TAG, "Camera surface created!");
    	
    	if(mCamera==null){
    		Log.i(TAG, "Surface without camera");
    		return;
    	}
        try {
        	
        	mCamera.stopPreview();
        	
        	mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    	
    	 Log.i(TAG, "Camera surface destroyed!");
    	 //this.getHolder().removeCallback(this);
    	 //mCamera.stopPreview();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
             mCamera.stopPreview();
            
             Camera.Parameters parameters=mCamera.getParameters();
			 Size s = parameters.getPreviewSize();
			 double w = s.width;
			 double h = s.height;
			 Log.i(TAG,"optimal size: "+optimal_size.width+" , "+optimal_size.height);
			 Log.i(TAG,"camera size: "+w+" , "+h+" picture size: "+parameters.getPictureSize().width+" , "+parameters.getPictureSize().height);
			 Log.i(TAG,"surface size: "+frame_size.width()+" "+frame_size.height());

			 //this.setLayoutParams(new FrameLayout.LayoutParams((int)optimal_size.width,(int)optimal_size.height));
			 float layout_ratio=(float)frame_size.height()/(float)frame_size.width();
			 float preview_ratio=(float)optimal_size.width/(float)optimal_size.height;
			 
			 if(layout_ratio>preview_ratio){
				 this.setLayoutParams(new FrameLayout.LayoutParams((int)(frame_size.height()/preview_ratio),frame_size.height()));		 
			 }else{			    
			    this.setLayoutParams(new FrameLayout.LayoutParams(frame_size.width(),(int)(frame_size.width()*preview_ratio)));
			 }
			 //this.setLayoutParams(new FrameLayout.LayoutParams((int)w,(int)h));		 
				
			 Log.i(TAG,"surface size: "+this.getWidth()+" "+this.getHeight());
			 
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.i(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}