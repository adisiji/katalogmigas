package neobyte.scode.webflip.adapter;

import android.support.v7.widget.RecyclerView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmObject;

/**
 * Created by neobyte on 11/3/2016.
 */

public abstract class RealmRecyclerViewAdapter <T extends RealmObject> extends RecyclerView.Adapter {
    private RealmBaseAdapter<T> mRealmBaseAdapter;
    public T getItem(int position){
        return mRealmBaseAdapter.getItem(position);
    }

    public RealmBaseAdapter<T> getmRealmBaseAdapter() {
        return mRealmBaseAdapter;
    }
    public void setmRealmBaseAdapter(RealmBaseAdapter<T> realmBaseAdapter){
        mRealmBaseAdapter = realmBaseAdapter;
    }
}
