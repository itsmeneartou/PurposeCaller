package com.purposecaller.purposecaller.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.models.Message;
import com.purposecaller.purposecaller.multibrowsing.MultiBrowsingData;
import com.purposecaller.purposecaller.multibrowsing.MultiBrowsingDataValueEventListener;

import static com.purposecaller.purposecaller.MyAuthStateListener.getPhotoUrl;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MultiBrowsingActivity extends PurposeActivity {


    private WebView webView;
    private EditText urlBox;
   public MultiBrowsingData data;
public String TAG="MultiBrowsingActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multi_browsing);

        gameStateTextView=findViewById(R.id.game_state_text_view);


        webView = findViewById(R.id.web_view);

        urlBox=findViewById(R.id.url_box);

        gameDataValueEventListener=new MultiBrowsingDataValueEventListener(webView,this);
        doCommonPurposeWork();
        urlBox.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String enteredUrl=v.getText().toString();
                 if(Patterns.WEB_URL.matcher(enteredUrl).matches()){
                     enteredUrl=enteredUrl.startsWith("https://")?enteredUrl:enteredUrl.startsWith("http://")?enteredUrl:("https://"+enteredUrl);
                     webView.loadUrl(enteredUrl);
                 }
                 else{
                    enteredUrl="https://www.google.com/search?q="+enteredUrl;
                     webView.loadUrl(enteredUrl);
                 }



                    return true;
                }
                return false;
            }
        });


       webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                if(!data.url.equals(url) && gameDataRef!=null){

                  writeToDatabase(url);
                }


                urlBox.setText(url);


            }


        });



    }

    public void writeToDatabase(String url){
        data=new MultiBrowsingData(url,uid);
        gameDataRef.setValue(data);
        if(chatFragment!=null){
            chatFragment.sendAMessage(new Message("Automatic Message: I changed the url",uid,getPhotoUrl()));
        }

    }









}
