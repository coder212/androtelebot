package com.resea.androtelebot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraService extends Service {

	static final int MEDIA_TYPE_IMAGE = 1;
	static File imgFile = null;
	static Context context;
	Camera camera;

	@SuppressWarnings("deprecation")
	private void CaptureImage() {
		final SurfaceView preview = new SurfaceView(context);
		SurfaceHolder holder = preview.getHolder();
		// deprecated setting, but required on Android versions prior to 3.0
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		holder.addCallback(new Callback() {
			@Override
			// The preview must happen at or after this point or takePicture
			// fails
			public void surfaceCreated(SurfaceHolder holder) {
				showMessage("Surface created");

				//camera = null;

				try {
					camera = Camera.open();
					showMessage("Opened camera");

					try {
						camera.setPreviewDisplay(holder);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

					camera.startPreview();
					showMessage("Started preview");
					 new java.util.Timer().schedule( 
				                new java.util.TimerTask() {
				                    @Override
				                    public void run() {
				                        // your code here
				                    	camera.takePicture(null, null, new PictureCallback() {

				    						@Override
				    						public void onPictureTaken(byte[] data, Camera camera) {
				    							showMessage("Took picture");
				    							File picFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
				    							if (picFile == null) {
				    								Log.d("MU", "permission acces sdcard denied");
				    							}
				    							try {
				    								FileOutputStream fos = new FileOutputStream(picFile);
				    								fos.write(data);
				    								fos.close();
				    							} catch (FileNotFoundException e) {
				    								e.printStackTrace();
				    							} catch (IOException e) {
				    								e.printStackTrace();
				    							}
				    							camera.release();
				    						}
				    					});
				                    }
				                }, 
				                2000 
				        );

					
				} catch (Exception e) {
					if (camera != null)
						camera.release();
					throw new RuntimeException(e);
				}
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
		});

		//WindowManager wm = (WindowManager) context
		//		.getSystemService(Context.WINDOW_SERVICE);
		//WindowManager.LayoutParams params = new WindowManager.LayoutParams(1,
		//		1, // Must be at least 1x1
		//		WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0,
				// Don't know if this is a safe default
		//		PixelFormat.UNKNOWN);

		// Don't set the preview visibility to GONE or INVISIBLE
		//wm.addView(preview, params);
	}

	private static void showMessage(String message) {
		Log.i("Camera", message);
	}

	private static File getOutputMediaFile(int type) {

		Boolean isSDPresent = android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED);
		if (!isSDPresent) {
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, "card not mounted", duration);
			toast.show();

			Log.d("ERROR", "Card not mounted");
		}
		File mediaStorageDir = new File(Environment
				.getExternalStorageDirectory().getPath()
				+ Environment.DIRECTORY_PICTURES+"Androbot/");

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {

				Log.d("MyCameraApp", "failed to create directory");
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

	@Override
	public IBinder onBind(Intent arg0) {
		// TODOs Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODOs Auto-generated method stub
		context = CameraService.this;
		CaptureImage();
		return (START_STICKY);
	}

}
