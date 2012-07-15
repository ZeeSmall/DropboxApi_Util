package org.takanola.dropbox.util01;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class DropboxUtils {
	// ログ出力用
	private static final String TAG = "DropboxUtils";
	// DropboxのAppkeyとSecret
	final static private String APP_KEY = "05tliddqm84xxau";
	final static private String APP_SECRET = "vrznqmwofyrb8kq";
	// アプリのアクセスタイプ
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	// Preferenceの設定
	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	// 呼び出し元のコンテキスト保持変数
	private Context ct;
	// DropboxのAPI
	private DropboxAPI<AndroidAuthSession> mApi;

	
	/**
	 * コンストラクタ．
	 * 
	 * @param context
	 */
	public DropboxUtils(Context context){
		// コンテキストを保持
		ct = context;
		// APIのセットアップ
		SetUpAPI();
	}

	/**
	 * APIの接続状態を返す．
	 * 
	 * @return
	 */
	public boolean hasLinked(){
		return mApi.getSession().isLinked();
	}
	
	/**
	 * Dropboxと接続を行う．<br>
	 * すでに接続している場合は接続を解除する．
	 * 
	 */
	public void ConnectorDisConnectDropbox(){
		if(mApi.getSession().isLinked()){
			mApi.getSession().unlink();
			// 接続情報をクリア
			clearKeys();
		}else{
			 mApi.getSession().startAuthentication(ct);
		}
	}
	
	/**
	 * Dropboxとの接続を行う．
	 * 
	 */
	public void ConnectDropbox(){
		mApi.getSession().startAuthentication(ct);
	}
	
	/**
	 * Dropboxとの接続を解除する．
	 * 
	 */
	public void DisconnectionDropbox(){
		mApi.getSession().unlink();
	}
	
	/**
	 * APIの生成とAppkeyのチェックをおこなう．
	 * 
	 */
	private void SetUpAPI(){
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		checkAppKeySetup();
	}
	
	/**
	 * 認証結果を受け取り記録する．
	 * 
	 */
	public void ResumeAuthentication(AndroidAuthSession session){
		if (session.authenticationSuccessful()) {
			Log.d(TAG,"ResumeAuth start");
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();
				
				// Store it locally in our app for later use
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
			} catch (IllegalStateException e) {
				showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
				Log.i(TAG, "Error authenticating", e);
			}
			Log.d(TAG,"ResumeAuth done.");
		}
	}
	
	/**
	 * AppKeyのセットアップをおこなう．
	 * 
	 */
	private void checkAppKeySetup() {
		// Check to make sure that we have a valid app key
		if (APP_KEY.startsWith("CHANGE") ||
				APP_SECRET.startsWith("CHANGE")) {
			showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
		}

		// Check if the app has set up its manifest properly.
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = ct.getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
			showToast("URL scheme in your app's " +
					"manifest is not set up correctly. You should have a " +
					"com.dropbox.client2.android.AuthActivity with the " +
					"scheme: " + scheme);
		}
	}

	/**
	 * DropboxのAPIを返す．
	 * 
	 * @return maApi DropboxAPI
	 */
	public DropboxAPI<AndroidAuthSession> getDropboxApi(){
		return mApi;
	}
	
	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local
	 * store, rather than storing user name & password, and re-authenticating each
	 * time (which is not to be done, ever).
	 *
	 * @return Array of [access_key, access_secret], or null if none stored
	 */
	private String[] getKeys() {
		SharedPreferences prefs = ct.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local
	 * store, rather than storing user name & password, and re-authenticating each
	 * time (which is not to be done, ever).
	 */
	private void storeKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = ct.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	/**
	 * Preferenceの内容を消去
	 * 
	 */
	private void clearKeys() {
		SharedPreferences prefs = ct.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	
	/**
	 * AndroidAuthSessionを生成する．
	 * 
	 * @return
	 */
	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		return session;
	}
	
	private void showToast(String msg) {
		Toast error = Toast.makeText(ct, msg, Toast.LENGTH_LONG);
		error.show();
	}

}
