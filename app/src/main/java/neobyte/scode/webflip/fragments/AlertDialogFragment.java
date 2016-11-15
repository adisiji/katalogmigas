package neobyte.scode.webflip.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import neobyte.scode.webflip.services.DownloadService;

/**
 * Created by neobyte on 11/14/2016.
 */

public class AlertDialogFragment extends DialogFragment {

    public AlertDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static AlertDialogFragment newInstance(String title, String judul, long id, int pos, int act) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("judul",judul);
        args.putLong("id",id);
        args.putInt("pos",pos);
        args.putInt("act",act);
        frag.setArguments(args);
        return frag;
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String action = "error";
        final int act = getArguments().getInt("act");
        if(act==1) {
            action = "menghapus";
        }
        else if(act == 3 || act ==2){
            action = "membatalkan download";
        }
        final String judul = getArguments().getString("judul");
        final long id = getArguments().getLong("id");
        final int pos = getArguments().getInt("pos");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage("Apakah anda ingin "+action+" file \""+judul+"\" ?");
        alertDialogBuilder.setPositiveButton("Ok",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on agreed
                if(act == 1) {
                    AlertDialogListener listener = (AlertDialogListener)getActivity();
                    listener.onFinishDialog(id,pos,judul);
                }
                else if(act==2){ //hapus queue
                    CancelDownloadListener listener = (CancelDownloadListener)getActivity();
                    listener.onCancelDown(2,pos,id);
                }
                else if(act == 3) { //cancel download
                    CancelDownloadListener listener = (CancelDownloadListener)getActivity();
                    listener.onCancelDown(3,pos,id);
                }

            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return alertDialogBuilder.create();
    }

    public interface AlertDialogListener{
        void onFinishDialog(long id,int position, String judul);
    }

    public interface CancelDownloadListener{
        void onCancelDown(int act, int pos, long id);
    }

}