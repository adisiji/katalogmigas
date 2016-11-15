package neobyte.scode.webflip.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import io.realm.Realm;
import neobyte.scode.webflip.R;
import neobyte.scode.webflip.model.pdf_item;
import neobyte.scode.webflip.tools.Constants;
import neobyte.scode.webflip.tools.NetworkChangeReceiver;
import neobyte.scode.webflip.tools.RealmController;

import static android.view.View.GONE;

/**
 * Created by neobyte on 11/3/2016.
 */

public class PDFAdapter extends RealmRecyclerViewAdapter<pdf_item> {

    private DownloadPDFInterface downloadPDFInterface;
    private DeletePDFInterface deletePDFInterface;
    private OpenPDFInterface openPDFInterface;
    private CancelDownloadInterface cancelDownloadInterface;
    final Context context;
    private Realm realm;

    public void setDownloadPDFInterface(DownloadPDFInterface downloadPDFInterface){
        this.downloadPDFInterface = downloadPDFInterface;
    }
    public void setOpenPDFInterface(OpenPDFInterface openPDFInterface){
        this.openPDFInterface = openPDFInterface;
    }

    public void setDeletePDFInterface(DeletePDFInterface deletePDFInterface){
        this.deletePDFInterface = deletePDFInterface;
    }

    public interface DownloadPDFInterface {
        void downPDF(View view, int position);
    }

    public interface OpenPDFInterface {
        void openPDF(View view, int position);
    }

    public interface DeletePDFInterface {
        void delPDF(View view, int position);
    }

    public interface CancelDownloadInterface{
        void cancelDown(View view, int position, int act);
    }

    public void setCancelDownInterface(CancelDownloadInterface cancelDownInterface){
        this.cancelDownloadInterface = cancelDownInterface;
    }

    public PDFAdapter(Context context){
        this.context = context;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewtype){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_pdf, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position){
        realm = RealmController.getInstance().getRealm();

        final pdf_item PDF = getItem(position);

        final ItemViewHolder holder = (ItemViewHolder)viewHolder;

        holder.title.setText(PDF.getJudul());
        holder.setIsRecyclable(false);
        /**
         * Set Custom Font
        try{
            Typeface face= Typeface.createFromAsset(context.getAssets(), "fonts/Georgia.ttf");
            holder.title.setTypeface(face);
        }
        catch(Exception e)
        {
            Log.e("error get font",e.getMessage());
        }
         */

        if(!NetworkChangeReceiver.isConnected()){
            holder.download.setEnabled(false);
        }
        else
        {
            holder.download.setEnabled(true);
        }


        if(PDF.getStats()==0) { //stats not downloaded yet
            holder.lindown.setVisibility(View.VISIBLE);
            holder.linproses.setVisibility(GONE);
            holder.linoke.setVisibility(GONE);
            holder.download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadPDFInterface.downPDF(v, position);
                }
            });
            Glide.with(context)
                    .load(R.drawable.ic_arrow_left_api)
                    .asBitmap()
                    .fitCenter()
                    .into(holder.coverbook);
        }
        else if (PDF.getStats()==1){ //queue downloading file
            holder.lindown.setVisibility(GONE);
            holder.linproses.setVisibility(View.VISIBLE);
            holder.linoke.setVisibility(GONE);
            holder.cancelDown.setClickable(false);
            holder.coverbook.setPadding(8,0,0,0);
            holder.downloaded.setText(context.getString(R.string.loading_all));
            Glide.with(context)
                    .load(R.drawable.close)
                    .asBitmap()
                    .fitCenter()
                    .into(holder.cancelDown);
            holder.cancelDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelDownloadInterface.cancelDown(view, position, 1);
                }
            });
            Glide.with(context)
                    .load(R.drawable.ic_download)
                    .asBitmap()
                    .fitCenter()
                    .into(holder.coverbook);
        }

        else if(PDF.getStats()==2) { //downloading file
            holder.downloaded.setText(PDF.getDownloaded());
            holder.progressBar.setProgress(PDF.getProgress());
            Glide.with(context)
                    .load(R.drawable.close)
                    .asBitmap()
                    .fitCenter()
                    .into(holder.cancelDown);
            holder.cancelDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelDownloadInterface.cancelDown(view, position, 2);
                }
            });
        }
        else if (PDF.getStats()==3) { //have been downloaded
            holder.coverbook.setPadding(0,0,0,0);
            holder.lindown.setVisibility(GONE);
            holder.linproses.setVisibility(GONE);
            holder.linoke.setVisibility(View.VISIBLE);
            holder.open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPDFInterface.openPDF(view,position);
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletePDFInterface.delPDF(v,position);
                }
            });
            Glide.with(context)
                    .load(Constants.DPATH+PDF.getId()+"/files/shot.png")
                    .asBitmap()
                    .into(holder.coverbook);
        }
    }

    @Override
    public int getItemCount(){
        if(getmRealmBaseAdapter()!=null){
            return getmRealmBaseAdapter().getCount();
        }
        return 0;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView title,downloaded;
        public ImageView coverbook, cancelDown;
        public Button download,open,delete;
        public ProgressBar progressBar;
        public LinearLayout lindown;
        public RelativeLayout linoke,linproses;

        public ItemViewHolder(View view){
            super(view);
            title = (TextView)view.findViewById(R.id.btn_pdf);
            download = (Button)view.findViewById(R.id.btn_download_item);
            open = (Button)view.findViewById(R.id.btn_open_item);
            delete = (Button)view.findViewById(R.id.btn_delete_item);
            downloaded = (TextView)view.findViewById(R.id.progress_txt);
            progressBar = (ProgressBar)view.findViewById(R.id.progress_download);
            coverbook = (ImageView)view.findViewById(R.id.img_cover);
            cancelDown = (ImageView)view.findViewById(R.id.cancel_download);
            lindown = (LinearLayout)view.findViewById(R.id.root_download);
            linoke = (RelativeLayout) view.findViewById(R.id.root_oke);
            linproses = (RelativeLayout)view.findViewById(R.id.root_proses_down);
        }
    }
}
