package com.example.frontzephiro.api;

import com.example.frontzephiro.models.LoginResponse;
import com.example.frontzephiro.models.UserEntity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApiService {

    @POST("user/login")
    Call<LoginResponse> login(@Body UserEntity user);

    @POST("user/register")
    Call<ResponseBody> register(@Body UserEntity user);

}