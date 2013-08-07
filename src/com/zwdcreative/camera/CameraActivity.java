package com.zwdcreative.camera;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.cam.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";

	private CameraPreview mCameraPreview;
	private LinearLayout mTakeCameraControls, mUseCameraControls;
	private Button mTakeButton, mAcceptButton, mRetakeButton;
	private Camera mCamera;
	private byte[] mPhotoData;
	private String fileName;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		// Check for a camera
		if(!checkCameraHardware(this)) {
			finish();
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Set main layout
		setContentView(R.layout.main);

		// Setup camera surface
		mCameraPreview = new CameraPreview(this,
				(SurfaceView) findViewById(R.id.surfaceView));
		mCameraPreview.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((FrameLayout) findViewById(R.id.preview)).addView(mCameraPreview);
		mCameraPreview.setKeepScreenOn(true);

		// Setup UI controls
		mTakeCameraControls = (LinearLayout) findViewById(R.id.camera_take_controls);
		mUseCameraControls = (LinearLayout) findViewById(R.id.camera_use_controls);
		mTakeButton = (Button) findViewById(R.id.camera_take);
		mAcceptButton = (Button) findViewById(R.id.camera_accept);
		mRetakeButton = (Button) findViewById(R.id.camera_reject);

		// Setup event listeners
		mTakeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
				showCameraControls(false);
			}
		});

		mRetakeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				resetCam();
				showCameraControls(true);
			}
		});

		mAcceptButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				FileOutputStream outStream = null;
				try {

					fileName = System.currentTimeMillis() + ".jpg";
					outStream = CameraActivity.this.openFileOutput(fileName,
							Context.MODE_PRIVATE);
					outStream.write(mPhotoData);
					outStream.close();
					Log.i(TAG, "onPictureTaken - wrote bytes: "
							+ mPhotoData.length);

					// Set the result and intent data
					Intent intent = new Intent();
					intent.getExtras().putString("filename", fileName);
					setResult(Activity.RESULT_OK, intent);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
					setResult(Activity.RESULT_CANCELED);
				} catch (IOException e) {
					e.printStackTrace();
					setResult(Activity.RESULT_CANCELED);
				} finally {
					// Return from activity
					finish();
				}
			}
		});

		mCameraPreview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCamera.autoFocus(new AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {

					}
				});
			}
		});

		mTakeButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				mCamera.autoFocus(new AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						if (success) {
							camera.takePicture(shutterCallback, rawCallback,
									jpegCallback);
						}
					}
				});
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCamera = Camera.open();
		mCamera.startPreview();
		mCamera.setDisplayOrientation(90);
		mCameraPreview.setCamera(mCamera);
	}

	@Override
	protected void onPause() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCameraPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
		super.onPause();
	}

	private void resetCam() {
		mCamera.startPreview();
		mCameraPreview.setCamera(mCamera);
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			// Log.d(TAG, "onShutter'd");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// Log.d(TAG, "onPictureTaken - raw");
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {

			mPhotoData = data;
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};

	private void showCameraControls(boolean show) {
		if (show) {
			mTakeCameraControls.setVisibility(View.VISIBLE);
			mUseCameraControls.setVisibility(View.GONE);
		} else {
			mTakeCameraControls.setVisibility(View.GONE);
			mUseCameraControls.setVisibility(View.VISIBLE);
		}
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}
}
