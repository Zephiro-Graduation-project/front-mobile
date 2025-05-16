// RetrofitClient.java
package com.example.frontzephiro.network;

import android.content.Context;
import android.util.Log;

import com.example.frontzephiro.utils.DateAdapter;
import com.example.frontzephiro.utils.TokenUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.Date;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.frontzephiro.api.GraphicApi;
import okhttp3.logging.HttpLoggingInterceptor;

public class RetrofitClient {
    // tus URLs existentes
    private static final String BASE_URL = "https://sv-auth.gentleground-3f80269d.eastus.azurecontainerapps.io/";
    private static final String CONTENT_BASE_URL = "https://sv-content.gentleground-3f80269d.eastus.azurecontainerapps.io";
    // nueva URL para artifact
    private static final String ARTIFACT_BASE_URL = "https://sv-artifacts.gentleground-3f80269d.eastus.azurecontainerapps.io/";
    // URLs para la tienda del jardin
    private static final String GAMIFICATION_BASE_URL = "https://sv-garden.gentleground-3f80269d.eastus.azurecontainerapps.io/";
    //url para perfilacion
    private static final String PROFILE_BASE_URL = "https://sv-psych-profiling.gentleground-3f80269d.eastus.azurecontainerapps.io/";

    // clientes existentes
    private static Retrofit retrofit = null;
    private static Retrofit retrofitContentPublic = null;
    private static Retrofit retrofitContentAuth = null;
    // Cliente autenticado para artifact
    private static Retrofit retrofitArtifactAuth = null;
    // nuevo cliente
    private static Retrofit retrofitArtifact = null;
    // Cliente autenticado para gamification
    private static Retrofit retrofitGamificationAuth = null;
    // cliente para gamificacion
    private static Retrofit retrofitGamification = null;
    //cliente para perfilacion
    private static Retrofit retrofitProfile = null;

    private static GraphicApi graphicApi = null;
    private static GraphicApi graphicApiAuth = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    // en RetrofitClient.java
    public static Retrofit getContentClient() {
        if (retrofitContentPublic == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                    Log.d("HTTP_CONTENT", message)
            );
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofitContentPublic = new Retrofit.Builder()
                    .baseUrl(CONTENT_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofitContentPublic;
    }


    public static Retrofit getAuthenticatedContentClient(final Context context) {
        if (retrofitContentAuth == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            String token = TokenUtils.getToken(context);
                            Request.Builder builder = original.newBuilder();
                            if (token != null && !token.isEmpty()) {
                                builder.header("Authorization", "Bearer " + token);
                            }
                            Request request = builder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            retrofitContentAuth = new Retrofit.Builder()
                    .baseUrl(CONTENT_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofitContentAuth;
    }

    /*** NUEVO MÉTODO PARA ARTIFACT ***/
    public static Retrofit getArtifactClient() {
        if (retrofitArtifact == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();
            retrofitArtifact = new Retrofit.Builder()
                    .baseUrl(ARTIFACT_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofitArtifact;
    }
    public static Retrofit getAuthenticatedArtifactClient(final Context context) {
        if (retrofitArtifactAuth == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            String token = TokenUtils.getToken(context);
                            Request.Builder builder = original.newBuilder();
                            if (token != null && !token.isEmpty()) {
                                builder.header("Authorization", "Bearer " + token);
                            }
                            Request request = builder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            retrofitArtifactAuth = new Retrofit.Builder()
                    .baseUrl(ARTIFACT_BASE_URL)            // http://10.0.2.2:8080/
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofitArtifactAuth;
    }

    public static Retrofit getGamificationClient() {
        if (retrofitGamification == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();
            retrofitGamification = new Retrofit.Builder()
                    .baseUrl(GAMIFICATION_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofitGamification;
    }
    public static Retrofit getAuthenticatedGamificationClient(final Context context) {
        if (retrofitGamificationAuth == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            String token = TokenUtils.getToken(context);
                            Request.Builder builder = original.newBuilder();
                            if (token != null && !token.isEmpty()) {
                                builder.header("Authorization", "Bearer " + token);
                            }
                            Request request = builder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            retrofitGamificationAuth = new Retrofit.Builder()
                    .baseUrl(GAMIFICATION_BASE_URL)            // http://10.0.2.2:8060/
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofitGamificationAuth;
    }

    public static Retrofit getProfileClient() {
        if (retrofitProfile == null) {
            // 1. Configura GSON
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();

            // 2. Crea un interceptor de logging que use el tag "HTTP"
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override public void log(String message) {
                    Log.d("HTTP", message);
                }
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 3. Usa ese interceptor en tu OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            // 4. Crea el Retrofit con ese cliente
            retrofitProfile = new Retrofit.Builder()
                    .baseUrl(PROFILE_BASE_URL)   // "http://10.0.2.2:5032/"
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofitProfile;
    }


    public static GraphicApi getGraphicApi() {
        if (graphicApi == null) {
            graphicApi = getArtifactClient()
                    .create(GraphicApi.class);
        }
        return graphicApi;
    }

    public static GraphicApi getAuthenticatedGraphicApi(final Context context) {
        if (graphicApiAuth == null) {
            graphicApiAuth = getAuthenticatedArtifactClient(context)
                    .create(GraphicApi.class);
        }
        return graphicApiAuth;
    }

}
