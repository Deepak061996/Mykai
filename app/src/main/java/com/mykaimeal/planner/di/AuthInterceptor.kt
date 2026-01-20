/*
package com.mykaimeal.planner.di
import android.annotation.SuppressLint
import android.content.Context
import com.mykaimeal.planner.basedata.SessionManagement
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(var context: Context) : Interceptor {

    @SuppressLint("SuspiciousIndentation")
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder: Request.Builder = chain.request().newBuilder()
        val token = getBearerToken()
        if (token != null && token.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
            requestBuilder.addHeader("Accept", "application/json") // Removed extra space after "Accept"
        }
        return chain.proceed(requestBuilder.build())
    }
    private fun getBearerToken(): String {
        val sessionManagement = SessionManagement(context)
        val token: String = sessionManagement.getAuthToken()!!

        return token
    }

}*/

package com.mykaimeal.planner.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.mykaimeal.planner.basedata.SessionManagement
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 🔹 Network check
        val networkType = getNetworkType()
        if (networkType == "NO_INTERNET") {
            throw IOException("No Internet Connection")
        }
        val originalRequest = chain.request()
        val requestBuilder: Request.Builder = originalRequest.newBuilder()
        // 🔹 Auth token
        val token = getBearerToken()
        if (token.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        requestBuilder.addHeader("Accept", "application/json")
        // 🔹 Network based header (optional but useful)
        when (networkType) {
            "WIFI" -> requestBuilder.addHeader("X-Network-Type", "WIFI")
            "MOBILE" -> requestBuilder.addHeader("X-Network-Type", "MOBILE")
        }
        return chain.proceed(requestBuilder.build())
    }
    private fun getBearerToken(): String {
        return SessionManagement(context).getAuthToken().orEmpty()
    }
    private fun getNetworkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "NO_INTERNET"
        val caps = cm.getNetworkCapabilities(network) ?: return "NO_INTERNET"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "MOBILE"
            else -> "OTHER"
        }
    }
}
