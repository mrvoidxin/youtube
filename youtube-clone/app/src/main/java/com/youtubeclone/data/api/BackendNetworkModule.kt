package com.youtubeclone.data.api

import com.youtubeclone.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackendNetworkModule {

    @Provides
    @Singleton
    fun provideBackendLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideBackendAuthInterceptor(): BackendAuthInterceptor {
        return BackendAuthInterceptor()
    }

    @Provides
    @Singleton
    fun provideBackendOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: BackendAuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideBackendRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBackendApiService(retrofit: Retrofit): BackendApiService {
        return retrofit.create(BackendApiService::class.java)
    }
}
