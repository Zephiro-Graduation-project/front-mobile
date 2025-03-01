package com.example.frontzephiro.api;

import com.example.frontzephiro.models.LoginRequest;
import com.example.frontzephiro.models.LoginResponse;
import com.example.frontzephiro.models.UserEntity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApiService {

    @POST("user/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("user/register")
    Call<ResponseBody> register(@Body UserEntity user);
}
