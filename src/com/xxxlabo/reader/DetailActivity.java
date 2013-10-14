package com.xxxlabo.reader;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.xxxlabo.reader.R;

public class DetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WebView webview = new WebView(this);

		// リンククリックしても標準ブラウザを起動しないようにwebviewclientを設定
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}
		});  

		setContentView(webview);
		Intent i = getIntent();

		// titleをブログエントリタイトルにする
//		String title = i.getStringExtra("title");
		// titleをブログタイトルにする
		String title = getString(R.string.blog_name);
		this.setTitle(title);
		
		String url = i.getStringExtra("url");
		webview.loadUrl(url);
	}
}
