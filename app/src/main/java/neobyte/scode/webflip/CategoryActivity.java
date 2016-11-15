package neobyte.scode.webflip;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import neobyte.scode.webflip.adapter.PDFAdapter;
import neobyte.scode.webflip.adapter.RealmPdfAdapter;
import neobyte.scode.webflip.fragments.AlertDialogFragment;
import neobyte.scode.webflip.model.Download;
import neobyte.scode.webflip.model.FileItem;
import neobyte.scode.webflip.model.pdf_item;
import neobyte.scode.webflip.services.DownloadService;
import neobyte.scode.webflip.tools.Constants;
import neobyte.scode.webflip.tools.NetworkChangeReceiver;
import neobyte.scode.webflip.tools.PrefUtil;
import neobyte.scode.webflip.tools.RealmController;
import neobyte.scode.webflip.tools.ZipPower;

import static neobyte.scode.webflip.tools.Constants.CANCEL_DOWNLOAD;
import static neobyte.scode.webflip.tools.Constants.MESSAGE_FAIL_DOWNLOAD;
import static neobyte.scode.webflip.tools.Constants.MESSAGE_PROGRESS;

/**
 * Created by neobyte on 11/3/2016.
 */

public class CategoryActivity extends AppCompatActivity implements NetworkChangeReceiver.ConnectivityReceiverListener,
        AlertDialogFragment.AlertDialogListener , AlertDialogFragment.CancelDownloadListener{

    private PDFAdapter adapter;
    private Realm realm;
    private RealmResults<pdf_item> dataModelDbs;
    private RealmPdfAdapter realmPdfAdapter;
    private RecyclerView recycler;
    private ProgressDialog prgDialog;
    private BackendlessDataQuery backendlessDataQuery;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private String urlx;

    @Override
    public void onNetworkConnectionChanged(boolean b){
        showSnack(b);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        checkConnection();
        if(Build.VERSION.SDK_INT>=21){
            Fade fade = new Fade();
            fade.setDuration(1000);
            getWindow().setEnterTransition(fade);

            Slide slide = new Slide();
            slide.setDuration(1000);
            getWindow().setReturnTransition(slide);
        }
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Semua Buku");
        }
        prgDialog = new ProgressDialog(this);
        backendlessDataQuery = new BackendlessDataQuery();
        backendlessDataQuery.setPageSize(50);

        long last_id = PrefUtil.getPrefLong(this,"last_id");

        recycler = (RecyclerView)findViewById(R.id.recycler);
        this.realm = RealmController.with(this).getRealm();

        if(NetworkChangeReceiver.isConnected()){
            if(last_id==0){
                setRealmData(null);
            }
            else {
                String query = "id >"+last_id;
                setRealmData(query);
            }
        }

        setupRecyclerView();
        // refresh the realm instance
        RealmController.with(this).refresh();
        setRealmAdapter(RealmController.with(this).getpdf_items());
    }

    @Override
    protected void onResume(){
        super.onResume();
        MyPDFApp.getInstance().setConnectivityListener(this);
        dataModelDbs = realm.where(pdf_item.class).findAll();
        realmPdfAdapter = new RealmPdfAdapter(getApplicationContext(),dataModelDbs,true);
        adapter.setmRealmBaseAdapter(realmPdfAdapter);
        adapter.notifyDataSetChanged();
        registerReceiver();
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
                .make(findViewById(R.id.lin_home), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    private void checkConnection() {
        boolean isConnected = NetworkChangeReceiver.isConnected();
        if(!isConnected){
            showSnack(false);
        }
    }

    private void setupRecyclerView() {
        recycler.setItemAnimator(new LandingAnimator());
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setLayoutManager(layoutManager);
        adapter = new PDFAdapter(this);
        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(adapter);
        recycler.setAdapter(animationAdapter);
        recycler.setHasFixedSize(true);
        adapter.setDownloadPDFInterface(new PDFAdapter.DownloadPDFInterface() {
            @Override
            public void downPDF(View view, int position) {
                //method download data
                if(checkPermission()){
                    long id = adapter.getItem(position).getId();
                    String url = adapter.getItem(position).getLink();
                    downData(id);
                    adapter.notifyItemChanged(position);
                    startDownload(position,id,url);
                } else {
                    requestPermission();
                }
            }
        });

        adapter.setOpenPDFInterface(new PDFAdapter.OpenPDFInterface(){
            @Override
            public void openPDF(View view, int position){
                long id = adapter.getItem(position).getId();
                Toast.makeText(getApplicationContext(), "Membuka "+adapter.getItem(position).getJudul(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), ViewPDF_Activity.class);
                String path = "file://"+Constants.DPATH+String.valueOf(id)+"/index.html";
                i.putExtra("pathurl",path);
                startActivity(i);
            }
        });

        adapter.setDeletePDFInterface(new PDFAdapter.DeletePDFInterface() {
            @Override
            public void delPDF(View view, int position) {
                //method delete data
                long id = adapter.getItem(position).getId();
                String judul = adapter.getItem(position).getJudul();
                showAlertDialog("Hapus File",judul,id,position,1);
            }
        });

        adapter.setCancelDownInterface(new PDFAdapter.CancelDownloadInterface() {
            @Override
            public void cancelDown(View view, int position, int act) {
                long id = adapter.getItem(position).getId();
                String judul = adapter.getItem(position).getJudul();
                urlx = adapter.getItem(position).getLink();
                if(act==1) {
                    //remove from queue
                    showAlertDialog("Cancel Download",judul,id,position,2);
                }
                else if (act==2) {
                    showAlertDialog("Cancel Download",judul,id,position,3);
                }

            }
        });
    }

    private void showAlertDialog(String title, String judul, long id, int pos, int act) {
        FragmentManager fm = getSupportFragmentManager();
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(title,judul,id,pos,act);
        alertDialog.show(fm, "fragment_alert");
    }

    @Override
    public void onFinishDialog(long id,int position, String judul){
        hapusitem(id);
        adapter.notifyItemChanged(position);
        File file = new File(Constants.DPATH+String.valueOf(id));
        deleteFileFolder deleteFileFolder = new deleteFileFolder(file,judul,1);
        deleteFileFolder.execute();
    }

    @Override
    public void onCancelDown(int act, int pos, long id){
        if(act==2) {
            DownloadService.removeQdownload(urlx);
            realm.beginTransaction();
            pdf_item pdfItem = realm.where(pdf_item.class).equalTo("id",id).findFirst();
            pdfItem.setStats(0);
            pdfItem.setProgress(0);
            pdfItem.setDownloaded("loading...");
            realm.commitTransaction();
            adapter.notifyItemChanged(pos);
        }
        else if(act==3){
            Intent intent = new Intent(CANCEL_DOWNLOAD);
            LocalBroadcastManager.getInstance(CategoryActivity.this).sendBroadcast(intent);
        }
    }

    private void setRealmAdapter(RealmResults<pdf_item> pdfItems){
        RealmPdfAdapter realmPdfAdapter = new RealmPdfAdapter(this.getApplicationContext(), pdfItems, true);
        // Set the data and tell the RecyclerView to draw
        adapter.setmRealmBaseAdapter(realmPdfAdapter);
        adapter.notifyDataSetChanged();
    }

    private void downData(long id){
        realm.beginTransaction();
        pdf_item pdfItem = realm.where(pdf_item.class).equalTo("id",id).findFirst();
        pdfItem.setProgress(0);
        pdfItem.setStats(1);
        realm.commitTransaction();
    }

    private void hapusitem(long id){
        realm.beginTransaction();
        pdf_item pdfItem = realm.where(pdf_item.class).equalTo("id",id).findFirst();
        pdfItem.setStats(0);
        realm.commitTransaction();
    }

    private void startDownload(int pos, long id, String url){
        Intent intent = new Intent(this,DownloadService.class);
        intent.putExtra("id",id);
        intent.putExtra("item_url",url);
        intent.putExtra("position",pos);
        startService(intent);
    }

    private void registerReceiver(){

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        intentFilter.addAction(MESSAGE_FAIL_DOWNLOAD);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MESSAGE_PROGRESS)){
                Download download = intent.getParcelableExtra("download");
                if(download.getProgress() == 100){ //data selesai didownload
                    unzipFile(download);
                    realm.beginTransaction();
                    pdf_item pdfItem = realm.where(pdf_item.class).equalTo("id",download.getId()).findFirst();
                    pdfItem.setStats(3);
                    pdfItem.setProgress(0);
                    pdfItem.setDownloaded("loading...");
                    realm.commitTransaction();
                    adapter.notifyItemChanged(download.getPosition());
                    Toast.makeText(getApplicationContext(), "Data berhasil didownload", Toast.LENGTH_SHORT).show();
                } else {
                    realm.beginTransaction(); //downloading process
                    pdf_item pdfItem = realm.where(pdf_item.class).equalTo("id",download.getId()).findFirst();
                    pdfItem.setStats(2);
                    pdfItem.setProgress(download.getProgress());
                    pdfItem.setDownloaded(String.format(Locale.ENGLISH,"Downloaded (%.2f/%.2f) MB",download.getCurrentFileSize(),download.getTotalFileSize()));
                    realm.commitTransaction();
                    adapter.notifyDataSetChanged();
                }
            }
            else if(intent.getAction().equals(MESSAGE_FAIL_DOWNLOAD)){
                Download download = intent.getParcelableExtra("download");
                realm.beginTransaction();
                pdf_item pdfItem = realm.where(pdf_item.class).equalTo("id",download.getId()).findFirst();
                pdfItem.setStats(0);
                pdfItem.setProgress(0);
                pdfItem.setDownloaded("loading...");
                realm.commitTransaction();
                adapter.notifyItemChanged(download.getPosition());
                Toast.makeText(getApplicationContext(),"Download Gagal",Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;
        }
    }

    private void requestPermission(){

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("GOOOD","result permission");
                } else {
                    Snackbar.make(findViewById(R.id.lin_home),"Permission Denied, Please allow to proceed !", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void setRealmData(@Nullable String query){
        prgDialog.setMessage(getString(R.string.loading_prg_bar));
        prgDialog.setCancelable(false);
        prgDialog.show();
        if(query!=null) {
            backendlessDataQuery.setWhereClause(query);
        }
        Backendless.Persistence.of(FileItem.class).find(backendlessDataQuery, new AsyncCallback<BackendlessCollection<FileItem>>() {
            @Override
            public void handleResponse(BackendlessCollection<FileItem> response) {
                long i=PrefUtil.getPrefLong(CategoryActivity.this,"last_id");
                List<FileItem> items = response.getCurrentPage();
                for(FileItem fileItem : items){
                    String url = fileItem.getUrl();
                    pdf_item pdfItem = new pdf_item();
                    pdfItem.setId((long)fileItem.getId());
                    pdfItem.setLink(url.substring(url.indexOf(".com")+4));
                    pdfItem.setJudul(url.substring(url.lastIndexOf("/")+1,url.lastIndexOf(".")).replace("-"," "));
                    pdfItem.setStats(0);
                    pdfItem.setDownloaded("loading...");
                    pdfItem.setProgress(0);
                    realm.beginTransaction();
                    realm.copyToRealm(pdfItem);
                    if(i<(long)fileItem.getId()){
                        i = (long)fileItem.getId();
                    }
                    realm.commitTransaction();
                    adapter.notifyItemInserted(dataModelDbs.size()-1);
                }
                PrefUtil.savePreference(CategoryActivity.this,"last_id",i);
                prgDialog.dismiss();
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                prgDialog.dismiss();
                Toast.makeText(CategoryActivity.this,getString(R.string.fail_loading_prgbar),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unzipFile(Download download){
        String source = Constants.DPATH+download.getId()+".zip";
        File file = new File(source);
        deleteFileFolder deleteFileFolder = new deleteFileFolder(file,"",2);
        deleteFileFolder.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()) );
        return true;
    }

    private class deleteFileFolder extends AsyncTask<Void,Void,Void>{

        private File file;
        private String judul;
        private int stats;

        public deleteFileFolder(File file, String judul, int stats){
            this.file = file;
            this.judul = judul;
            this.stats = stats;
        }

        @Override
        protected void onPreExecute(){
            if(stats!=1){
                ZipPower power = new ZipPower();
                power.unzip(file.getPath());
            }
        }

        @Override
        protected Void doInBackground(Void... params){
            deleteFolder(file);
            return null;
        }

        private void deleteFolder(File fileOrDirectory){
            if (fileOrDirectory.isDirectory()){
                for (File child : fileOrDirectory.listFiles())
                    deleteFolder(child);
            }
            fileOrDirectory.delete();
        }

        @Override
        protected void onPostExecute(Void result){
            if(stats==1){ //hasil hapus dari button
                Toast.makeText(getApplicationContext(),judul+" berhasil dihaspus",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Log.d("Delete Finished",file.getPath());
            }
        }
    }

}
