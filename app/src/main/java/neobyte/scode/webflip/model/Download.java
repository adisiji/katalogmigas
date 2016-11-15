package neobyte.scode.webflip.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by neobyte on 11/7/2016.
 */

public class Download implements Parcelable{

public Download(){

        }
private long id;
private int progress,position;
private float currentFileSize;
private double totalFileSize;

        public int getPosition() {
                return position;
        }

        public void setPosition(int position) {
                this.position = position;
        }

        public long getId() {
                return id;
        }

        public void setId(long id) {
                this.id = id;
        }

        public int getProgress() {
        return progress;
        }

public void setProgress(int progress) {
        this.progress = progress;
        }

public float getCurrentFileSize() {
        return currentFileSize;
        }

public void setCurrentFileSize(float currentFileSize) {
        this.currentFileSize = currentFileSize;
        }

public double getTotalFileSize() {
        return totalFileSize;
        }

public void setTotalFileSize(double totalFileSize) {
        this.totalFileSize = totalFileSize;
        }

        @Override
public int describeContents() {
        return 0;
        }

@Override
public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(progress);
        dest.writeFloat(currentFileSize);
        dest.writeDouble(totalFileSize);
        }

private Download(Parcel in) {

        progress = in.readInt();
        currentFileSize = in.readInt();
        totalFileSize = in.readDouble();
        }

public static final Parcelable.Creator<Download> CREATOR = new Parcelable.Creator<Download>() {
public Download createFromParcel(Parcel in) {
        return new Download(in);
        }

public Download[] newArray(int size) {
        return new Download[size];
        }
    };
}