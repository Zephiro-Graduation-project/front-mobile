package com.example.frontzephiro.api;

import com.example.frontzephiro.models.LoginRequest;
import com.example.frontzephiro.models.LoginResponse;
import com.example.frontzephiro.models.MailDTO;
import com.example.frontzephiro.models.UserEntity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserApiService {

    @POST("user/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("user/register")
    Call<ResponseBody> register(@Body UserEntity user);

    @HTTP(method = "DELETE", path = "user/delete/{id}", hasBody = true)
    Call<ResponseBody> deleteAccount(@Path("id") String userId, @Body MailDTO mailDTO);
}
