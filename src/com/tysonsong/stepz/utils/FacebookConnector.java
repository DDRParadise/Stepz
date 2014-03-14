package com.tysonsong.stepz.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class FacebookConnector {
	private Facebook facebook;
	private Context context;
	private String[] permissions;
	private Handler mHandler;
	private Activity activity;
	private AsyncFacebookRunner mAsyncRunner;
	private JSONObject userdata;
	
	public FacebookConnector(String appId,Activity activity,Context context,String[] permissions) {
		this.facebook = new Facebook(appId);
	    this.context=context;
	    this.permissions=permissions;
	    this.mHandler = new Handler();
	    this.activity=activity;
	    this.mAsyncRunner = new AsyncFacebookRunner(facebook);
	}
	
	public void postMessageOnWall(String msg, RequestListener requestListener) {
	    if (facebook.isSessionValid()) {
	        Bundle parameters = new Bundle();
	        parameters.putString("message", msg);
            mAsyncRunner.request("me/feed", parameters, 
            		"POST", requestListener, null);
	    } else {
	        Toast.makeText(context, "Invalid Facebook session", Toast.LENGTH_SHORT);
	    }
	}

	public void login(DialogListener dialogListener) {
	    if (!facebook.isSessionValid()) {
	        facebook.authorize(this.activity, this.permissions,Facebook.FORCE_DIALOG_AUTH, dialogListener);
	    }
	}
	
	public void logout(RequestListener requestListener)
	{
		mAsyncRunner.logout(context, requestListener);
	}
	
	public void getUserData(){
		if (facebook.isSessionValid()) {
	        try {
				try {
					userdata = Util.parseJson(facebook.request("me"));
				} catch (FacebookError e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	}

	public String getUserEmail() {
		try {
			return userdata.getString("email");
		} catch (JSONException e) {
			e.printStackTrace();
			return "!!EMAIL_ERROR!!";
		}
	}
}
