[![](https://www.jitpack.io/v/Gredicer/CircleImageChooseView.svg)](https://www.jitpack.io/#Gredicer/CircleImageChooseView)
# CircleImageChooseView
A New circle image control and you can use it as head portrait control,It has two function:  
1. choose Camera to take pictures or select the picture from the album
2. Crop the image and display it on this control

This is base [SelectAvatarApplication](https://github.com/zhudfly/SelectAvatarApplication) and [CropPicture](https://github.com/Goodbao/CropPicture)

# Gradle 
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
	        implementation 'com.github.Gredicer:CircleImageChooseView:1.0.2'
	}
```