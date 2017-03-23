package com.ks.media.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void takePhote(View view) {
        startActivity(new Intent(this, TakePhoteActivity.class));
    }

    public void takePhote2(View view) {
        startActivity(new Intent(this, TakePhoto2Activity.class));
    }

    public void camera(View view) {
        Intent intent = new Intent("com.ks.media.action.IMAGE_CAPTURE");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EsoNote/cropper/";
        intent.putExtra(MediaStore.EXTRA_OUTPUT, path);
        intent.putExtra("rotation", true);
        intent.putExtra("isline",true);
        intent.putExtra("istips",true);
        intent.putExtra("tips","这是标题\n包含子标题");
        intent.putExtra("iscrop",true);
        intent.putExtra("iscroptips",true);
        intent.putExtra("croptips","请选择裁切位置");
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode==Activity.RESULT_OK) {
                    Log.i("path", data.getStringExtra("data"));
                }
                break;
        }
    }
}
