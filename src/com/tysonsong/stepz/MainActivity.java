package com.tysonsong.stepz;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.fusiontables.ftclient.ClientLogin;
import com.google.fusiontables.ftclient.FTClient;
import com.tysonsong.stepz.utils.MyLocationListener;
import com.tysonsong.stepz.utils.FacebookConnector;

public class MainActivity extends Activity implements
	SensorEventListener {
    
	private FacebookConnector fbConnector;
	private final String APP_ID = "219676084800369";
	private final String[] PERMISSIONS = { "email", "publish_checkins" };
	private String userEmail;
	
	private float mLastX, mLastY;
	private int count;
	private boolean mInitialized;
	private boolean isCounting;

	private SensorManager mSensorManager;
	
	private MyLocationListener locationListener;
	private LocationManager locationManager;
	private String locationProvider = LocationManager.NETWORK_PROVIDER;
	private boolean locationAvailable;

	private FTClient ftclient;
	private long tableid = 3443207;
	private String username = "stepzdev354@gmail.com";
	private String password = "dubstepz";
	
	private final float THRESHOLD = (float) 8.0;
	private final float Z_THRESHOLD = (float) 3.0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        fbConnector = new FacebookConnector(APP_ID, this, getApplicationContext(), PERMISSIONS);
        fbConnector.login(new DialogListener() {
			public void onComplete(Bundle values) {
				
				fbConnector.getUserData();
				userEmail = fbConnector.getUserEmail();
				
				setContentView(R.layout.main);

				Toast.makeText(MainActivity.this, "Facebook Authenticated as " + userEmail, Toast.LENGTH_LONG).show();
	
				mInitialized = false;
				isCounting = false;
				mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				
				initLocation();
				
				String token = ClientLogin.authorize(username, password);
			    ftclient = new FTClient(token);
			}

			public void onFacebookError(FacebookError e) {}

			public void onError(DialogError e) {}

			public void onCancel() {}
			});
    }
    
    /* Button Events */
    
    public void startCounting(View v)
    {
    	isCounting = true;
    	mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
    	
    	Button start = (Button) MainActivity.this.findViewById(R.id.start);
		start.setVisibility(View.INVISIBLE);
    	
    	Button stop = (Button) MainActivity.this.findViewById(R.id.stop);
		stop.setVisibility(View.VISIBLE);
    }
    
    public void stopCounting(View v)
    {
    	isCounting = false;
    	mSensorManager.unregisterListener(this);
    	
    	StringBuilder insert = new StringBuilder();
        insert.append("INSERT INTO ");
        insert.append(tableid);
        insert.append(" (user, steps, location, dateCreated) VALUES ");
        insert.append("('");
        insert.append(userEmail);
        insert.append("', ");
        insert.append(count);
        insert.append(", '");
        insert.append(locationListener.getStringLocation());
        insert.append("', '");
        insert.append(new Date());
        insert.append("')");
       
        ftclient.query(insert.toString());
    	
    	Button stop = (Button) MainActivity.this.findViewById(R.id.stop);
		stop.setVisibility(View.INVISIBLE);
		
		Button reset = (Button) MainActivity.this.findViewById(R.id.reset);
		reset.setVisibility(View.VISIBLE);
		
		Button post = (Button) MainActivity.this.findViewById(R.id.post);
		post.setVisibility(View.VISIBLE);
    }
    
    public void postData(View v)
    {
    	Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
    }
    
    public void resetCounting(View v)
    {
    	mInitialized = false;
    	count = 0;
    	((TextView) findViewById(R.id.count)).setText("0");
    	
    	Button reset = (Button) MainActivity.this.findViewById(R.id.reset);
		reset.setVisibility(View.INVISIBLE);
		
		Button post = (Button) MainActivity.this.findViewById(R.id.post);
		post.setVisibility(View.INVISIBLE);
		
		Button start = (Button) MainActivity.this.findViewById(R.id.start);
		start.setVisibility(View.VISIBLE);
    }

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		TextView tvCount = (TextView) findViewById(R.id.count);
		
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			count = 0;
			tvCount.setText("0");
			mInitialized = true;
		} else {
			float deltaX = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);

			mLastX = x;
			mLastY = y;
			
			if (deltaX + deltaY > THRESHOLD && Math.abs(z) < Z_THRESHOLD)
			{
				count += 1;
			}
			tvCount.setText(Integer.toString(count));
		}
	}
	
	/**
	 * Initializes the location listener.
	 */
	private void initLocation() {
		locationAvailable = false;
		locationManager = (LocationManager) getSystemService(
				Context.LOCATION_SERVICE);
		Location lastKnownLocation = locationManager
				.getLastKnownLocation(locationProvider);

		locationListener = new MyLocationListener(this);
		if (lastKnownLocation != null) {
			locationListener.setLocation(lastKnownLocation);
		}

		requestUpdates();
	}

	/**
	 * Requests location updates.
	 */
	private void requestUpdates() {
		locationManager.requestLocationUpdates(locationProvider, 5000, 1,
				locationListener);
	}

	public void onFoundLocation(String address) {
		locationAvailable = true;
	}
	
//	protected void onResume() {
//		super.onResume();
//		mSensorManager.registerListener(this,
//				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//				SensorManager.SENSOR_DELAY_NORMAL);
//	}
//
//	protected void onPause() {
//		super.onPause();
//		mSensorManager.unregisterListener(this);
//	}
}