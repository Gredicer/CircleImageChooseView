package com.gredicer.circleimagechooseview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private AvatarImageView avatarImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        InitAvatarImageView();
        //初始化裁剪工具
    }

    private void InitAvatarImageView() {
        avatarImageView = (AvatarImageView) findViewById(R.id.avatarIv);

        avatarImageView.setAfterCropListener(new AvatarImageView.AfterCropListener() {
            @Override
            public void afterCrop() {
                Toast.makeText(MainActivity.this,"设置新的头像成功",Toast.LENGTH_SHORT).show();
                avatarImageView.setImageURI( avatarImageView.getImage_uri() );
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //在拍照、选取照片、裁剪Activity结束后，调用的方法
        if(avatarImageView != null){
            avatarImageView.onActivityResult(requestCode,resultCode,data);
        }
    }
}
