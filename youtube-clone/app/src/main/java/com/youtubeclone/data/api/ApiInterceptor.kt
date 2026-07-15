package com.youtubeclone.data.api

import com.youtubeclone.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response

class ApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalUrl = original.url

        // Don't add the key if it's already present
        if (originalUrl.queryParameter("key") != null) {
            return chain.proceed(original)
        }

        val url = originalUrl.newBuilder()
            .addQueryParameter("key", Constants.API_KEY)
            .build()

        val request = original.newBuilder()
            .url(url)
            .build()

        return chain.proceed(request)
    }
}
