package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by ne250214 on 15/12/08.
 */
public class MyWebView extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent i = getIntent();
        String url = i.getStringExtra("url");
        //レイアウトで指定したWebViewのIDを指定する。
        WebView myWebView = (WebView) findViewById(R.id.webView);
        //リンクをタップしたときに標準ブラウザを起動させない
        myWebView.setWebViewClient(new WebViewClient());
        //最初にYahoo! Japanのページを表示する。
        myWebView.loadUrl(url);
    }
}
