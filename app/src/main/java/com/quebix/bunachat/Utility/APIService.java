package com.quebix.bunachat.Utility;

import com.quebix.bunachat.Notification.MyResponse;
import com.quebix.bunachat.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAbdUPFgc:APA91bH37bxEk7Sr41Yc_Qq5SZ_pB6bgnGmbl8UKDLrPktvN29P39qtwlooLfULrxIRQ461-fJ-nCcf28_NjmP6JBnMk5J5VlS7EyV1FoEw7SEiCBJBnJDwWfI5VunJtrx1rszmEnlQ6"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
