package com.gredicer.avatorview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * Author zhudf
 * Date 2015/10/20
 * <p/>
 * 一个圆形的头像控件，点击后会弹出选择照片或者拍照的对话框
 */
@SuppressLint("AppCompatCustomView")
public class AvatarImageView extends ImageView {

    private Uri image_uri;
    //对话框的对象
    private AvatarDialog avatarDialog;
    //Activity的上下文
    private Context mContext;

    //Dialog的属性定制
    private String titleColor;
    private String btnBackgroundColor;
    private String titleLineColor;
    private String lineColor;
    private String dialogBackgroundColor;
    private String btnTextColor;

    private CharSequence mTitle;
    private CharSequence mPhotoButtonText;
    private CharSequence mChoosePicButtonText;

    private int titlePaddingTopBottom = -1;
    private int btnPaddingTopBottom = -1;
    private int dialogCorner = -1;
    private int animResId = -1;
    private float titleTextSize = -1;
    private float btnTextSize = -1;

    //裁剪完照片的回调
    private AfterCropListener afterCropListener;
    private String btnClickedColor;
    private CropImageUtils cropImageUtils;


    public AvatarImageView(Context context) {
        super(context);
        this.mContext = context;
        //在编辑模式下不需要进行初始化
        if (!this.isInEditMode()) {
            //初始化文件相关
            init();
            //初始化点击事件
            initClickListener();
        }
    }


    public AvatarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        //在编辑模式下不需要进行初始化
        if (!this.isInEditMode()) {
            //初始化文件相关
            init();
            //初始化点击事件
            initClickListener();
        }
    }

    public AvatarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        //在编辑模式下不需要进行初始化
        if (!this.isInEditMode()) {
            //初始化文件相关
            init();
            //初始化点击事件
            initClickListener();
        }
    }

    public void init() {
        Log.d( "asd", "init: "+getContext());
        cropImageUtils.FILE_CONTENT_FILEPROVIDER=CropImageUtils.getFileProviderName( getContext() );
        //初始化裁剪工具
        cropImageUtils = new CropImageUtils((Activity) getContext());

        judgePermission();

    }



    protected void judgePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            List <String> permissionList = new ArrayList <>();
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.
                    permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.CAMERA);
            }
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.
                    permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissionList.isEmpty()) {
                String[] permissions = permissionList.toArray(new String[permissionList.size()]);
                ActivityCompat.requestPermissions((Activity) getContext(), permissions, 1);
            } else {

            }
        }
    }





    @Override
    protected void onDraw(Canvas canvas) {
        //从ImageView中获取drawable对象
        Drawable drawable = getDrawable();

        /**
         * 以下情况不需要进行绘制:
         * 1.drawable 为空；
         * 2.图片高度为0
         * 3.图片宽度为0
         */
        if (drawable == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        //将drawable转换成Bitmap格式
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        //获得高度和宽度
        int mHeight = getHeight();
        int mWidth = getWidth();

        //计算半径
        int radius = (mWidth < mHeight ? mWidth : mHeight) / 2;

        //画圆形图像
        Bitmap roundBitmap = cropBitmap(bitmap, radius);
        canvas.drawBitmap(roundBitmap, mWidth / 2 - radius,
                mHeight / 2 - radius, null);
    }

    /**
     * 对原来的bitmap进行裁剪
     *
     * @param bmp    原有的bitmap
     * @param radius 半径
     */
    public Bitmap cropBitmap(Bitmap bmp, int radius) {
        Bitmap scaledSrcBmp;
        //获得直径
        int diameter = radius * 2;

        // 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        int squareWidth = 0, squareHeight = 0;
        int x = 0, y = 0;
        Bitmap squareBitmap;
        if (bmpHeight > bmpWidth) {// 高大于宽
            squareWidth = squareHeight = bmpWidth;
            x = 0;
            y = (bmpHeight - bmpWidth) / 2;
            // 截取正方形图片
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,
                    squareHeight);
        } else if (bmpHeight < bmpWidth) {// 宽大于高
            squareWidth = squareHeight = bmpHeight;
            x = (bmpWidth - bmpHeight) / 2;
            y = 0;
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,
                    squareHeight);
        } else {
            squareBitmap = bmp;
        }

        //获得缩放后的bitmap
        if (squareBitmap.getWidth() != diameter
                || squareBitmap.getHeight() != diameter) {
            scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter,
                    diameter, true);
        } else {
            scaledSrcBmp = squareBitmap;
        }

        //获得将画入画布的bitmap
        Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(),
                scaledSrcBmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(),
                scaledSrcBmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(scaledSrcBmp.getWidth() / 2,
                scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2,
                paint);
        paint.setXfermode(new PorterDuffXfermode( PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);

        //变量置空后等待系统回收
        bmp = null;
        squareBitmap = null;
        scaledSrcBmp = null;

        return output;
    }

    /**
     * 初始化点击事件
     * 点击的效果为弹出对话框选择拍照或者从相册获取
     */
    private void initClickListener() {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarDialog = new AvatarDialog(AvatarImageView.this.mContext);
                avatarDialog.setCancelable(true);
                avatarDialog.setPositiveListener(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        cropImageUtils.takePhoto();
                        avatarDialog.dismiss();
                    }
                });
                avatarDialog.setNegativeListener(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Log.e("asd", "onClick: "+"选择从手机选择选项" );

                        cropImageUtils.openAlbum();
                        avatarDialog.dismiss();
                    }
                });






                //设置样式的属性
                setDialogAttr();

                avatarDialog.show();
            }
        });
    }

    private void setDialogAttr() {
        avatarDialog.setDialogBackgroundColor(dialogBackgroundColor);

        avatarDialog.setTitleColor(titleColor);

        avatarDialog.setBtnBackGroundColor(btnBackgroundColor);

        avatarDialog.setBtnClickedColor(btnClickedColor);

        avatarDialog.setBtnTextColor(btnTextColor);

        avatarDialog.setTitleLineColor(titleLineColor);

        avatarDialog.setLineColor(lineColor);

        avatarDialog.setTitlePaddingTopBottom(titlePaddingTopBottom);

        avatarDialog.setBtnPaddingTopBottom(btnPaddingTopBottom);

        avatarDialog.setTitleText(mTitle);

        avatarDialog.setPhotoButtonText(mPhotoButtonText);

        avatarDialog.setChoosePicButtonText(mChoosePicButtonText);

        avatarDialog.setDialogCorner(dialogCorner);

        avatarDialog.setAnimResId(animResId);

        avatarDialog.setTitleTextSize(titleTextSize);

        avatarDialog.setBtnTextSize(btnTextSize);
    }

    public void setImage_uri(Uri uri){
        this.image_uri=uri;
    }
    public Uri getImage_uri(){
        return this.image_uri;
    }


    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.d( "asd_AvatarImageView", requestCode+" "+resultCode+" "+ data );
        cropImageUtils.onActivityResult(requestCode, resultCode, data, new CropImageUtils.OnResultListener() {
            @Override
            public void takePhotoFinish(String path) {
                Toast.makeText(mContext, "照片存放在：" + path, Toast.LENGTH_SHORT).show();
                //拍照回调，去裁剪
                cropImageUtils.cropPicture(path);
            }

            @Override
            public void selectPictureFinish(String path) {
                Toast.makeText(mContext, "打开图片：" + path, Toast.LENGTH_SHORT).show();
                Log.e("asd", "selectPictureFinish: "+"4、打开图片成功，去剪裁"+path );
                //相册回调，去裁剪
                cropImageUtils.cropPicture(path);
            }

            @Override
            public void cropPictureFinish(String path) {
                Toast.makeText(mContext, "裁剪保存在：" + path, Toast.LENGTH_SHORT).show();

                //裁剪回调
                setImageURI( Uri.parse( path ) );
                setImage_uri(Uri.parse( path ));
                if (afterCropListener != null) {
                    afterCropListener.afterCrop();
                }

            }
        });



    }



    @Override
    public void setOnClickListener(OnClickListener l) {
        //使点击事件设置无效
        // super.setOnClickListener(l);
    }

    //对弹出的AlterDialog进行样式的设置
    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    public void setBtnBackgroundColor(String btnBackgroundColor) {
        this.btnBackgroundColor = btnBackgroundColor;
    }

    public void setTitleLineColor(String titleLineColor) {
        this.titleLineColor = titleLineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public void setTitlePaddingTopBottom(int titlePaddingTopBottom) {
        this.titlePaddingTopBottom = titlePaddingTopBottom;
    }

    public void setBtnPaddingTopBottom(int btnPaddingTopBottom) {
        this.btnPaddingTopBottom = btnPaddingTopBottom;
    }

    public void setTitleText(CharSequence mTitle) {
        this.mTitle = mTitle;
    }

    public void setPhotoButtonText(CharSequence mPhotoButtonText) {
        this.mPhotoButtonText = mPhotoButtonText;
    }

    public void setChoosePicButtonText(CharSequence mChoosePicButtonText) {
        this.mChoosePicButtonText = mChoosePicButtonText;
    }

    public void setDialogCorner(int dialogCorner) {
        this.dialogCorner = dialogCorner;
    }

    public void setAnimResId(int animResId) {
        this.animResId = animResId;
    }

    public void setBtnTextColor(String btnTextColor) {
        this.btnTextColor = btnTextColor;
    }

    public void setTitleTextSize(float titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    public void setBtnTextSize(float btnTextSize) {
        this.btnTextSize = btnTextSize;
    }

    public void setDialogBackgroundColor(String dialogBackgroundColor) {
        this.dialogBackgroundColor = dialogBackgroundColor;
    }

    public void setBtnClickedColor(String btnClickedColor) {
        this.btnClickedColor = btnClickedColor;
    }

    //图片裁剪完的监听器
    public interface AfterCropListener {
        // 在裁剪完后会调用该函数
        void afterCrop();
    }

    public void setAfterCropListener(AfterCropListener afterCropListener) {
        this.afterCropListener = afterCropListener;
    }

    /**
     * 显示选项的对话框
     * 提示用户选择拍照或者从相册选择
     */
    public class AvatarDialog extends AlertDialog {
        private static final float TITLE_TEXT_SIZE = 17;
        private static final float BTN_TEXT_SIZE = 15;

        //默认显示的文字
        private final CharSequence PHOTO_BTN_SHOW = "拍 照";
        private final CharSequence CHOOSE_PIC_BTN_SHOW = "从手机相册选择";
        private final CharSequence TITLE_SHOW = "选择头像";

        //默认的颜色值
        private final String DIALOG_BACKGROUND_COLOR = "#f5f5f5";
        private final String TITLE_COLOR = "#555555";
        private final String BTN_COLOR = "#f5f5f5";
        private final String BTN_CLICKED_COLOR = "#f5f5f5";
        private final String LINE_COLOR = "#f5f5f5";
        private final String LINE_BETWEEN_BTN_COLOR = "#f5f5f5";
        private final String BTN_TEXT_COLOR = "#555555";

        //设置控件的padding值 dp的单位
        private final int TITLE_PADDING_TOP_BOTTOM = 17;
        private final int BTN_PADDING_TOP_BOTTOM = 15;

        //dialog 角度 dp值
        private final int DIALOG_CORNER = 15;

        //标题、按钮和分割线的颜色
        private String dialogBackgroundColor = DIALOG_BACKGROUND_COLOR;
        private String titleColor = TITLE_COLOR;
        private String btnBackgroundColor = BTN_COLOR;
        private String btnTextColor = BTN_TEXT_COLOR;
        private String titleLineColor = LINE_COLOR;
        private String lineColor = LINE_BETWEEN_BTN_COLOR;
        //点击后显示的颜色
        private String btnClickedColor = BTN_CLICKED_COLOR;

        //控件的padding值
        private int titlePaddingTopBottom = TITLE_PADDING_TOP_BOTTOM;
        private int btnPaddingTopBottom = BTN_PADDING_TOP_BOTTOM;

        //标题及按钮上显示的文字
        private CharSequence mTitle = TITLE_SHOW;
        private CharSequence mPhotoButtonText = PHOTO_BTN_SHOW;
        private CharSequence mChoosePicButtonText = CHOOSE_PIC_BTN_SHOW;

        //按钮的点击事件
        private OnClickListener mPhotoButtonListener;
        private OnClickListener mChoosePicListener;

        //标题控件
        private TextView mTitleTv;
        //拍照按钮控件
        private TextView mPhotoBtn;
        //选择图片控件
        private TextView mChoosePicBtn;

        //保存上下文
        Context mContext;

        //动画资源文件的id
        private int animResId = -1;

        //dialog 角度 dp值
        private int dialogCorner = DIALOG_CORNER;

        //文字大小
        private float titleTextSize = TITLE_TEXT_SIZE;
        private float btnTextSize = BTN_TEXT_SIZE;


        public AvatarDialog(Context context) {
            super(context);
            this.mContext = context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawable(new ColorDrawable(0));
            super.onCreate(savedInstanceState);

            Window window = getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            int width = getContext().getResources().getDisplayMetrics().widthPixels;
            params.width = (int) (width * 4f / 5);
            window.setAttributes(params);

            //设置动画
            if (animResId > 0) {
                window.setWindowAnimations(animResId);
            }
            setContentView(createView());
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private View createView() {
            //生成根布局
            LinearLayout mRootView = new LinearLayout(this.mContext);
            //默认颜色为白色
            mRootView.setBackgroundColor(Color.parseColor(dialogBackgroundColor));
            mRootView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            //设置布局方向为竖直
            mRootView.setOrientation(LinearLayout.VERTICAL);
            mRootView.setPadding(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, //转换dp值
                    dialogCorner, //dp值
                    getResources().getDisplayMetrics()));
            //设置布局形状
            GradientDrawable rootBackGround = new GradientDrawable();
            rootBackGround.setColor(Color.parseColor(dialogBackgroundColor));

            //将dp值的角度转换为px值
            float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, //转换dp值
                    dialogCorner, //dp值
                    getResources().getDisplayMetrics());
            rootBackGround.setCornerRadius(radius);

            //setBackGroundDrawable需要判断当前sdk的版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mRootView.setBackground(rootBackGround);
            } else {
                mRootView.setBackgroundDrawable(rootBackGround);
            }

            //生成并加入标题
            mTitleTv = new TextView(this.mContext);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mTitleTv.setLayoutParams(params);
            mTitleTv.setText(mTitle);
            mTitleTv.setTextColor(Color.parseColor(titleColor));
            mTitleTv.setGravity(Gravity.CENTER);
            mTitleTv.setTextSize(titleTextSize);
            //设置padding
            //将dp值的角度转换为px值
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, //转换dp值
                    titlePaddingTopBottom, //dp值
                    getResources().getDisplayMetrics());
            mTitleTv.setPadding(0, padding, 0, padding);
            mRootView.addView(mTitleTv);

            //加入分割线
            TextView mLine = new TextView(this.mContext);
            mLine.setBackgroundColor(Color.parseColor(titleLineColor));

            //将dp值的角度转换为px值
            int lineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, //转换dp值
                    1, //dp值
                    getResources().getDisplayMetrics());
            ViewGroup.LayoutParams paramsForLine = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    lineHeight);
            mLine.setLayoutParams(paramsForLine);
            mRootView.addView(mLine);

            //生成并加入拍照按钮
            mPhotoBtn = new TextView(this.mContext);
            mPhotoBtn.setBackgroundColor(Color.parseColor(btnBackgroundColor));
            setViewClickedColor(mPhotoBtn);


            mPhotoBtn.setText(mPhotoButtonText);
            mPhotoBtn.setTextSize(btnTextSize);
            mPhotoBtn.setGravity(Gravity.CENTER);
            mPhotoBtn.setTextColor(Color.parseColor(btnTextColor));
            mPhotoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    if (mPhotoButtonListener != null) {
                        mPhotoButtonListener.onClick(AvatarDialog.this, BUTTON_POSITIVE);
                    }
                }
            });
            //将dp值的角度转换为px值
            padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, //转换dp值
                    btnPaddingTopBottom, //dp值
                    getResources().getDisplayMetrics());
            mPhotoBtn.setPadding(0, padding, 0, padding);
            mRootView.addView(mPhotoBtn);

            //加入分割线
            TextView mLineBetweenBtn = new TextView(this.mContext);
            mLineBetweenBtn.setBackgroundColor(Color.parseColor(lineColor));
            mLineBetweenBtn.setLayoutParams(paramsForLine);
            mRootView.addView(mLineBetweenBtn);

            //生成并加入选择图片按钮
            mChoosePicBtn = new TextView(this.mContext);
            mChoosePicBtn.setText(mChoosePicButtonText);
            mChoosePicBtn.setTextSize(btnTextSize);
            mChoosePicBtn.setGravity(Gravity.CENTER);
            mChoosePicBtn.setTextColor(Color.parseColor(btnTextColor));

            mChoosePicBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        Toast.makeText(mContext, "点击 ", Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                    if (mChoosePicListener != null) {
                        mChoosePicListener.onClick(AvatarDialog.this, BUTTON_NEGATIVE);

                    }
                }
            });

            mChoosePicBtn.setBackgroundColor(Color.parseColor(btnBackgroundColor));
            setViewClickedColor(mChoosePicBtn);

            //将dp值的角度转换为px值
            padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, //转换dp值
                    btnPaddingTopBottom, //dp值
                    getResources().getDisplayMetrics());
            mChoosePicBtn.setPadding(0, padding, 0, padding);
            mRootView.addView(mChoosePicBtn);

            return mRootView;
        }

        /**
         * 设置控件的点击效果
         * 5.0之后为水波纹效果
         *
         * @param desView 需要设置点击效果的控件
         */
        private void setViewClickedColor(final View desView) {
            //5.0之后加入水波纹效果
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int[][] states = new int[1][];
                int[] colors = new int[]{Color.parseColor(btnClickedColor)};
                states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
                ColorStateList colorStateList = new ColorStateList(states, colors);
                RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, desView.getBackground(), null);
                desView.setBackground(rippleDrawable);
            } else {
                //5.0之前为点击替换背景颜色
                desView.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                desView.setBackgroundColor(Color.parseColor(btnClickedColor));
                                break;
                            case MotionEvent.ACTION_UP:
                                desView.setBackgroundColor(Color.parseColor(btnBackgroundColor));
                                break;
                        }
                        return false;
                    }
                });
            }
        }

        /**
         * 拍照
         */
        public AvatarDialog setPositiveListener(final OnClickListener listener) {
            mPhotoButtonListener = listener;
            return this;
        }

        /**
         * 选择照片
         */
        public AvatarDialog setNegativeListener(final OnClickListener listener) {
            mChoosePicListener = listener;
            return this;
        }


        public void setTitleColor(String titleColor) {
            if (!TextUtils.isEmpty(titleColor)) {
                this.titleColor = titleColor;
            }
        }

        public void setBtnBackGroundColor(String btnColor) {
            if (!TextUtils.isEmpty(btnColor)) {
                this.btnBackgroundColor = btnColor;
            }
        }

        public void setTitleLineColor(String titleLineColor) {
            if (!TextUtils.isEmpty(titleLineColor)) {
                this.titleLineColor = titleLineColor;
            }
        }

        public void setLineColor(String lineColor) {
            if (!TextUtils.isEmpty(lineColor)) {
                this.lineColor = lineColor;
            }
        }

        public void setTitlePaddingTopBottom(int titlePaddingTopBottom) {
            if (titlePaddingTopBottom > 0) {
                this.titlePaddingTopBottom = titlePaddingTopBottom;
            }
        }

        public void setBtnPaddingTopBottom(int btnPaddingTopBottom) {
            if (btnPaddingTopBottom > 0) {
                this.btnPaddingTopBottom = btnPaddingTopBottom;
            }
        }

        public void setTitleText(CharSequence mTitle) {
            if (!TextUtils.isEmpty(mTitle)) {
                this.mTitle = mTitle;
            }
        }

        public void setPhotoButtonText(CharSequence mPhotoButtonText) {
            if (!TextUtils.isEmpty(mPhotoButtonText)) {
                this.mPhotoButtonText = mPhotoButtonText;
            }
        }

        public void setChoosePicButtonText(CharSequence mChoosePicButtonText) {
            if (!TextUtils.isEmpty(mChoosePicButtonText)) {
                this.mChoosePicButtonText = mChoosePicButtonText;
            }
        }

        public void setDialogCorner(int dialogCorner) {
            if (dialogCorner >= 0) {
                this.dialogCorner = dialogCorner;
            }
        }

        public void setAnimResId(int animResId) {
            this.animResId = animResId;
        }

        public void setBtnTextColor(String btnTextColor) {
            if (!TextUtils.isEmpty(btnTextColor)) {
                this.btnTextColor = btnTextColor;
            }
        }

        public void setTitleTextSize(float titleTextSize) {
            if (titleTextSize > 0) {
                this.titleTextSize = titleTextSize;
            }
        }

        public void setBtnTextSize(float btnTextSize) {
            if (btnTextSize > 0) {
                this.btnTextSize = btnTextSize;
            }
        }

        public void setDialogBackgroundColor(String dialogBackgroundColor) {
            if (!TextUtils.isEmpty(dialogBackgroundColor)) {
                this.dialogBackgroundColor = dialogBackgroundColor;
            }
        }

        public void setBtnClickedColor(String btnClickedColor) {
            if (!TextUtils.isEmpty(btnClickedColor)) {
                this.btnClickedColor = btnClickedColor;
            }
        }
    }
}
