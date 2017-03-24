package com.ks.media.camera;

/**
 * Created by Admin on 2017/3/23 0023 10:27.
 * Author: kang
 * Email: kangsafe@163.com
 */

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Fragment;
import android.view.Window;
import android.view.WindowManager;

public class ImageCaptureActivity extends Activity {
    private String filePath;
    private boolean rotation = false;
    private boolean isline = true;
    private boolean istips = true;
    private String tips = "";
    private boolean iscrop = true;
    private boolean iscroptips = true;
    private String croptips = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePath = getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT);
        rotation = getIntent().getBooleanExtra("rotation", false);
        isline = getIntent().getBooleanExtra("isline", true);
        istips = getIntent().getBooleanExtra("istips", true);
        tips = getIntent().getStringExtra("tips");
        iscrop = getIntent().getBooleanExtra("iscrop", true);
        iscroptips = getIntent().getBooleanExtra("iscroptips", true);
        croptips = getIntent().getStringExtra("croptips");
        // 设置横屏
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_capture);
        if (null == savedInstanceState) {
            Bundle bundle = new Bundle();
            bundle.putString(MediaStore.EXTRA_OUTPUT, filePath);
            bundle.putBoolean("rotation", rotation);
            bundle.putBoolean("isline", isline);
            bundle.putBoolean("istips", istips);
            bundle.putString("tips", tips);
            bundle.putBoolean("iscrop", iscrop);
            bundle.putBoolean("iscroptips", iscroptips);
            bundle.putString("croptips", croptips);
            Fragment fragment = null;
            //api 21即android 5.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment = Camera2Fragment.newInstance();
            } else {
                fragment = CameraFragment.newInstance();
            }
            if (fragment != null) {
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
            }
        }
    }
}
