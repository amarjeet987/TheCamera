package com.example.amarjeet.camera2api;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import static android.content.Context.CAMERA_SERVICE;

public class MainActivity extends AppCompatActivity {
    private static final int SETUP_PERMISSIONS = 1;
    private static final String TAG = "MainActivity";
    private CameraManager cameraManager;
    private Size previewSize;
    private String mCameraIDFront, mCameraIDBack;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CameraDevice mcameraDevice;
    private TextureView textureView;
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureReqBuilder;
    private CameraCaptureSession mCameraCapSession;
    private DisplayMetrics screenSize;
    private int height, width;
    private StreamConfigurationMap map;
    private File galleryFolder;
    private int brightVal;
    private SeekBar brightness;
    private CameraCharacteristics cameraCharacteristics;
    private Handler handler;
    private Thread editCamera;
    private Range<Integer> range;
    private ImageView video_off;
    private LinearLayout recordClock;
    private TextView recordClockText;
    private Runnable timer;
    private Handler setText ;
    private Thread timerThread;
    private int a,b,c,d;
    private CountDownTimer t;
    private boolean settings_open = false;
    private TextView changeSettings, brightness_text;
    private MediaRecorder mMediaRecorder;
    private String mNextVideoAbsolutePath;
    private Size videoSize;
    private VideoView videoView;
    private ArrayList<String> imgUrls;
    private boolean Recording_state = false;
    private String var_cameraID;
    private ImageView flash_toggle, camera_toggle;
    private Surface previewSurface;
    private boolean flash_available_front, flash_available_back;
    private String flashState;
    private ImageReader mImgReader;
    private FileOutputStream outfile;
    private String dir;
    private String imgLoc, vidLoc;
    private Thread mThread;
    private boolean ImgVisible = false, VidVisible = false;
    private ImageView mImgView;
    private RelativeLayout mRelativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize stuffs
        textureView = (TextureView)findViewById(R.id.textureView);
        ImageView capture;

        //imageview
        mImgView = (ImageView)findViewById(R.id.img_view);

        //videoView
        videoView = (VideoView)findViewById(R.id.videoView);
        mRelativeLayout = (RelativeLayout)findViewById(R.id.rellayout);

        //set fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //request permissions
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, SETUP_PERMISSIONS);

        //transition
        if(Build.VERSION.SDK_INT > 21) {
            Fade fade = new Fade();
            fade.setDuration(1000);
            getWindow().setExitTransition(fade);

        }

        //setup camera service
        cameraManager = (CameraManager)getSystemService(CAMERA_SERVICE);

        //recyclerview
        initRecyclerView();

        capture = (ImageView)findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Onclick capture");
                //create an output file stream
                outfile = null;
                try {
                    Log.d(TAG, "Inside try outfile");
                    //create the image file and put it in the output stream
                    outfile = new FileOutputStream(createImageFile(galleryFolder));

                    CameraCaptureSession.CaptureCallback mCallback = new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            Log.d(TAG, "Capture successful");
                            super.onCaptureCompleted(session, request, result);
                        }

                        @Override
                        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                            Log.d(TAG, "Capture Failed");
                            super.onCaptureFailed(session, request, failure);
                        }
                    };

                    captureReqBuilder.addTarget(mImgReader.getSurface());

                    //Callback after photo is taken
                    mCameraCapSession.capture(captureReqBuilder.build(), mCallback, mBackgroundHandler);
                    mImgReader.setOnImageAvailableListener(onImgAvailableListener,mBackgroundHandler);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        //settings menu
        changeSettings = (TextView)findViewById(R.id.change_settings);
        brightness_text = (TextView)findViewById(R.id.text_brightness);

        changeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(settings_open) {
                    changeSettings.setText("Change Settings");
                    brightness.setVisibility(View.GONE);
                    brightness_text.setVisibility(View.GONE);
                    settings_open = false;
                }
                else
                {
                    changeSettings.setText("Done");
                    brightness.setVisibility(View.VISIBLE);
                    brightness_text.setVisibility(View.VISIBLE);
                    settings_open = true;
                }

            }
        });

        //for recording
        mMediaRecorder = new MediaRecorder();

        video_off = (ImageView)findViewById(R.id.record_off);

        videoView = (VideoView)findViewById(R.id.videoView);

        a=0; b=0; c=0; d=0;

        recordClock = (LinearLayout)findViewById(R.id.record_clock);
        recordClockText = (TextView) findViewById(R.id.record_clock_text);


        video_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Recording_state == false) {
                    Recording_state = true;
                    recordClock.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Video_OFF");
                    startTimer();
                    startRecordingVideo();
                    video_off.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_stop));
                } else {
                    Recording_state = false;
                    stopTimer();
                    video_off.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_off));
                    stopRecordingVideo();

                }
            }
        });

        //initialize the seekbar
        brightness = (SeekBar)findViewById(R.id.seekbar_brightness);

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brightVal = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                controlBrightness(brightVal);
            }
        });

        //shift camera
        var_cameraID = "1";
        camera_toggle = (ImageView)findViewById(R.id.rotate);
        camera_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "CameraIDcurrent" + var_cameraID);

                if(var_cameraID.equals("0")) {

                    Log.d(TAG, "CameraCharacteristics.LENS_FACING_BACK");
                    closeCamera();
                    openBackCamera();

                }else if(var_cameraID.equals("1")) {

                    Log.d(TAG, "CameraCharacteristics.LENS_FACING_FRONT");
                    closeCamera();
                    openFrontCamera();
                }
            }
        });

        //flash
        flash_toggle = (ImageView)findViewById(R.id.flash);
        flash_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraForFlash();
            }
        });

    }

    private void checkCameraForFlash() {
        if(var_cameraID.equals("0")) {
            setFlash(flash_available_front);
        } else if(var_cameraID.equals("1")) {
            setFlash(flash_available_back);
        }
    }

    private void setFlash(Boolean flash_status) {
        if(!flash_status) {
            return;
        }
        if(flashState.equals("Auto")) {
            Log.d(TAG,"On");
            flashState = "On";
            flash_toggle.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on));
            captureReqBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

        } else if(flashState.equals("On")) {
            Log.d(TAG,"Off");
            flashState = "Off";
            flash_toggle.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
            captureReqBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
        } else if(flashState.equals("Off")) {
            Log.d(TAG,"Auto");
            flashState = "Auto";
            flash_toggle.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_auto));
            captureReqBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.FLASH_MODE_OFF);
        }
        closePreviewSession();
        startPreviewSession();
    }
    //start the thread
    private void initRecyclerView() {

        imgUrls = new ArrayList<>();

        final Handler loadRecyclerView = new Handler(Looper.getMainLooper()) {
            @Override
            public void dispatchMessage(Message msg) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/AppPictures" );
                Log.d(TAG, Arrays.asList(directory.list()).toString());

                if(directory.exists()) {

                    Log.d(TAG, "DIR:  " + directory);

                    if(directory.listFiles() == null) {
                        Log.d(TAG, "Listfiles null");
                    }


                    if(directory.listFiles().length > 0) {
                        for(int i = 0; i < directory.listFiles().length; i++) {
                            imgUrls.add(directory.listFiles()[i].getAbsolutePath());
                            Log.d(TAG, directory.listFiles()[i].getAbsolutePath());
                        }
                    }
                }

                LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL,false);
                RecyclerView mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
                mRecyclerView.setLayoutManager(mLinearLayoutManager);

                RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(imgUrls, MainActivity.this);
                mRecyclerView.setAdapter(mAdapter);
            }
        };

        Runnable r = new Runnable() {
            @Override
            public void run() {
                loadRecyclerView.sendEmptyMessage(1);
            }
        };

        mThread = new Thread(r);
        mThread.start();
        try {
            mThread.join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        setText = new Handler(Looper.getMainLooper()) {
            @Override
            public void dispatchMessage(Message msg) {
                t = new CountDownTimer(30000, 1000) {

                    @Override
                    public void onTick(long l) {
                        a++;
                        if(a==10) {
                            a=0;
                            b++;
                        }
                        Log.d(TAG, "a:" + a + ", b:" + b);
                        setText();

                    }

                    @Override
                    public void onFinish() {
                        stopTimer();
                    }
                }.start();

            }

            private void setText() {
                recordClockText.setText("" + c + d + ":" + b + a);
            }
        };

        timer = new Runnable() {
            @Override
            public void run() {
                setText.sendEmptyMessage(0);

            }
        };

        timerThread = new Thread(timer);
        timerThread.start();
    }

    private void stopTimer()
    {
        video_off.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_off));
        recordClock.setVisibility(View.INVISIBLE);
        a=0; b=0; c=0; d=0;
        Log.d(TAG, timerThread.isAlive() + "");
        Log.d(TAG, "Video_ON");
        try {
            timerThread.join();

            t.cancel();
            setText = null;
            timerThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void CreateImageGallery() {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Log.d(TAG,storageDirectory.getAbsolutePath());
        galleryFolder = new File(storageDirectory,"AppPictures");
        if(!galleryFolder.exists()) {
            Boolean createFolder = galleryFolder.mkdirs();
            if(!createFolder) {
                Log.d(TAG,"Failed to create directory");
            } else {
                Log.d(TAG,"Folder created");
            }
        } else {
            Log.d(TAG,"Folder already exists");
        }
    }

    private File createImageFile(File galleryFolder) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imagefilename = "IMG_" + timestamp + "_";
        File image = File.createTempFile(imagefilename,".png", galleryFolder);
        imgLoc = image.getAbsolutePath();
        Log.d(TAG, imgLoc);
        return image;
    }


    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            setupCamera();
            openBackCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        CreateImageGallery();
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
        closeBackgroundThread();
    }

    private void closeCamera() {
        if(mCameraCapSession != null) {
            mCameraCapSession.close();
            mCameraCapSession = null;
        }
        if(mcameraDevice != null) {
            mcameraDevice.close();
            mcameraDevice = null;
        }
    }

    private void closeBackgroundThread() {
        if(mBackgroundHandler != null) {
            mBackgroundThread.quitSafely();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CreateBackgroundThread();
        initRecyclerView();
        if(textureView.isAvailable()) {
            setupCamera();
            openBackCamera();
        }
        else
        {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    ImageReader.OnImageAvailableListener onImgAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "Image is available");
            closePreviewSession();
            Image mImage = reader.acquireNextImage();
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            Bitmap bm = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

            Matrix mat = new Matrix();
            if(var_cameraID.equals("0")) {
                mat.postRotate(90);
                mat.preScale(-1,1);
            } else if(var_cameraID.equals("1")) {
                mat.postRotate(90);
            }

            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),mat, true);

            try {
                bm.compress(Bitmap.CompressFormat.PNG, 90, outfile);
                outfile.close();
                Log.d(TAG,"Closing File");

            } catch (IOException e) {
                e.printStackTrace();
            }
            gotonextAct();
        }
    };

    public void gotonextAct() {
        Log.d(TAG,"Going to next activity");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(imgLoc!=null) {
                    Log.d(TAG, imgLoc);
                    textureView.animate().alpha(0).setDuration(100);
                    mImgView.setVisibility(View.VISIBLE);
                    mImgView.animate().alpha(1.0f).setDuration(1000);
                    mImgView.setImageBitmap(BitmapFactory.decodeFile(imgLoc));
                    ImgVisible = true;
                }

                /**
                Intent i = new Intent(MainActivity.this, SecActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this,null);
                i.putExtra("ImageUri", imgLoc);
                startActivity(i, optionsCompat.toBundle());
                 **/

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && ImgVisible) {
            Log.d(TAG, "Going back");
            startPreviewSession();
            textureView.animate().alpha(1.0f).setDuration(300);
            mImgView.animate().alpha(0f).setDuration(100);
            mImgView.setVisibility(View.INVISIBLE);
            ImgVisible = false;
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_BACK && VidVisible) {
            Log.d(TAG, "Going back");
            startPreviewSession();
            textureView.animate().alpha(1.0f).setDuration(300);
            mRelativeLayout.animate().alpha(0f).setDuration(100);
            mRelativeLayout.setVisibility(View.INVISIBLE);
            VidVisible = false;
            videoView.setVisibility(View.INVISIBLE);
            return true;
        } else {
            Log.d(TAG, "Exiting");
            return super.onKeyDown(keyCode, event);
        }
    }

    private void showVideo(String videoUri, final VideoView videoView) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d(TAG, metrics.heightPixels + "     " + metrics.widthPixels);
        android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) videoView.getLayoutParams();
        params.width =  metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        videoView.setLayoutParams(params);

        videoView.setVisibility(View.VISIBLE);
        try {
            videoView.setMediaController(null);
            videoView.setVideoURI(Uri.parse(videoUri));
        } catch (Exception e){
            e.printStackTrace();
        }
        videoView.requestFocus();
        //videoView.setZOrderOnTop(true);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {

                videoView.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });
    }

    private void setupCamera() {
        //Go through device's list of cameras and find the one having LENS_FACING_BACK
        try {

            //get a list of camera IDs
            for(String cameraID : cameraManager.getCameraIdList()) {

                //get the camera characteristic of each camera ID
                cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                range = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                brightness.setMax(2*range.getUpper());

                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    mCameraIDFront = cameraID;
                    flash_available_front = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Log.d(TAG, "Front Camera flash :" + flash_available_front);
                }

                //check for rear camera
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    flash_available_back = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Log.d(TAG, "Back Camera flash :" + flash_available_back);
                    //retrieve the cameraID
                    mCameraIDBack = cameraID;

                    //Now we retrieve the available screen sizes for our TextureView using StreamConfigurationMap
                    map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    //get the screen size
                    screenSize = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(screenSize);
                    height = screenSize.heightPixels;
                    width = screenSize.widthPixels;
                    Log.d(TAG, "Texture Width: " + width + "   Texture Height:" + height);

                    videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
                    previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), videoSize);

                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 16 / 9) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    private CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "openFrontCamera : inside onOpened ");
            mcameraDevice = cameraDevice;
            startPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mcameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mcameraDevice = null;
        }
    };

    private void openFrontCamera() {
        //Check for permissions
        if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //CameraIDFront : Front facing
            //stateCallback : In case the camera connects or fails
            // mBackgroundHandler : background thread to run the camera
            try {
                checkFlashAvailable(flash_available_front);
                cameraManager.openCamera(mCameraIDFront, stateCallBack, mBackgroundHandler);
                var_cameraID = mCameraIDBack;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "App requires camera permissions", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBackCamera() {
        //Check for permissions
        if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "openBackCamera : BackCameraID :" + mCameraIDBack);
            //CameraIDBack : Default facing
            //stateCallback : In case the camera connects or fails
            // mBackgroundHandler : background thread to run the camera
            try {
                var_cameraID = mCameraIDFront;
                checkFlashAvailable(flash_available_back);
                cameraManager.openCamera(mCameraIDBack, stateCallBack, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "App requires camera permissions", Toast.LENGTH_SHORT).show();
        }
    }

    //check if flash is available
    private void checkFlashAvailable(Boolean available) {
        if(available)
        {
            flash_toggle.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_auto));
            flashState = "Auto";
        } else {
            flash_toggle.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_name));
            flashState = "Off";
        }
    }

    //Method to create the background thread
    private void CreateBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera2API_handler_thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }


    private void startPreviewSession() {
        closePreviewSession();
        Log.d(TAG, "openFrontCamera : inside startPreviewSession ");
        //set the dimensions for the TextureView
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

        //create a surface to fit the TextureView for camera
        if(previewSurface!=null) {
            previewSurface = null;
        }
        previewSurface = new Surface(surfaceTexture);
        mImgReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG,2);


        //set up the capture request builder for mCameraDevice with default template and specify its target as the previewSurface
        try {
            if(captureReqBuilder!=null) {
                captureReqBuilder = null;
            }
            Log.d(TAG, "openFrontCamera : inside try ");
            captureReqBuilder = mcameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);


            //flash
            if(flashState.equals("Auto")) {
                Log.d(TAG,"flashState.equals(\"Auto\")");
                captureReqBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            } else if (flashState.equals("On")) {
                captureReqBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
            } else if(flashState.equals("Off")) {
                captureReqBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.FLASH_MODE_OFF);
            }
            //initial brightness
            captureReqBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, brightVal - range.getUpper());
            captureReqBuilder.addTarget(previewSurface);

            Log.d(TAG, "openFrontCamera : after add target ");
            //Create the captureSession

            Log.d(TAG, "Surface 1 : " +previewSurface.toString());
            Log.d(TAG, "Surface 2 : " +mImgReader.getSurface().toString());

            mcameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImgReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "openFrontCamera : inside createCaptureSession 1 ");
                    if(mcameraDevice == null) {
                        Log.d(TAG, "cameradev is null ");
                        return;
                    }
                    captureRequest = captureReqBuilder.build();
                    Log.d(TAG, "openFrontCamera : inside createCaptureSession 2");
                    mCameraCapSession = cameraCaptureSession;
                    try {
                        mCameraCapSession.setRepeatingRequest(captureRequest, null,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "Failed");
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static Size chooseOptimalSize(Size[] choices, Size aspectRatio) {
        Log.d(TAG, Arrays.toString(choices));
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w) {
                bigEnough.add(option);
                Log.d(TAG, option + "");
            }
        }


        Log.d(TAG, "bigEnough.size() :" + bigEnough.size());
        // Pick the smallest of those, assuming we found any
        Size result = choices[0];
        if (bigEnough.size() > 0) {
            result = Collections.max(bigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return Long.signum((long) o1.getHeight()*o1.getWidth() - (long) o2.getHeight()*o2.getWidth());
                }
            });
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
        }

        return result;
    }


    private void controlBrightness(final int change)
    {
        //handler for thread
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void dispatchMessage(Message msg) {
                startPreviewSession();
            }
        };

        //runnable for thread
        Runnable r = new Runnable() {
            @Override
            public void run() {
                captureReqBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, change - range.getUpper());
                handler.sendEmptyMessage(0);
            }
        };

        //initialize the thread
        editCamera = new Thread(r);
        //Start the thread
        editCamera.start();
    }

    private void startRecordingVideo() {
        if (null == mcameraDevice || !textureView.isAvailable() || null == previewSize) {
            Log.d(TAG, "Cannot continue");
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            captureReqBuilder = mcameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            captureReqBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            captureReqBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mcameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mCameraCapSession = cameraCaptureSession;
                    try {
                        mCameraCapSession.setRepeatingRequest(captureReqBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                    // Start recording
                    mMediaRecorder.start();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpMediaRecorder() {

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath();
            Log.d(TAG, mNextVideoAbsolutePath);
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.d(TAG, "IOException :" + e);
        }

        mMediaRecorder.setOnErrorListener(errorMedia);
    }

    MediaRecorder.OnErrorListener errorMedia = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.d(TAG, "Whaat : " + what + "   Extra :" + extra);
        }
    };

    private String getVideoFilePath() {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        Log.d(TAG,storageDirectory.getAbsolutePath());
        galleryFolder = new File(storageDirectory,"AppVideos");
        if(!galleryFolder.exists()) {
            Boolean createFolder = galleryFolder.mkdirs();
            if(!createFolder) {
                Log.d(TAG,"Failed to create directory");
            } else {
                Log.d(TAG,"Folder created");
            }
        } else {
            Log.d(TAG,"Folder already exists");
        }
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imagefilename = "VID_" + timestamp + "_";
        File image = null;
        try {
            image = File.createTempFile(imagefilename,".png", galleryFolder);
        } catch (IOException e) {
            Log.d(TAG, "IOExc :" +e);
        }
        vidLoc = image.getAbsolutePath();
        return vidLoc;
    }

    private void closePreviewSession() {
        if(mCameraCapSession!=null) {
            mCameraCapSession.close();
            mCameraCapSession = null;
            captureReqBuilder = null;
        }
    }

    private void stopRecordingVideo() {
        // Stop recording
        mMediaRecorder.reset();
        Log.d(TAG, vidLoc);
        textureView.animate().alpha(0f).setDuration(100);
        mRelativeLayout.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.VISIBLE);
        mRelativeLayout.animate().alpha(1.0f).setDuration(1000);
        showVideo(vidLoc,videoView);
        Log.d(TAG, "Inside stop recording");
        VidVisible = true;
    }
}