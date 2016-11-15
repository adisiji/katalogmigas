package neobyte.scode.webflip.tools;

/**
 * Created by neobyte on 11/3/2016.
 */

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import io.realm.Realm;
import io.realm.RealmResults;
import neobyte.scode.webflip.model.pdf_item;


public class RealmController {

    private static RealmController instance;
    private final Realm realm;

    public RealmController(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Fragment fragment) {

        if (instance == null) {
            instance = new RealmController(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmController with(Activity activity) {

        if (instance == null) {
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application) {

        if (instance == null) {
            instance = new RealmController(application);
        }
        return instance;
    }

    public static RealmController getInstance() {

        return instance;
    }

    public Realm getRealm() {

        return realm;
    }

    //Refresh the realm istance
    public void refresh() {

        realm.refresh();
    }

    //clear all objects from pdf_item.class
    public void clearAll() {

        realm.beginTransaction();
        realm.clear(pdf_item.class);
        realm.commitTransaction();
    }

    //find all objects in the pdf_item.class
    public RealmResults<pdf_item> getpdf_items() {

        return realm.where(pdf_item.class).findAll();
    }

    //query a single item with the given id
    public pdf_item getPdf(String id) {

        return realm.where(pdf_item.class).equalTo("id", id).findFirst();
    }

    //check if pdf_item.class is empty
    public boolean hasPdf() {

        return !realm.allObjects(pdf_item.class).isEmpty();
    }

    //query example
    public RealmResults<pdf_item> queryedPdf() {

        return realm.where(pdf_item.class)
                .contains("author", "Author 0")
                .or()
                .contains("title", "Realm")
                .findAll();

    }
}