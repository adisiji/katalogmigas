package neobyte.scode.webflip.model;

/**
 * Created by neobyte on 11/10/2016.
 */

public class FileItem {
    private double id;
    private String url,deviceId;

    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
