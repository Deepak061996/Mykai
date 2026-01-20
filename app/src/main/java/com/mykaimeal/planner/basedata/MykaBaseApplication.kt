package com.mykaimeal.planner.basedata

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.mykaimeal.planner.commonworkutils.AppsFlyerConstants
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


@HiltAndroidApp
class MykaBaseApplication : Application() {

    companion object {
        @Volatile
        var instance: MykaBaseApplication? = null
        fun getAppContext(): Context {
            return instance?.applicationContext
                ?: throw IllegalStateException("Application instance is null")
        }
    }

    @SuppressLint("RestrictedApi", "ObsoleteSdkInt")
    override fun onCreate() {
        super.onCreate()
        instance = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw()
        }

        keyHash()

        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
       /* FirebaseInstallations.getInstance().id
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    Log.w("FIS", "getId failed", task.exception)
                    return@addOnCompleteListener
                }
                Log.d("FIS", "Installation ID: " + task.result)
            }*/
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        registerReceiver(NetworkChangeReceiver(), filter)
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()

        AppsFlyerConstants.afDevKey

        val afDevKey = "M57zyjkFgb7nSQwHWN6isW"

        val conversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                Log.d("AppsFlyerssssssss", "Conversion success: $data")
                // Store in a singleton or SharedPreferences for later use
            }

            override fun onConversionDataFail(error: String?) {
                Log.e("AppsFlyerssssssss", "Conversion error: $error")
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {}
            override fun onAttributionFailure(error: String?) {}
        }

        AppsFlyerLib.getInstance().init(afDevKey, conversionListener, applicationContext)
        AppsFlyerLib.getInstance().start(applicationContext)

    }

    private fun keyHash(){
        try {
            val info = packageManager.getPackageInfo(
                "com.mykaimeal.planner",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }
}
