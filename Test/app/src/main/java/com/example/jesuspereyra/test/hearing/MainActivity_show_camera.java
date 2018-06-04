package com.example.jesuspereyra.test.hearing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jesuspereyra.test.R;
import com.example.jesuspereyra.test.ResultCallback;
import com.example.jesuspereyra.test.SendToServer;
import com.example.jesuspereyra.test.SendToServerRGB;

import org.opencv.android.JavaCameraView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// OpenCV Classes

public class MainActivity_show_camera extends AppCompatActivity implements CvCameraViewListener2 {

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    Context context = this;

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degreeœ to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    Mat GRAY;
    Mat WB;
    Mat ROI;
    Mat ROI2;
    Mat ROI3;
    Mat ROI4;
    String IP_ADDRESS = "192.168.43.26";

    Integer framecounter = 0;
    SendToServer server;
    SendToServer server2;
    SendToServerRGB serverRGB;
    Rect roi;
    TextToSpeech textToSpeech;
    SpeechRecognizer speechRecognizer;
    Locale spanish = new Locale("es", "ES");
    int resultSpeech;

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

    public MainActivity_show_camera() {
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

        FloatingActionButton settingBtn = (FloatingActionButton) findViewById(R.id.settingBtn);

        textToSpeech = new TextToSpeech(MainActivity_show_camera.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    resultSpeech = textToSpeech.setLanguage(spanish);
                } else {
                    Toast.makeText(getApplicationContext(),  "Feature not supported in your device", Toast.LENGTH_LONG).show();
                }
            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingAlert();
            }
        });

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Set server IP address")
                .setTitle("Network Settings");

//        builder.set

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void alert(String results, String buff) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String buffer = buff;
        builder.setTitle("La sena correcta es " + results.toString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stop();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                        final EditText input = new EditText(context);
                        builder2.setView(input);
                        builder2.setTitle("What was the sign")
                                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        server2 = new SendToServer(new ResultCallback() {
                                            @Override
                                             public void success(String results) {
                                                Toast.makeText(MainActivity_show_camera.this, "Enviando al servidor", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        server2.execute(buffer, IP_ADDRESS, "5678", input.getText().toString());
                                        stop();
                                    }
                                });
                        builder2.create();
                        builder2.show();
                    }
                });
        builder.create();
        builder.show();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        stop();
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
        GRAY = Mat.zeros(height, width, CvType.channels(1));
        ROI = Mat.zeros(height, width, CvType.channels(1));
        ROI2 = Mat.zeros(height, width, CvType.channels(1));
        WB = Mat.zeros(height, width, CvType.channels(1));
        roi = new Rect(width/2 - 200, height/2 - 200, 400, 400);
    }

    public void stop() {
        if(server != null && !server.isCancelled()) {
            server.cancel(true);
        }
    }

    public void settingAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText input = new EditText(context);
        builder.setView(input);
        builder.setMessage("IP ADDRESS")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(!input.getText().toString().isEmpty()) {
                    IP_ADDRESS = input.getText().toString();
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.create();
        builder.show();
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }


    public void sendToServer(String buffer) {
        server = new SendToServer(new ResultCallback() {
            @Override
            public void success(String results) {
                if(results != null) {
                    if(!results.toLowerCase().matches("no hay mano")) {
                        Toast.makeText(MainActivity_show_camera.this, results, Toast.LENGTH_SHORT).show();
                        textToSpeech.speak(results, TextToSpeech.QUEUE_FLUSH, null);
//                           alert(results, buffer);
                        stop();
                    }
                } else {
                    Toast.makeText(MainActivity_show_camera.this, "Bad", Toast.LENGTH_SHORT).show();
                    textToSpeech.speak("MAL", TextToSpeech.QUEUE_FLUSH, null);
                    stop();
                }
            }
        });
        server.execute(buffer, IP_ADDRESS, "5679");
    }

    public void sendToServerRGB(Mat rgb) {
        List<Mat> buffer = new ArrayList<Mat>(3);
        Core.split(rgb, buffer);
        serverRGB = new SendToServerRGB(new ResultCallback() {
            @Override
            public void success(String results) {
                if(results != null) {
                    Toast.makeText(MainActivity_show_camera.this, results, Toast.LENGTH_SHORT).show();
                    stop();
                } else {
                    Toast.makeText(MainActivity_show_camera.this, "Bad", Toast.LENGTH_SHORT).show();
                    stop();
                }
            }
        });
        serverRGB.execute(convertBitmap(buffer.get(0)), convertBitmap(buffer.get(1)), convertBitmap(buffer.get(2)), IP_ADDRESS, "5675");
    }

    public String convertBitmap(Mat gray) {
        ROI4 = gray.submat(roi);
        Bitmap bm = Bitmap.createBitmap(ROI4.cols(), ROI4.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(ROI4, bm);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80 , stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = (inputFrame.rgba());
        Imgproc.cvtColor(mRgba, GRAY, Imgproc.COLOR_RGBA2GRAY);

//        Every 10 frames enter to the condition and save the image in the android device

        if ((server == null || server.isCancelled()) && (framecounter % 25 == 0)) {
//            sendToServerRGB(mRgba);
            sendToServer(convertBitmap(GRAY));

        }
        framecounter++;
        return mRgba; // This function must return
    }
}