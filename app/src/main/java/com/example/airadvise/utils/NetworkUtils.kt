package com.example.airadvise.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkUtils {
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityMananger = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityMananger.activeNetwork ?: return false
        val activeNetwork = connectivityMananger.getNetworkCapabilities(networkCapabilities) ?: return false

        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}