package neobyte.scode.webflip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import neobyte.scode.webflip.tools.Constants;
import neobyte.scode.webflip.tools.NetworkChangeReceiver;
import neobyte.scode.webflip.tools.PrefUtil;

public class HomeActivity extends AppCompatActivity implements NetworkChangeReceiver.ConnectivityReceiverListener{

    private AdView adBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        checkConnection();

        adBanner = (AdView)findViewById(R.id.adViewBanner);
        requestBanner();

        if( Constants.APPLICATION_ID.equals( "" ) || Constants.SECRET_KEY.equals( "" ) || Constants.VERSION.equals( "" ) )
        {
            showAlert( this, "Missing application ID and secret key arguments. Login to Backendless Console, select your app and get the ID and key from the Manage - App Settings screen. Copy/paste the values into the Backendless.initApp call" );
            return;
        }
        Backendless.setUrl( Constants.SERVER_URL );
        Backendless.initApp( this, Constants.APPLICATION_ID, Constants.SECRET_KEY, Constants.VERSION );
        ImageView header_img = (ImageView)findViewById(R.id.img_title_home);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.ic_book_light_blue_500_48dp);
        Glide.with(this)
                .load(R.drawable.ic_open_book)
                .asBitmap()
                .fitCenter()
                .into(header_img);
        Button btn_all = (Button)findViewById(R.id.btn_all_book);
        Log.d("nilai last ",String.valueOf(PrefUtil.getPrefLong(this,"last_id")));
        btn_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),CategoryActivity.class);
                startActivity(i);
            }
        });
    }

    /*private void requestInterstitial(){
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        interstitialAd.loadAd(adRequest);
    }*/

    private void requestBanner(){
        AdRequest.Builder adRequest = new AdRequest.Builder();
        adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        adRequest.addTestDevice("AB65D4D610C6856E00BE2BB6B4D907D2");
        AdRequest request = adRequest.build();
        adBanner.loadAd(request);
    }

    @Override
    public void onNetworkConnectionChanged(boolean b){
        showSnack(b);
    }

    @Override
    protected void onResume(){
        super.onResume();
        MyPDFApp.getInstance().setConnectivityListener(this);
        if(adBanner!=null){
            adBanner.resume();
        }
    }

    @Override
    protected void onPause(){
        if(adBanner!=null){
            adBanner.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        if(adBanner!=null){
            adBanner.destroy();
        }
        super.onDestroy();
    }

    private void checkConnection() {
        boolean isConnected = NetworkChangeReceiver.isConnected();
        if(!isConnected){
            showSnack(false);
        }
    }

    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = getString(R.string.connected);
            color = Color.WHITE;
        } else {
            message = getString(R.string.no_conn);
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.activity_home), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    public static void showAlert(final Activity context, String message )
    {
        new AlertDialog.Builder( context ).setTitle( "An error occurred" ).setMessage( message ).setPositiveButton( "OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialogInterface, int i )
            {
                context.finish();
            }
        } ).show();
    }
}
