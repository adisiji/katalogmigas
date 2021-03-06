package neobyte.scode.webflip.tools;

/**
 * Created by neobyte on 11/7/2016.
 */
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface RetrofitInterface {

    @GET
    @Streaming
    Call<ResponseBody> downloadFile(@Url String fileUrl);
}