package com.example.activitypackage;

import com.example.pengpengcheungtest.AccessTokenKeeper;
import com.example.pengpengcheungtest.Constants;
import com.example.pengpengcheungtest.R;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,
		IWeiboHandler.Response {
	private IWeiboShareAPI mWeiboShareAPI;
	private Button button;
	public static final int SHARE_CLIENT = 1;
	public static final int SHARE_ALL_IN_ONE = 2;
	private int mShareType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mShareType = 2;
		button = (Button) this.findViewById(R.id.share);
		button.setOnClickListener(this);
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);
		mWeiboShareAPI.registerApp(); // 将应用注册到微博客户端

	}

	private TextObject getTextObj() {
		TextObject textObject = new TextObject();
		textObject.text = "This is a Weibo Message pengpeng send!";
		return textObject;
	}

	private void sendMultiMessage(boolean hasText, boolean hasImage,
			boolean hasWebpage, boolean hasMusic, boolean hasVideo,
			boolean hasVoice) {
		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();// 初始化微博的分享消息
		if (hasText) {
			weiboMessage.textObject = getTextObj();
		}
		SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.multiMessage = weiboMessage;
		// mWeiboShareAPI.sendRequest(MainActivity.this,request);
		// //发送请求消息到微博,唤起微博分享界面
		if (mShareType == SHARE_CLIENT) {
			mWeiboShareAPI.sendRequest(MainActivity.this, request);
		} else if (mShareType == SHARE_ALL_IN_ONE) {
			AuthInfo authInfo = new AuthInfo(this, Constants.APP_KEY,
					Constants.REDIRECT_URL, Constants.SCOPE);
			Oauth2AccessToken accessToken = AccessTokenKeeper
					.readAccessToken(getApplicationContext());
			String token = "";
			if (accessToken != null) {
				token = accessToken.getToken();
			}
			mWeiboShareAPI.sendRequest(this, request, authInfo, token,
					new WeiboAuthListener() {

						@Override
						public void onWeiboException(WeiboException arg0) {
						}

						@Override
						public void onComplete(Bundle bundle) {
							// TODO Auto-generated method stub
							Oauth2AccessToken newToken = Oauth2AccessToken
									.parseAccessToken(bundle);
							AccessTokenKeeper.writeAccessToken(
									getApplicationContext(), newToken);
							Toast.makeText(
									getApplicationContext(),
									"onAuthorizeComplete token = "
											+ newToken.getToken(), 0).show();
						}

						@Override
						public void onCancel() {
						}
					});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mWeiboShareAPI.handleWeiboResponse(intent, this); // 当前应用唤起微博分享后,返回当前应用
	}

	@Override
	public void onResponse(BaseResponse baseResp) {
		// TODO Auto-generated method stub
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			Toast.makeText(this, "share success", Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:
			Toast.makeText(this, "share canceled", Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			Toast.makeText(this,
					"share failed" + "Error Message: " + baseResp.errMsg,
					Toast.LENGTH_LONG).show();
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		sendMultiMessage(true, false, false, false, false, false);
		Log.i("Main", "finish");
	}

}
