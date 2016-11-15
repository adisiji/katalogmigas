package neobyte.scode.webflip.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.realm.RealmBaseAdapter;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by neobyte on 11/3/2016.
 */

public class RealmModelAdapter <T extends RealmObject> extends RealmBaseAdapter<T> {
    public RealmModelAdapter(Context context, RealmResults<T> realmResults, boolean automaticUpdate){
        super(context,realmResults,automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        return null;
    }
}
