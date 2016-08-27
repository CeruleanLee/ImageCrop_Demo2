package com.example.administrator.imagecrop_demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageActivity extends AppCompatActivity {

    @Bind(R.id.button_img)
    Button buttonImg;
    @Bind(R.id.imageView_img)
    ImageView imageViewImg;
    @Bind(R.id.textView_img)
    TextView textViewImg;

    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    File tempFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);
        tempFile = new File(Environment.getExternalStorageDirectory(), getPhotoFileName());

    }
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'Img'_yyyyMMdd_HHmmss");

        return dateFormat.format(date) + ".jpg";
    }
//    //设置Toolbar标题
String cameraScalePath;
    /**
     * 经过裁剪，返回结果，这里我一般只需要裁剪后的图片绝对路径（调用上面startUCrop，即返回图片路径），然后调接口上传服务器。
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode==PHOTO_REQUEST_TAKEPHOTO){
            cameraScalePath = startUCrop(ImageActivity.this, tempFile, UCrop.REQUEST_CROP, 1, 1);


        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Log.d("0826","==============resultUri"+resultUri);
            sendPicToImg(cameraScalePath);


        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    private void sendPicToImg(String cameraScalePath) {
        Log.d("0826",cameraScalePath);
        ByteArrayOutputStream baos=null;
        Bitmap photo= BitmapFactory.decodeFile(cameraScalePath);
        Log.d("0826","压缩前"+photo.getWidth()*photo.getHeight());
        if (photo==null){
            imageViewImg.setImageResource(R.mipmap.ic_yanwenziq);
        }else{
            try{
            baos=new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG,50,baos);
            byte[] photoData=baos.toByteArray();
            photo=BitmapFactory.decodeByteArray(photoData,0,photoData.length);
            Log.d("0826","----------------"+photoData.length);
//            Toast.makeText(ImageActivity.this,"photoData.toString()"+photoData.toString(),Toast.LENGTH_SHORT).show();
                Log.d("0826","压缩后   质量压缩 不会变"+photo.getWidth()*photo.getHeight());
            imageViewImg.setImageBitmap(photo);
            textViewImg.setText(cameraScalePath);
        }catch (Exception ex){
            Toast.makeText(ImageActivity.this, "exception", Toast.LENGTH_SHORT).show();

        }finally {
            if (baos!=null){
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        }

    }

    /**
     * 启动裁剪
     *
     * @param activity       上下文
     * @param file 需要裁剪图片的绝对路径
     * @param requestCode    比如：UCrop.REQUEST_CROP
     * @param aspectRatioX   裁剪图片宽高比
     * @param aspectRatioY   裁剪图片宽高比
     * @return
     */
    public static String startUCrop(Activity activity, File file,
                                    int requestCode, float aspectRatioX, float aspectRatioY) {
        Uri sourceUri = Uri.fromFile(file);
        File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        File outFile = new File(outDir, System.currentTimeMillis() + ".jpg");
        //裁剪后图片的绝对路径
        String cameraScalePath = outFile.getAbsolutePath();
        Uri destinationUri = Uri.fromFile(outFile);
        //初始化，第一个参数：需要裁剪的图片；第二个参数：裁剪后图片
        UCrop uCrop = UCrop.of(sourceUri, destinationUri);
        //初始化UCrop配置
        UCrop.Options options = new UCrop.Options();
        //设置裁剪图片可操作的手势
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        //是否隐藏底部容器，默认显示
        options.setHideBottomControls(true);
        //设置toolbar颜色
        options.setToolbarColor(Color.GREEN);
        //设置状态栏颜色
        options.setStatusBarColor(ActivityCompat.getColor(activity, R.color.colorPrimary));
        //是否能调整裁剪框
        options.setFreeStyleCropEnabled(true);
        //UCrop配置
        uCrop.withOptions(options);
        //设置裁剪图片的宽高比，比如16：9
        uCrop.withAspectRatio(aspectRatioX, aspectRatioY);
        uCrop.useSourceImageAspectRatio();
        //跳转裁剪页面
        uCrop.start(activity, requestCode);
        return cameraScalePath;
    }

    @OnClick({R.id.button_img, R.id.imageView_img, R.id.textView_img})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_img:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//调用相机
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));//指定相机拍照后的存储路径
                startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
//                startUCrop()

                break;
            case R.id.imageView_img:
                break;
            case R.id.textView_img:
                break;
        }
    }
}

//    void setToolbarTitle(@Nullable String text)
//    //设置裁剪的图片格式
//    void setCompressionFormat(@NonNull Bitmap.CompressFormat format)
//    //设置裁剪的图片质量，取值0-100
//    void setCompressionQuality(@IntRange(from = 0) int compressQuality)
//    //设置最多缩放的比例尺
//    void setMaxScaleMultiplier(@FloatRange(from = 1.0, fromInclusive = false) float maxScaleMultiplier)
//    //动画时间
//    void setImageToCropBoundsAnimDuration(@IntRange(from = 100) int durationMillis)
//    //设置图片压缩最大值
//    void setMaxBitmapSize(@IntRange(from = 100) int maxBitmapSize)
//    //是否显示椭圆裁剪框阴影
//    void setOvalDimmedLayer(boolean isOval)
//    //设置椭圆裁剪框阴影颜色
//    void setDimmedLayerColor(@ColorInt int color)
//    //是否显示裁剪框
//    void setShowCropFrame(boolean show)
//    //设置裁剪框边的宽度
//    void setCropFrameStrokeWidth(@IntRange(from = 0) int width)
//    //是否显示裁剪框网格
//    void setShowCropGrid(boolean show)
//    //设置裁剪框网格颜色
//    void setCropGridColor(@ColorInt int color)
//    //设置裁剪框网格宽
//    void setCropGridStrokeWidth(@IntRange(from = 0) int width)