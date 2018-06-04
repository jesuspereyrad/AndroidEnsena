package com.example.jesuspereyra.test.onBoarding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.example.jesuspereyra.test.R;
import com.example.jesuspereyra.test.SendToServer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by jesuspereyra on 9/18/17.
 */

public class OnBoarding extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degreeœ to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    Integer framecounter = 0;
    SendToServer server;
    Rect roi;
    Rect circle;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public OnBoarding() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title not the title bar

        setContentView(R.layout.show_camera);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
        roi = new Rect(width/2 - 350, height/2 - 350, 700, 700);
        circle = new Rect(width/2, height/2, 700, 700);
    }

    public void stop() {
        if(server != null && !server.isCancelled()) {
            server.cancel(true);
        }
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = (inputFrame.rgba());

        Mat ROI = mRgba.clone();
//        Imgproc.Canny(mRgba, ROI, 100, 255);
        Imgproc.rectangle(ROI, new Point(roi.x, roi.y), new Point(roi.x + roi.width, roi.y + roi.height), new Scalar(255), 3, 8, 0);
        Imgproc.circle(ROI, new Point(circle.x + 200, circle.y), 15, new Scalar(255));
        Imgproc.circle(ROI, new Point(circle.x - 200, circle.y), 15, new Scalar(255));
        Imgproc.circle(ROI, new Point(circle.x - 150, circle.y + 75), 15, new Scalar(255));
        Imgproc.circle(ROI, new Point(circle.x - 150, circle.y - 75), 15, new Scalar(255));
        Imgproc.circle(ROI, new Point(circle.x, circle.y), 15, new Scalar(255));
        Imgproc.circle(ROI, new Point(circle.x, circle.y + 100), 15, new Scalar(255));
        Imgproc.circle(ROI, new Point(circle.x, circle.y - 100), 15, new Scalar(255));

        return ROI; // This function must return
    }
}
