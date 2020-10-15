package com.coeater.android.api

import android.content.Context
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val baseUrl = "http://ec2-52-78-98-130.ap-northeast-2.compute.amazonaws.com:8000/api/"

fun provideAuthApi(): AuthApi =
        Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(provideOkHttpClient(provideLoggingInterceptor(), null))
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(AuthApi::class.java)
//
// fun provideApi(context: Context): GithubApi
//        = Retrofit.Builder()
//    .baseUrl(baseUrl)
//    .client(provideOkHttpClient(provideLoggingInterceptor(),
//        provideAuthInterceptor(provideAuthTokenProvider(context))))
//    .addConverterFactory(GsonConverterFactory.create())
//    .build()
//    .create(GithubApi::class.java)

private fun provideOkHttpClient(
    interceptor: HttpLoggingInterceptor,
    authInterceptor: AuthInterceptor?
): OkHttpClient =
        OkHttpClient.Builder()
    .run {
        if (null != authInterceptor) {
            addInterceptor(authInterceptor)
        }
        addInterceptor(interceptor)
        build()
    }

private fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

private fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
    val token = provider.token ?: throw IllegalStateException("authToken cannot be null.")
    return AuthInterceptor(token)
}

private fun provideAuthTokenProvider(context: Context): AuthTokenProvider =
        AuthTokenProvider(context.applicationContext)

internal class AuthInterceptor(private val token: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain):
            Response = with(chain) {
        val newRequest = request().newBuilder().run {
            addHeader("Authorization", "jwt " + token)
            build()
        }
        proceed(newRequest)
    }
}
