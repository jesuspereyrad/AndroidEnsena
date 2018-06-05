package com.example.jesuspereyra.test.dynamic;

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
import com.example.jesuspereyra.test.SendToServerPieces;
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
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// OpenCV Classes

public class DynamicActivity extends AppCompatActivity implements CvCameraViewListener2 {

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
    Rect SQ1;
    Rect SQ2;
    Rect SQ3;
    Rect SQ4;
    Rect SQ5;
    Rect SQ6;
    Rect SQ7;
    Rect SQ8;
    Rect SQ9;
    Rect SQ10;
    String IP_ADDRESS = "10.0.0.6";
    Integer[][][] result = new Integer[10][2][30];

    Integer framecounter = 0;
    Integer framecounter2 = 0;
    SendToServer server;
    SendToServer server2;
    SendToServerRGB serverRGB;
    SendToServerPieces serverPieces;
    Rect roi;
    Rect chunks;
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

    public DynamicActivity() {
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

        textToSpeech = new TextToSpeech(DynamicActivity.this, new TextToSpeech.OnInitListener() {
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
                                                Toast.makeText(DynamicActivity.this, "Enviando al servidor", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        server2.execute(buffer, IP_ADDRESS, "5680", input.getText().toString());
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
        SQ1 = new Rect(width/2, height/3, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());
        SQ2 = new Rect(width/2 + 300, height/3, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());
        SQ3 = new Rect(width/2 - 300, height/3, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());
        SQ4 = new Rect(width/2, height/3 + 200, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());
        SQ5 = new Rect(width/2, height/3 - 200, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());
        SQ6 = new Rect(width/2 , 2 * height/3, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());
        SQ7 = new Rect(width/2 + 300, 2 * height/3, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());
        SQ8 = new Rect(width/2  - 300, 2 * height/3, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());
        SQ9 = new Rect(width/2 , 2 * height/3 + 200, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());
        SQ10 = new Rect(width/2 , 2 * height/3 - 200, new Double(width * 0.03).intValue(),
                new Double(height * 0.03).intValue());

        WB = Mat.zeros(height, width, CvType.channels(1));
        roi = new Rect(width/2 - 200, height/2 - 200, 400, 400);
        chunks = new Rect(0,0, width/10, height/10);
    }

    public int colorRangeMin(int media, int deviation) {
        return media - deviation;
    }

    public int colorRangeMax(int media, int deviation) {
        return media + deviation;
    }

    public double Mean(Mat mat) {
        double total = 0;
        Scalar sum = Core.sumElems(mat);
//        for(int a = 0; a < mat.cols() - 1; a++) {
//            for(int b = 0; b < mat.rows() - 1; b++) {
//                total += mat.get(b, a)[0];
//            }
//        }
        return sum.val[0] /(mat.cols() * mat.rows());
    }

    public double StandarDeviation(Mat mat, double mean) {
        int total = 0;
        double temp = 0;
        for(int a = 0; a < mat.cols() - 1; a++) {
            for(int b = 0; b < mat.rows() - 1; b++) {
                Double c = mat.get(b, a)[0];
                temp += (c - mean) * (c - mean);
            }
        }
        return Math.sqrt(temp/(mat.cols() * mat.rows()));
    }

    public Integer SumDoubleToInt(Double a, Double b) {
        Double total = a + b;
        return total.intValue();
    }

    public Integer SubDoubleToInt(Double a, Double b) {
        Double total = a - b;
        return total.intValue();
    }

    public Scalar SumOfElement(ArrayList<Integer> array) {
        int c = 0;
        for(int a :array) {
            c += a;
        }
        return new Scalar(c);
    }


    public Mat SumColors(Mat image, Integer[][][] result) {
        Mat bi = new Mat();
        Mat aux = new Mat();
        Mat dest = new Mat();

        Scalar colorRangeMinTotal = new Scalar(result[0][0][0], result[0][0][1], result[0][0][2]);
        Scalar colorRangeMaxTotal = new Scalar(result[0][1][0], result[0][1][1], result[0][1][2]);

        Core.inRange(image, colorRangeMinTotal, colorRangeMaxTotal, aux);
        aux.copyTo(bi);

        for(int i = 1; i < result.length; i+=1) {
            for(int j = 0; j < 15; j+=3){
                Core.inRange(image,
                        new Scalar(result[i][0][j], result[i][0][j+1], result[i][0][j+2]),
                        new Scalar(result[i][1][j], result[i][1][j+1], result[i][1][j+2]),
                        aux);

                Core.add(bi, aux, bi);
            }
        }


        return bi;
    }

    /*
    Traking is a function to track hand and head
    of a person in base of colors.

    Params: Mat image
    Return: Mat out
     */
    public Integer[][] tracking(Mat image) {

        List<Mat> sub = new ArrayList<Mat>();
        List<Mat> buffer = new ArrayList<Mat>(3);

        ArrayList<ArrayList<Integer>> colorRangeMin = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> colorRangeMax = new ArrayList<ArrayList<Integer>>();


//      TODO: Image from rgba to media blur to hsv

        ArrayList<Rect> rects = new ArrayList<Rect>();
        rects.add(SQ1);
        rects.add(SQ2);
        rects.add(SQ3);
        rects.add(SQ4);
        rects.add(SQ5);
        rects.add(SQ6);
        rects.add(SQ7);
        rects.add(SQ8);
        rects.add(SQ9);
        rects.add(SQ10);


//        TODO: For loop for looking the media and standard deviation x = 15;
        Integer[] colorRangeMaxList = new Integer[30];
        Integer[] colorRangeMinList = new Integer[30];
        for (int x = 0; x < rects.size(); x++) {

            Mat roi = image.submat(rects.get(x));
            Core.split(roi,buffer);

            Double[] meanColor = new Double[3];
            meanColor[0] = Mean(buffer.get(0));
            meanColor[1] = Mean(buffer.get(1));
            meanColor[2] = Mean(buffer.get(2));

            Double[] deviation = new Double[3];
            deviation[0] = StandarDeviation(buffer.get(0), meanColor[0]);
            deviation[1] = StandarDeviation(buffer.get(1), meanColor[1]);
            deviation[2] = StandarDeviation(buffer.get(2), meanColor[2]);


            colorRangeMaxList[x * 3] = SumDoubleToInt(meanColor[0], deviation[0]);
            colorRangeMaxList[(x * 3) + 1] = SumDoubleToInt(meanColor[1], deviation[1]);
            colorRangeMaxList[(x * 3) + 2] = SumDoubleToInt(meanColor[2], deviation[2]);


            colorRangeMinList[x * 3] = SubDoubleToInt(meanColor[0], deviation[0]);
            colorRangeMinList[(x * 3) + 1] = SubDoubleToInt(meanColor[1], deviation[1]);
            colorRangeMinList[(x * 3) + 2] = SubDoubleToInt(meanColor[2], deviation[2]);


            //  TODO: save the colorRangeMin and colorRangeMax in an array

        }
//        TODO: Add all the elements from the colorRangeMin and colorRangeMax;
//        Scalar colorRangeMinTotal = SumOfElement(colorRangeMax);
//        Scalar colorRangeMaxTotal = SumOfElement(colorRangeMin);

//        TODO: make a mask to take the colors: inRange(image, colorRangeMinTotal, colorRangeMaxTotal)
//        Core.inRange(image, colorRangeMinTotal, colorRangeMaxTotal, image);

//        TODO: return the mask
        Integer[][] test = new Integer[2][3 * rects.size()];
        test[0] = colorRangeMinList;
        test[1] = colorRangeMaxList;
        return test;
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = (inputFrame.rgba());

        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGBA2RGB);
        Imgproc.medianBlur(mRgba,mRgba, 5);
        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGB2HSV);


        if(framecounter2 < 10){
            result[framecounter2] = tracking(mRgba);
            framecounter2++;
        }
        else{
            return SumColors(mRgba, result);
        }

//
        Imgproc.rectangle(mRgba, new Point(SQ1.x, SQ1.y), new Point(SQ1.width + SQ1.x,SQ1.height + SQ1.y), new Scalar(0,100,0));
        Imgproc.rectangle(mRgba, new Point(SQ2.x, SQ2.y), new Point(SQ2.width + SQ2.x,SQ2.height + SQ2.y), new Scalar(0,100,0));
        Imgproc.rectangle(mRgba, new Point(SQ3.x, SQ3.y), new Point(SQ3.width + SQ3.x,SQ3.height + SQ3.y), new Scalar(0,100,0));
        Imgproc.rectangle(mRgba, new Point(SQ4.x, SQ4.y), new Point(SQ4.width + SQ4.x,SQ4.height + SQ4.y), new Scalar(0,100,0));
        Imgproc.rectangle(mRgba, new Point(SQ5.x, SQ5.y), new Point(SQ5.width + SQ5.x,SQ5.height + SQ5.y), new Scalar(0,100,0));
        Imgproc.rectangle(mRgba, new Point(SQ6.x, SQ6.y), new Point(SQ6.width + SQ6.x,SQ6.height + SQ6.y), new Scalar(0,100,0));
        Imgproc.rectangle(mRgba, new Point(SQ7.x, SQ7.y), new Point(SQ7.width + SQ7.x,SQ7.height + SQ7.y), new Scalar(0,100,0));
        Imgproc.rectangle(mRgba, new Point(SQ8.x, SQ8.y), new Point(SQ8.width + SQ8.x,SQ8.height + SQ8.y), new Scalar(0,100,0));
        Imgproc.rectangle(mRgba, new Point(SQ9.x, SQ9.y), new Point(SQ9.width + SQ9.x,SQ9.height + SQ9.y), new Scalar(0,100,0));
        Imgproc.rectangle(mRgba, new Point(SQ10.x, SQ10.y), new Point(SQ10.width + SQ10.x,SQ10.height + SQ10.y), new Scalar(0,100,0));
        framecounter++;
        return mRgba; // This function must return
    }




//
//    public Boolean DetectBlurImage() {
//
//    }
//
//    /*
//    Traking is a function to track hand and head
//    of a person in base of colors.
//
//    Params: Mat image
//    Return: Mat out
//     */
//    public Mat filter() {
//
//    }

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
                        Toast.makeText(DynamicActivity.this, results, Toast.LENGTH_SHORT).show();
                        textToSpeech.speak(results, TextToSpeech.QUEUE_FLUSH, null);
//                           alert(results, buffer);
                        stop();
                    }
                } else {
                    Toast.makeText(DynamicActivity.this, "Bad", Toast.LENGTH_SHORT).show();
                    textToSpeech.speak("MAL", TextToSpeech.QUEUE_FLUSH, null);
                    stop();
                }
            }
        });
        server.execute(buffer, IP_ADDRESS, "5680");
    }

    public void sendToServerRGB(Mat rgb) {
        List<Mat> buffer = new ArrayList<Mat>(3);
        Core.split(rgb, buffer);
        serverPieces = new SendToServerPieces(new ResultCallback() {
            @Override
            public void success(String results) {
                if(results != null) {
                    Toast.makeText(DynamicActivity.this, results, Toast.LENGTH_SHORT).show();
                    stop();
                } else {
                    Toast.makeText(DynamicActivity.this, "Bad", Toast.LENGTH_SHORT).show();
                    stop();
                }
            }
        });
        serverPieces.execute(convertBitmap(buffer.get(0)), convertBitmap(buffer.get(1)), convertBitmap(buffer.get(2)), IP_ADDRESS, "5680");
    }

    public void sendToServerChunks(Mat rgb) {
        List<Mat> buffer = new ArrayList<Mat>(3);
        int width = chunks.width;
        int height = chunks.height;
        for (int y = 0; y < 5; y++) {

            chunks = new Rect(y * width, y * height, width, height);
            buffer.add(rgb.submat(chunks));
        }
//        Core.split(rgb, buffer);
        serverRGB = new SendToServerRGB(new ResultCallback() {
            @Override
            public void success(String results) {
                if(results != null) {
                    Toast.makeText(DynamicActivity.this, results, Toast.LENGTH_SHORT).show();
                    stop();
                } else {
                    Toast.makeText(DynamicActivity.this, "Bad", Toast.LENGTH_SHORT).show();
                    stop();
                }
            }
        });
        serverRGB.execute(convertBitmap(buffer.get(0)), convertBitmap(buffer.get(1)),
                convertBitmap(buffer.get(2)),convertBitmap(buffer.get(3)), convertBitmap(buffer.get(4)) , IP_ADDRESS, "5680");
    }

    public String convertBitmap(Mat gray) {
        ROI4 = gray.submat(roi);
        Bitmap bm = Bitmap.createBitmap(ROI4.cols(), ROI4.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(ROI4, bm);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80 , stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

}