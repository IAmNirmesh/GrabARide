package rahul.nirmesh.grabaride.remote;

import rahul.nirmesh.grabaride.model.FCMResponse;
import rahul.nirmesh.grabaride.model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by NIRMESH on 19-Mar-18.
 */

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAkTCXK9g:APA91bHez_BrqJfGLDf24xlOsLnM7R1Y9ioxCOAKQQ7cjhIjjyerSCBAxH5pJWB42f62jRp1kPipf7DHWgPO16JSVPhQ5mX8rGWUGhUDYbbWNwFaCQ1DNJls99W9XOSZ08Lyq1ybqR2J"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
