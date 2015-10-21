package com.resea.androtelebot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraActivity extends Activity {

	 private static final String TAG = "MU";
     Camera camera;
     Preview preview;
     static final int MEDIA_TYPE_IMAGE = 1;
 	static File imgFile = null;
 	static Context context;
 	int timer;
     /** Called when the activity is first created. */
     @Override


 public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.camera);

         preview = new Preview(this);
         context = this;
         timer = getIntent().getExtras().getInt("time",4000);
         ((FrameLayout) findViewById(R.id.prev)).addView(preview);
         
         new java.util.Timer().schedule( 
                 new java.util.TimerTask() {
                     @Override
                     public void run() {
                         // your code here
                          preview.camera.takePicture(shutterCallback,rawCallback,jpegCallback);
                     }
                 }, 
                 timer 
         );


         //preview.camera.takePicture(shutterCallback,rawCallback,jpegCallback);       
//If I write above code here it shows error.


         Log.d(TAG, "onCreate'd");
     }


     ShutterCallback shutterCallback = new ShutterCallback() 
     {
         public void onShutter() 
         {
             Log.d(TAG, "onShutter'd");
         }
     };

     /** Handles data for raw picture */
     PictureCallback rawCallback = new PictureCallback() 
     {
         public void onPictureTaken(byte[] data, Camera camera) 
         {
             Log.d(TAG, "onPictureTaken - raw");
         }
     };
     
     private static File getOutputMediaFile(int type) {

 		Boolean isSDPresent = android.os.Environment.getExternalStorageState()
 				.equals(android.os.Environment.MEDIA_MOUNTED);
 		if (!isSDPresent) {
 			int duration = Toast.LENGTH_LONG;

 			Toast toast = Toast.makeText(context, "card not mounted", duration);
 			toast.show();

 			Log.d("MU", "Card not mounted");
 		}
 		File mediaStorageDir = new File(Environment
 				.getExternalStorageDirectory().getPath()
 				+ "/"+Environment.DIRECTORY_PICTURES+"/Androbot/");

 		if (!mediaStorageDir.exists()) {
 			if (!mediaStorageDir.mkdirs()) {

 				Log.d("MU", "failed to create directory");
 				return null;
 			}
 		}

 		// Create a media file name
 		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss")
 				.format(new Date());
 		File mediaFile;
 		if (type == MEDIA_TYPE_IMAGE) {
 			mediaFile = new File(mediaStorageDir.getPath() + File.separator
 					+ "Andro-" + timeStamp + ".jpg");
 			imgFile = mediaFile;
 		} else {
 			return null;
 		}

 		return mediaFile;
 	}

     /** Handles data for jpeg picture */
     PictureCallback jpegCallback = new PictureCallback() 
     {
         public void onPictureTaken(byte[] data, Camera camera) 
         {
             FileOutputStream outStream = null;
             File picFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
				if (picFile == null) {
					Log.d("MU", "permission acces sdcard denied");
				}
             try
             {
                 // write to local sandbox file system
                 // outStream = CameraDemo.this.openFileOutput(String.format("%d.jpg", System.currentTimeMillis()), 0);  
                 // Or write to sdcard
                 outStream = new FileOutputStream(picFile);  
                 outStream.write(data);
                 outStream.close();
                 Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
             }
             catch (FileNotFoundException e) 
             {
                 e.printStackTrace();
             }
             catch (IOException e) 
             {
                 e.printStackTrace();
             }
             finally 
             {
             }
             Log.d(TAG, "onPictureTaken - jpeg");
             CameraActivity.this.finish();
         }
     };

 }

