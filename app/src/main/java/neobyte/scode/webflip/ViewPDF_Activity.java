package neobyte.scode.webflip;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import neobyte.scode.webflip.tools.PrefUtil;


public class ViewPDF_Activity extends AppCompatActivity {

    private final String UA_Chrome = "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [WebView Android/v2.0]";
    private String pathurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        if(savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            pathurl = bundle.getString("pathurl");
        }
        else
        {
            pathurl = savedInstanceState.getString("pathurl");
        }
        if(pathurl!=null) {
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.getSettings().setAllowFileAccessFromFileURLs(true);
            myWebView.getSettings().setAllowContentAccess(true);
            myWebView.getSettings().setAppCacheEnabled(true);
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            myWebView.getSettings().setUserAgentString(UA_Chrome);
            myWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            myWebView.getSettings().setJavaScriptEnabled(true);
            myWebView.getSettings().setSupportZoom(true);
            myWebView.getSettings().setBuiltInZoomControls(true);
            myWebView.getSettings().setDisplayZoomControls(true);
            myWebView.loadUrl(pathurl);
        }
        else {
            Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("pathurl",pathurl);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        pathurl = savedInstanceState.getString("pathurl");
    }
}