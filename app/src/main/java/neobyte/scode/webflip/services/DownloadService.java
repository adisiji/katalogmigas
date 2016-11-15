package neobyte.scode.webflip.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import neobyte.scode.webflip.R;
import neobyte.scode.webflip.fragments.AlertDialogFragment;
import neobyte.scode.webflip.model.Download;
import neobyte.scode.webflip.tools.Constants;
import neobyte.scode.webflip.tools.RetrofitInterface;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

import static neobyte.scode.webflip.tools.Constants.CANCEL_DOWNLOAD;
import static neobyte.scode.webflip.tools.Constants.MESSAGE_FAIL_DOWNLOAD;
import static neobyte.scode.webflip.tools.Constants.MESSAGE_PROGRESS;

/**
 * Created by neobyte on 11/7/2016.
 */

public class DownloadService extends IntentService {

    public DownloadService(){
        super("Download PDF Service");
    }

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private double totalFileSize;
    private long id;
    private int pos;
    private boolean throwx = false;
    private static List<String> listUrl = new ArrayList<String>();

    public static void removeQdownload(String url){
        listUrl.add(url);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Bundle bundle = intent.getExtras();
        String url = bundle.getString("item_url");
        if(!listUrl.contains(url) || listUrl.isEmpty()){
            id = bundle.getLong("id");
            pos = bundle.getInt("position");
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_download)
                    .setContentTitle("Download Book")
                    .setAutoCancel(true);
            notificationManager.notify(0,notificationBuilder.build());
            initDownload(url);
        }
        else {
            listUrl.remove(url);
        }

    }

    private void initDownload(String url){
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.SERVER_URL).build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        final Call<ResponseBody> request = retrofitInterface.downloadFile(url);
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CANCEL_DOWNLOAD);
        bManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                throwx = true;
            }
        },intentFilter);
        try {
            String name = String.valueOf(id)+".zip";
            downloadFile(request.execute().body(),name);
        }
        catch (IOException e){
            Log.e("Fail initDownload",e.getMessage());
            onDownloadFail();
        }
    }


    private void downloadFile(ResponseBody body, String fileName) throws IOException {

        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File path = Environment.getExternalStoragePublicDirectory("BookOfMigas");
        File outputFile = new File(path, fileName);
        try{
            path.mkdirs();
            OutputStream output = new FileOutputStream(outputFile);
            long total = 0;
            long startTime = System.currentTimeMillis();
            int timeCount = 1;

            while ((count = bis.read(data)) != -1) {
                total += count;
                totalFileSize = (fileSize / (Math.pow(1024, 2)));
                double current = (total / (Math.pow(1024, 2)));
                int progress = (int) ((total * 100) / fileSize);
                long currentTime = System.currentTimeMillis() - startTime;
                Download download = new Download();
                download.setTotalFileSize(totalFileSize);
                download.setId(id);
                download.setPosition(pos);
                if (currentTime > 1000 * timeCount) {
                    download.setCurrentFileSize((float) current);
                    download.setProgress(progress);
                    sendNotification(download);
                    timeCount++;
                }
                if(throwx) {
                    throwx = false;
                    throw new IOException("Throw aku mas");
                }
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            bis.close();
            onDownloadComplete();
        }
        catch (IOException e){
            Log.e("Error writing",e.getMessage());
            onDownloadFail();
        }

    }

    private void sendNotification(Download download){

        sendIntent(download);
        notificationBuilder.setProgress(100,download.getProgress(),false);
        notificationBuilder.setContentText(String.format(Locale.ENGLISH,"Downloading file %.2f/%.2f MB",download.getCurrentFileSize(),totalFileSize));
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendIntent(Download download){
        Intent intent = new Intent(MESSAGE_PROGRESS);
        intent.putExtra("download",download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void onDownloadComplete(){
        Download download = new Download();
        download.setProgress(100);
        download.setId(id);
        download.setPosition(pos);
        sendIntent(download);

        notificationManager.cancel(0);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentText("Download Completed");
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void onDownloadFail(){
        Download download = new Download();
        download.setProgress(0);
        download.setId(id);
        download.setPosition(pos);
        sendIntent(download);

        notificationManager.cancel(0);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentText("Failed to Download");
        notificationManager.notify(0, notificationBuilder.build());

        Intent intent = new Intent(MESSAGE_FAIL_DOWNLOAD);
        intent.putExtra("download",download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(0);
    }
}
