package neobyte.scode.webflip.adapter;

import android.content.Context;

import io.realm.RealmResults;
import neobyte.scode.webflip.model.pdf_item;

/**
 * Created by neobyte on 11/3/2016.
 */

public class RealmPdfAdapter extends RealmModelAdapter<pdf_item> {
    public RealmPdfAdapter(Context context, RealmResults<pdf_item> realmResults, boolean automaticUpdate){
        super(context,realmResults,automaticUpdate);
    }
}
