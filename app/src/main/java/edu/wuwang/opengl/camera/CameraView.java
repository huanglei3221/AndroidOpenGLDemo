/*
 *
 * CameraView.java
 * 
 * Created by Wuwang on 2016/11/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package edu.wuwang.opengl.camera;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Description:
 */
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private String TAG = "CameraView HAHAHA";

    private KitkatCamera mCamera2;
    private CameraDrawer mCameraDrawer;

    // 目前卓宇板子的cameraid是0
    private int cameraId=0;
//    private int cameraId=1;

    private Runnable mRunnable;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mCamera2=new KitkatCamera();
        mCameraDrawer=new CameraDrawer(getResources());

        int numberOfCameras = Camera.getNumberOfCameras();

        // 测试一下摄像头的cameraid， 找最大的那个
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            String cameraFacing = info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? "Front" : "Back";
            Log.d(TAG, "Camera ID: " + i + ", Camera Facing: " + cameraFacing);
            cameraId = i;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraDrawer.onSurfaceCreated(gl,config);
        if(mRunnable!=null){
            mRunnable.run();
            mRunnable=null;
        }
        mCamera2.open(cameraId);
        mCameraDrawer.setCameraId(cameraId);
        Point point=mCamera2.getPreviewSize();
        mCameraDrawer.setDataSize(point.x,point.y);
        mCamera2.setPreviewTexture(mCameraDrawer.getSurfaceTexture());
        mCameraDrawer.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });
        mCamera2.preview();
    }

    public void switchCamera(){
        mRunnable=new Runnable() {
            @Override
            public void run() {
                mCamera2.close();
                cameraId=cameraId==1?0:1;
            }
        };
        onPause();
        onResume();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraDrawer.setViewSize(width,height);
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraDrawer.onDrawFrame(gl);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera2.close();
    }
}
