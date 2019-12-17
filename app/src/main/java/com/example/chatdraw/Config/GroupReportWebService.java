package com.example.chatdraw.Config;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface GroupReportWebService {
    @POST("1FAIpQLSfh81-NiLjUhYfQ-TD_UkHuuhRekJKzuXroahQaVZ0gq8Ypng/formResponse")
    @FormUrlEncoded
    Call<Void> completeQuestionnaire(
            @Field("entry.283431578") String groupName,
            @Field("entry.814847552") String userName,
            @Field("entry.470327294") String description
    );
}
