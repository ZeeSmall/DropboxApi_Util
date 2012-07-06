package org.takanola.dropbox.util01;

import android.app.Activity;
import android.os.Bundle;

public class Dropbox_UtilActivity extends Activity {
    /** Called when the activity is first created. */
	private static final String TAG = "DropboxUtilActivity";
	DropboxUtils util;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        util = new DropboxUtils(this);
        util.ConnectorDisConnectDropbox();
    }

	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();
		
		util.ResumeAuth(util.getDropboxApi().getSession());
	}
    
}