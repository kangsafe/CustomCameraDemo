package com.ks.media.camera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ks.media.camera.camare.CameraPreview;
import com.ks.media.camera.camare.FocusView;
import com.ks.media.camera.camare.ReferenceLine;
import com.ks.media.camera.cropper.CropImageView;
import com.ks.media.camera.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

/**
 * Created by Admin on 2017/3/23 0023 10:17.
 * Author: kang
 * Email: kangsafe@163.com
 */

public class CameraFragment extends Fragment implements View.OnClickListener, CameraPreview.OnCameraStatusListener,
        SensorEventListener {
    CameraPreview mCameraPreview;
    CropImageView mCropImageView;
    RelativeLayout mTakePhotoLayout;
    LinearLayout mCropperLayout;
    ImageView mCameraTake;
    ImageView mCameraClose;
    ImageView mCropperClose;
    ImageView mCropperStart;
    ReferenceLine mCameraLine;
    TextView mCameraHint;
    TextView mCropperHint;
    View view;
    private Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private String PATH;
    private boolean rotation = false;
    private boolean isline = true;
    private boolean istips = true;
    private String tips = "";
    private boolean iscrop = true;
    private boolean iscroptips = true;
    private String croptips = "";

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_camera, container, false);
        // Initialize components of the app
        mCameraTake = (ImageView) view.findViewById(R.id.vcamera_take);
        mCameraTake.setOnClickListener(this);
        mCameraClose = (ImageView) view.findViewById(R.id.vcamera_close);
        mCameraClose.setOnClickListener(this);
        mCameraLine = (ReferenceLine) view.findViewById(R.id.vcamera_line);
        mCameraHint = (TextView) view.findViewById(R.id.hint);
        mCropperClose = (ImageView) view.findViewById(R.id.vcropper_close);
        mCropperClose.setOnClickListener(this);
        mCropperStart = (ImageView) view.findViewById(R.id.vcropper_start);
        mCropperStart.setOnClickListener(this);
        mCropImageView = (CropImageView) view.findViewById(R.id.CropImageView);
        mCameraPreview = (CameraPreview) view.findViewById(R.id.cameraPreview);
        FocusView focusView = (FocusView) view.findViewById(R.id.view_focus);
        mTakePhotoLayout = (RelativeLayout) view.findViewById(R.id.take_photo_layout);
        mCropperLayout = (LinearLayout) view.findViewById(R.id.cropper_layout);
        mCropperHint = (TextView) view.findViewById(R.id.crop_hint);
        mCameraPreview.setFocusView(focusView);
        mCameraPreview.setOnCameraStatusListener(this);
        mCropImageView.setGuidelines(2);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.
                SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.
                TYPE_ACCELEROMETER);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PATH = getArguments().getString(MediaStore.EXTRA_OUTPUT);
        rotation = getArguments().getBoolean("rotation", false);
        isline = getArguments().getBoolean("isline", true);
        istips = getArguments().getBoolean("istips", true);
        tips = getArguments().getString("tips");
        iscrop = getArguments().getBoolean("iscrop", true);
        iscroptips = getArguments().getBoolean("iscroptips", true);
        croptips = getArguments().getString("croptips");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isline) {
            mCameraLine.setVisibility(View.VISIBLE);
        } else {
            mCameraLine.setVisibility(View.GONE);
        }
        if (istips) {
            mCameraHint.setText(tips);
            mCameraHint.setVisibility(View.VISIBLE);
        } else {
            mCameraHint.setVisibility(View.GONE);
        }
        if (iscroptips) {
            mCropperHint.setVisibility(View.VISIBLE);
            mCropperHint.setText(croptips);
        } else {
            mCropperHint.setVisibility(View.GONE);
        }
        if (rotation) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mCameraHint, "rotation", 0f, 90f);
            animator.setStartDelay(800);
            animator.setDuration(1000);
            animator.setInterpolator(new LinearInterpolator());
            animator.start();
            AnimatorSet animSet = new AnimatorSet();
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(mCropperHint, "rotation", 0f, 90f);
            ObjectAnimator moveIn = ObjectAnimator.ofFloat(mCropperHint, "translationX", 0f, -50f);
            animSet.play(animator1).before(moveIn);
            animSet.setDuration(10);
            animSet.start();
            mCameraTake.setRotation(0);
        }
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    public void takePhoto() {
        if (mCameraPreview != null) {
            mCameraPreview.takePicture();
        }
    }

    public void finish() {
        super.onDestroy();
        getActivity().finish();
    }

    public void close() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        finish();
    }

    /**
     * 关闭截图界面
     */
    public void closeCropper() {
        showTakePhotoLayout();
    }

    /**
     * 开始截图，并保存图片
     */
    public void startCropper() {
        //获取截图并旋转90度
        CropperImage cropperImage = mCropImageView.getCroppedImage();
        Log.e(TAG, cropperImage.getX() + "," + cropperImage.getY());
        Log.e(TAG, cropperImage.getWidth() + "," + cropperImage.getHeight());
        Bitmap bitmap;
        if (rotation) {
            bitmap = Utils.rotate(cropperImage.getBitmap(), -90);
        } else {
            bitmap = Utils.rotate(cropperImage.getBitmap(), 0);
        }
        // 系统时间
        long dateTaken = System.currentTimeMillis();
        // 图像名称
        String filename = DateFormat.format("yyyyMMddHHmmss", dateTaken)
                .toString() + ".jpg";
        Uri uri = insertImage(getActivity().getContentResolver(), filename, dateTaken, PATH, filename, bitmap, null);
        cropperImage.getBitmap().recycle();
        cropperImage.setBitmap(null);
//        Intent intent = new Intent(getActivity(), ShowCropperedActivity.class);
//        intent.setData(uri);
//        intent.putExtra("path", PATH + filename);
//        intent.putExtra("width", bitmap.getWidth());
//        intent.putExtra("height", bitmap.getHeight());
//        intent.putExtra("cropperImage", cropperImage);
//        startActivity(intent);
        bitmap.recycle();
        Intent intent = new Intent();
        intent.putExtra("data", PATH + filename);
        getActivity().setResult(Activity.RESULT_OK, intent);
        finish();
        getActivity().overridePendingTransition(R.anim.fade_in,
                R.anim.fade_out);
//        doAnimation(cropperImage);
    }

    private void doAnimation(CropperImage cropperImage) {
        ImageView imageView = new ImageView(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.image_view_layout, null);
        ((RelativeLayout) view.findViewById(R.id.root_layout)).addView(imageView);
        RelativeLayout relativeLayout = ((RelativeLayout) view.findViewById(R.id.root_layout));
//        relativeLayout.addView(imageView);
        imageView.setX(cropperImage.getX());
        imageView.setY(cropperImage.getY());
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = (int) cropperImage.getWidth();
        lp.height = (int) cropperImage.getHeight();
        imageView.setLayoutParams(lp);
        imageView.setImageBitmap(cropperImage.getBitmap());
        try {
            getActivity().getWindow().addContentView(view, lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator translationX = ObjectAnimator.ofFloat(this, "translationX", cropperImage.getX(), 0);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this, "translationY", cropperImage.getY(), 0);*/

        TranslateAnimation translateAnimation = new TranslateAnimation(
                0, -cropperImage.getX(), 0, -(Math.abs(cropperImage.getHeight() - cropperImage.getY())));// 当前位置移动到指定位置
        RotateAnimation rotateAnimation = new RotateAnimation(0, -90,
                Animation.ABSOLUTE, cropperImage.getX(), Animation.ABSOLUTE, cropperImage.getY());
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(rotateAnimation);
        animationSet.setFillAfter(true);
        animationSet.setDuration(2000L);
        imageView.startAnimation(animationSet);
//        finish();
    }

    /**
     * 拍照成功后回调
     * 存储图片并显示截图界面
     *
     * @param data
     */
    @Override
    public void onCameraStopped(byte[] data) {
        Log.i("TAG", "==onCameraStopped==");
        // 创建图像
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        // 系统时间
        long dateTaken = System.currentTimeMillis();
        // 图像名称
        String filename = DateFormat.format("yyyyMMddHHmmss", dateTaken)
                .toString() + ".jpg";
        // 存储图像（PATH目录）
        Uri source = insertImage(getActivity().getContentResolver(), filename, dateTaken, PATH,
                filename, bitmap, data);
        if (iscrop) {
            //准备截图
            try {
                mCropImageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), source));
//            mCropImageView.rotateImage(90);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            showCropperLayout();
        } else {
            Intent intent = new Intent();
            intent.putExtra("data", PATH + filename);
            getActivity().setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /**
     * 存储图像并将信息添加入媒体数据库
     */
    private Uri insertImage(ContentResolver cr, String name, long dateTaken,
                            String directory, String filename, Bitmap source, byte[] jpegData) {
        OutputStream outputStream = null;
        String filePath = directory + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(directory, filename);
            if (file.createNewFile()) {
                outputStream = new FileOutputStream(file);
                if (source != null) {
                    source.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                } else {
                    outputStream.write(jpegData);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable t) {
                }
            }
        }
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Images.Media.TITLE, name);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATA, filePath);
        return cr.insert(IMAGE_URI, values);
    }

    private void showTakePhotoLayout() {
        mTakePhotoLayout.setVisibility(View.VISIBLE);
        mCropperLayout.setVisibility(View.GONE);
    }

    private void showCropperLayout() {
        mTakePhotoLayout.setVisibility(View.GONE);
        mCropperLayout.setVisibility(View.VISIBLE);
        mCameraPreview.start();   //继续启动摄像头
    }


    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    private boolean mInitialized = false;
    private SensorManager mSensorManager;
    private Sensor mAccel;

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }
        float deltaX = Math.abs(mLastX - x);
        float deltaY = Math.abs(mLastY - y);
        float deltaZ = Math.abs(mLastZ - z);

        if (deltaX > 0.8 || deltaY > 0.8 || deltaZ > 0.8) {
            mCameraPreview.setFocus();
        }
        mLastX = x;
        mLastY = y;
        mLastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vcamera_take:
                takePhoto();//拍照
                break;
            case R.id.vcamera_close:
                close();//关闭拍照
                break;
            case R.id.vcropper_close:
                closeCropper();//关闭裁剪
                break;
            case R.id.vcropper_start:
                startCropper();//开始裁剪
                break;
        }
    }
}
