package com.mykaimeal.planner.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.databinding.ActivityIntroPageBinding
import com.mykaimeal.planner.databinding.FragmentWebViewByUrlBinding
import com.mykaimeal.planner.di.SessionEventBus
import com.mykaimeal.planner.messageclass.ErrorMessage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WebViewByUrlActivity : AppCompatActivity() {

    private lateinit var binding: FragmentWebViewByUrlBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use `inflate` method directly without creating an extra object
        binding = FragmentWebViewByUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var url = intent.getStringExtra("url") ?: ""


        binding.relBack.setOnClickListener{
            val resultIntent = Intent()
            resultIntent.putExtra("submitted_result", "close")
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }


        observeSessionExpiration()

        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadsImagesAutomatically = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccess = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Set a WebViewClient to capture URL clicks
        binding.webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, urlNew: String): Boolean {
                url=urlNew
                // Capture the clicked URL
//                Toast.makeText(requireContext(), "Clicked URL: $url", Toast.LENGTH_SHORT).show()
                 Log.d("Clicked URL:", "***$urlNew")
                // Decide whether to load the URL in the WebView
                view.loadUrl(urlNew) // Load the URL in the WebView
                return true // Return true if you handle the URL loading
            }
        }
        Log.d("url", "****$url")
        binding.webView.loadUrl(url)

        binding.rlImportApp.setOnClickListener{
            val resultIntent = Intent()
            resultIntent.putExtra("submitted_result", url)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    }

    private fun observeSessionExpiration() {
        lifecycleScope.launch {
            SessionEventBus.sessionExpiredFlow.collectLatest {
                if (!isDialogShown) {
                    isDialogShown = true
                    showSessionExpiredDialog()
                }
            }
        }
    }

    private fun showSessionExpiredDialog() {
        BaseApplication.alertError(this, ErrorMessage.sessionError,true)
    }

    private var isDialogShown = false

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val resultIntent = Intent()
        resultIntent.putExtra("submitted_result", "close")
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
