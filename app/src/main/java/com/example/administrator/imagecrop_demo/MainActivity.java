package com.example.administrator.imagecrop_demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.button)
    Button button;
    @Bind(R.id.imageView)
    ImageView imageView;
    @Bind(R.id.textView)
    TextView textView;
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果

    File tempFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        tempFile=new File(Environment.getExternalStorageDirectory(),getPhotoFileName());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PHOTO_REQUEST_TAKEPHOTO://拍照
                startPhotoZoom(Uri.fromFile(tempFile));
                break;
            case PHOTO_REQUEST_GALLERY://选择本地获取图片时
                if (data!=null){
                    startPhotoZoom(data.getData());
                }
                break;
            case PHOTO_REQUEST_CUT://返回结果
                if (data!=null){
                    sentPicToNext(data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
/**将裁剪后的图片传递到下一个界面上**/
    private void sentPicToNext(Intent data) {
        Bundle bundle=data.getExtras();
        if (bundle!=null){
            Bitmap photo=bundle.getParcelable("data");
            if (photo==null){
                imageView.setImageResource(R.mipmap.ic_yanwenziq);
            }else{
                imageView.setImageBitmap(photo);
                textView.setText(tempFile.getAbsolutePath());//图片绝对路径和名字
            }
            ByteArrayOutputStream baos=null;
           try{

               baos=new ByteArrayOutputStream();
               photo.compress(Bitmap.CompressFormat.JPEG,80,baos);
               byte[] photoData=baos.toByteArray();
               Log.d("0826",photoData.toString());
//            Intent intent=new Intent(MainActivity.class,ShowActivity.class);
//            intent.putExtra("photo",photoData);
//            startActivity(intent);
//            finish();
           } catch (Exception ex){
               Toast.makeText(MainActivity.this,"exception",Toast.LENGTH_SHORT).show();

           }finally {
               if (baos!=null){
                   try {
                       baos.close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }
//测试


        }
    }

    private void startPhotoZoom(Uri uri) {
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.putExtra("crop",true);//可裁剪
        intent.putExtra("aspectX",1);//宽高比
        intent.putExtra("aspectY",1);
        //裁剪图片的宽高
        intent.putExtra("outputX",300);
        intent.putExtra("outputY",300);
        intent.putExtra("return-data",true );
        intent.putExtra("noFaceDetection",true);
        startActivityForResult(intent,PHOTO_REQUEST_CUT);


    }

    private String getPhotoFileName() {
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat=new SimpleDateFormat("'Img'_yyyyMMdd_HHmmss");

        return dateFormat.format(date)+".jpg";
    }

    @OnClick({R.id.button, R.id.imageView, R.id.textView})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//调用相机
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));//指定相机拍照后的存储路径
                startActivityForResult(intent,PHOTO_REQUEST_TAKEPHOTO);
                break;
            case R.id.imageView:
                break;
            case R.id.textView:
                break;
        }
    }
}
