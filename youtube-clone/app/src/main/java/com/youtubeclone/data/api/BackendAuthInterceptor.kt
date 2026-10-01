package com.youtubeclone.data.api

import com.youtubeclone.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response

class BackendAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalUrl = original.url

        val url = originalUrl.newBuilder()
            .build()

        val request = original.newBuilder()
            .url(url)
            .build()

        return chain.proceed(request)
    }
}
