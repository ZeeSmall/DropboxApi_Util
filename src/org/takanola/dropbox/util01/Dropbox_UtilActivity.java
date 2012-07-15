package org.takanola.dropbox.util01;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Dropbox_UtilActivity extends Activity {
    /** Called when the activity is first created. */
	private static final String TAG = "DropboxUtilActivity";
	DropboxUtils util;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // TextView生成
        TextView console = (TextView)findViewById(R.id.console);
        // Dropbox接続クラスを生成
        util = new DropboxUtils(this);
        
        // Dropboxと接続
        //util.ConnectDropbox();
        // Dropboxとの接続を解除
        //util.DisconnectionDropbox();
        
        // Dropboxからのダウンロードクラス（非同期）を生成
        DownloadItem item = new DownloadItem(this, util.getDropboxApi(), "one.txt", console);
        // 非同期処理を開始
        item.execute();
    }

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume");
		// 認証画面から帰ってきたときに情報を記録する
		util.ResumeAuthentication(util.getDropboxApi().getSession());
		
	}
    
}