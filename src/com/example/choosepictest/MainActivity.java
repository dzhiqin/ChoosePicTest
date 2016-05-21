package com.example.choosepictest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public final static int TAKE_PHOTO=1;
	public final static int CROP_PHOTO=2;
	public final static int CHOOSE_PHOTO=3;
	private Button takePicBtn;
	private Button choosePicBtn;
	private ImageView picture;
	private Uri imageUri;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		takePicBtn=(Button)findViewById(R.id.takePicBtn);
		choosePicBtn=(Button)findViewById(R.id.choosePicBtn);
		picture=(ImageView)findViewById(R.id.picture);
		takePicBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// ����File�������ڴ洢���պ����Ƭ
				File outputImage=new File(Environment.getExternalStorageDirectory(),"output_image.jpg");
				try{
					if(outputImage.exists()){
						outputImage.delete();
					}
					outputImage.createNewFile();
				}catch(IOException e){
					e.printStackTrace();
				}
				//��outputImageת����Uri������������ʶ��output_image.jpg����ͼƬ��Ψһ��ַ
				imageUri=Uri.fromFile(outputImage);
				Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				//�����������
				startActivityForResult(intent,TAKE_PHOTO);
			}
		});
		
		choosePicBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// �������
				Intent intent=new Intent("android.intent.action.GET_CONTENT");
				intent.setType("image/*");
				startActivityForResult(intent,CHOOSE_PHOTO);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		switch(requestCode){
		case TAKE_PHOTO:
			if(resultCode==RESULT_OK){
				Intent intent=new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(imageUri, "image/*");
				intent.putExtra("scale", true);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent,CROP_PHOTO);//�������ó���
			}
			break;
		case CROP_PHOTO:
			if(resultCode==RESULT_OK){
				try{
					Bitmap bitmap=BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
					picture.setImageBitmap(bitmap);
				}catch(FileNotFoundException e){
					e.printStackTrace();
				}
				
			}
			break;
		case CHOOSE_PHOTO:
			if(resultCode==RESULT_OK){
				//�ж��ֻ��汾��
				if(Build.VERSION.SDK_INT>=19){
					//android4.4������ϵͳʹ�������������ͼƬ
					handleImageOnKitKat(data);
				}else{
					handleImageBeforeKitKat(data);
				}
			}
			break;
		default:
			break;
		}
	}

	private void handleImageBeforeKitKat(Intent data) {
		// TODO �Զ����ɵķ������
		Uri uri=data.getData();
		String imagePath=getImagePath(uri,null);
		displayImage(imagePath);
	}

	private void displayImage(String imagePath) {
		// TODO �Զ����ɵķ������
		if(imagePath!=null){
			Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
			picture.setImageBitmap(bitmap);					
		}else{
			Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
		}
		
	}

	private String getImagePath(Uri  uri, String selection) {
		// ͨ��uri��selection����ȡ���ǵ�ͼƬ·��
		String path=null;
		Cursor cursor=getContentResolver().query(uri, null, selection, null, null);
		if(cursor!=null){
			if(cursor.moveToFirst()){
				path=cursor.getString(cursor.getColumnIndex(Media.DATA));
			}
			cursor.close();
		}
		return path;
	}

	@SuppressLint("NewApi")
	private void handleImageOnKitKat(Intent data) {
		// TODO �Զ����ɵķ���		
		String imagePath=null;
		Uri uri=data.getData();
		if(DocumentsContract.isDocumentUri(this, uri)){
			//�����Document���͵�uri����ͨ��document id����
			String docId=DocumentsContract.getDocumentId(uri);
			if("com.android.providers.media.documents".equals(uri.getAuthority())){
				String id=docId.split(":")[1];
				String selection=MediaStore.Images.Media._ID+"="+id;
				imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
			}else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
				Uri contentUri=ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
				imagePath=getImagePath(contentUri,null);
			}else if("content".equalsIgnoreCase(uri.getScheme())){
				//�������document���͵�uri����ʹ����ͨ��ʽ����
				imagePath=getImagePath(uri,null);
			}
			displayImage(imagePath);//����ͼƬ·����ʾͼƬ
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
