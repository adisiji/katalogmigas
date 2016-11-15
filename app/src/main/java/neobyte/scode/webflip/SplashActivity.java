package neobyte.scode.webflip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by neobyte on 11/3/2016.
 */

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 5;
    private ImageView splashImageView;
    private InterstitialAd interstitialAd;
    private Timer waitTimer;
    private Boolean interstitialCanceled=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        requestInterstitial();
        interstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                gotoHome();
            }

            @Override
            public void onAdLoaded() {
                if (!interstitialCanceled) {
                    waitTimer.cancel();
                    interstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                gotoHome();
            }
        });

        waitTimer = new Timer();
        waitTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                interstitialCanceled = true;
                SplashActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gotoHome();
                    }
                });
            }
        }, 6000);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        final RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.main_layout_splash);
        Glide.with(this).load(R.drawable.wallpaper_cover).asBitmap().into(new SimpleTarget<Bitmap>(width,height) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Drawable drawable = new BitmapDrawable(resource);
                relativeLayout.setBackground(drawable);
            }
        });
        splashImageView = (ImageView)findViewById(R.id.splash_img);
        Typeface face= Typeface.createFromAsset(getAssets(), "fonts/Panton-LightCaps.otf");
        TextView header = (TextView)findViewById(R.id.header_splash);
        if(face!=null){
            header.setTypeface(face);
        }
        Glide.with(this)
                .load(R.drawable.open_book)
                .asBitmap().fitCenter().into(splashImageView);

    }



    @Override
    protected void onResume() {
        //AppInitializer.getInstance().trackScreenView("Welcome Screen");
        super.onResume();
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else if (interstitialCanceled) {
            gotoHome();
        }
    }

    @Override
    public void onPause() {
        waitTimer.cancel();
        interstitialCanceled = true;
        super.onPause();
    }
    private void requestInterstitial(){
        AdRequest.Builder adRequest = new AdRequest.Builder();
        adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        adRequest.addTestDevice("AB65D4D610C6856E00BE2BB6B4D907D2");
        AdRequest request = adRequest.build();
        interstitialAd.loadAd(request);
    }

    private void gotoHome(){
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }


}
