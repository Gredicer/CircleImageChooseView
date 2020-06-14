[![](https://www.jitpack.io/v/Gredicer/CircleImageChooseView.svg)](https://www.jitpack.io/#Gredicer/CircleImageChooseView)
## CircleImageChooseView
A New circle image control and you can use it as head portrait control,It has two function:  
1. choose Camera to take pictures or select the picture from the album
2. Crop the image and display it on this control

This is base [SelectAvatarApplication](https://github.com/zhudfly/SelectAvatarApplication) and [CropPicture](https://github.com/Goodbao/CropPicture)

![preview](/gif/1575939298556.gif)

## Gradle 
Step 1. Add the JitPack repository to your build file 
```
	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```
dependencies {
	       implementation 'com.github.Gredicer.CircleImageChooseView:avatorView:1.2.9'
	}
```

## Usage

#### layout  
```
    <com.gredicer.avatorview.AvatarImageView
        android:id="@+id/avatarIv"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:src="@drawable/gredicer" />
```
#### Sample use 
```
    private AvatarImageView avatarImageView;

    avatarImageView = (AvatarImageView) findViewById(R.id.avatarIv);


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(avatarImageView != null){
            avatarImageView.onActivityResult(requestCode,resultCode,data);
        }
    }
	

```
#### Attributes
```
//设置对话框的背景色
avatarImageView.setDialogBackgroundColor("#00AAAA"); 

//设置按钮点击后的颜色
avatarImageView.setBtnClickedColor("#00AAAA"); 

//设置dialog显示的动画
avatarImageView.setAnimResId(R.style.avatar_dialog_animation); 

//设置标题的颜色
avatarImageView.setTitleColor("#FFEEAA");  

//设置按钮的背景色
avatarImageView.setBtnBackgroundColor("#FFEEAA"); 

//设置标题下的分割线的颜色
avatarImageView.setTitleLineColor("#FFEEAA"); 

//设置按钮之间的分割线的颜色
avatarImageView.setLineColor("#FFEEAA"); 

//设置标题的padding
avatarImageView.setTitlePaddingTopBottom(30); 

//设置按钮的padding
avatarImageView.setBtnPaddingTopBottom(30); 

//设置标题的文字
avatarImageView.setTitleText("testTitle"); 

//设置拍照按钮的文字
avatarImageView.setPhotoButtonText("testPhotoText"); 

//设置选择照片的文字
avatarImageView.setChoosePicButtonText("testChooseText"); 

//设置dialog的角度
avatarImageView.setDialogCorner(20); 

//设置按钮文本的颜色
avatarImageView.setBtnTextColor("#FFEEAA"); 

//设置标题的文字大小
avatarImageView.setTitleTextSize(30); 

//设置按钮的文字大小
avatarImageView.setBtnTextSize(30); 
```

#### When the avataar switch is successful
```
        avatarImageView.setAfterCropListener(new AvatarImageView.AfterCropListener() {
            @Override
            public void afterCrop() {
                Toast.makeText(MainActivity.this,"设置新的头像成功",Toast.LENGTH_SHORT).show();
                //avatarImageView.setImageURI( avatarImageView.getImage_uri() );
            }
        });
```

#### Set and get image
```
 avatarImageView.setImageURI( );	
 avatarImageView.getImage_uri();	// This uri is the image that you cut 
```
## Changelog
* 1.2.4
	* Fit API 29
	* Environment.getExternalStorageDirectory() was replaced by activiy.getExternalCacheDir()
* 1.0.2
	* Initial release

## License
```
MIT License

Copyright (c) 2020 Gredicer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
