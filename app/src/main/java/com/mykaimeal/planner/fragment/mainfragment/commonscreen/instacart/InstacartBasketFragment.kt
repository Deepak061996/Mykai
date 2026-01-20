package com.mykaimeal.planner.fragment.mainfragment.commonscreen.instacart

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterCompareItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.databinding.FragmentInstacartBasketBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.instacartviewmodel.InstaCartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.Locale


@Suppress("DEPRECATION")
@AndroidEntryPoint
class InstacartBasketFragment : Fragment() {

    private lateinit var binding: FragmentInstacartBasketBinding
    private var linkUrl: String = ""
    private lateinit var priceExtractor: PriceExtractor
    private var openedBrowser = false
    private lateinit var instaCartViewModel: InstaCartViewModel
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: android.os.Bundle?): View {
        binding = FragmentInstacartBasketBinding.inflate(inflater, container, false)
        linkUrl = arguments?.getString("linkUrl") ?: "https://www.instacart.com/store"
        Log.d("linkUrl", "****** $linkUrl")
        instaCartViewModel = ViewModelProvider(requireActivity())[InstaCartViewModel::class.java]
        priceExtractor = PriceExtractor(requireContext())
        binding.rcyCompare.adapter = AdapterCompareItem()
        buttonActive(false)
        setupWebView(binding.webView)
        setupBackPress()
        setupUIListeners()
        return binding.root
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setSupportZoom(false)
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        // JS → Android bridge
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onLoginClicked(text: String) {
                Log.d("INSTACART_CLICK", "Clicked item = $text")
                val clickData=cleanButtonText(text.trim())
                Log.d("INSTACART_CLICK", "clickData$clickData")
                requireActivity().runOnUiThread {
                    if (clickData.isNotEmpty()){
                        binding.tvTotal.text = "Scan basket for\ntotal"
                    }
                    if (clickData.equals("log in",true) ||
                        clickData.equals("add ingredients to cart",true) ||
                        clickData.equals("account settings",true) ||
                        clickData.equals("continue shopping",true)){
                        openCheckoutInBrowser()
                    }
                }
            }
        }, "Android")
        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("SetTextI18n")
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                buttonActive(true)
                binding.tvTotal.text = "Scan basket for\ntotal"
                view?.evaluateJavascript(
                    """
                (function () {
                    if (window.__loginHooked) return;
                    window.__loginHooked = true;
                    document.addEventListener('click', function (e) {
                        var el = e.target;
                        while (el && el !== document.body) {
                            var text = el.innerText || "";
                            Android.onLoginClicked(text.trim());
                            break;
                            el = el.parentElement;
                        }
                    }, true);

                })();
                """.trimIndent(),
                    null
                )
            }
        }
        webView.loadUrl(linkUrl)
    }
    private fun cleanButtonText(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text
            .replace(Regex("\\d+"), "")   // remove numbers
            .replace(Regex("\\s+"), " ")  // remove extra spaces
            .trim()
            .lowercase()
    }

    private fun buttonActive(status: Boolean){
        if (status){
            binding.tvTotal.setTextColor("#FF000000".toColorInt())
            binding.imgScan.isEnabled=true
            binding.imgScan.isClickable=true
            binding.layScan.setBackgroundResource(R.drawable.scanbasket)
        }else{
            binding.tvTotal.setTextColor("#9A9A9A".toColorInt())
            binding.imgScan.isEnabled=false
            binding.imgScan.isClickable=false
            binding.layScan.setBackgroundResource(R.drawable.scanbasketdisable)
        }
    }
    private fun openCheckoutInBrowser() {
        val checkoutUrl = linkUrl
        val intent = Intent(Intent.ACTION_VIEW, checkoutUrl.toUri())
        startActivity(intent)
        openedBrowser = true
    }
    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) binding.webView.goBack()
                    else
                        findNavController().navigateUp()
                    findNavController().navigate(R.id.newMissingIngredientFragment)
                }
            })
    }
    @SuppressLint("DefaultLocale", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUIListeners() {
        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
            findNavController().navigate(R.id.newMissingIngredientFragment)
        }

        binding.imageRefresh.setOnClickListener {
            binding.webView.loadUrl(linkUrl)
        }
        binding.imgScan.setOnClickListener {
           /* BaseApplication.showMe(requireContext())
            val originalWidth = binding.webView.width
            val originalHeight = binding.webView.height
            captureFullPageImageFromWebView(binding.webView){
                binding.webView.postDelayed({
                    captureFullPageImageFromWebView(binding.webView){ bitmap->
                        if (bitmap != null) {
//                          saveBitmapToGalleryTesting(requireContext(), bitmap, "Mykai")
                            lifecycleScope.launch {
                                val base64Image = withContext(Dispatchers.Default) {
                                    bitmapToBase64(bitmap)
                                }
                                val approxTokens = (base64Image.length / 4.0).toInt()
                                Log.d("Base64", base64Image)
                                val request = GeminiRequest(
                                    contents = listOf(Content(parts = listOf(Part(inline_data = InlineData(mime_type = "image/jpeg",
                                        data = base64Image)),Part(
                                        text = """
Perform OCR on the image.
STRICT RULES:
- Read ONLY numbers that are clearly visible AND NOT crossed out.
- IGNORE any numbers that are:
  - crossed out
  - struck through
  - overwritten
  - scratched
  - marked with X, lines, or cancellation marks
- Do NOT guess, estimate, infer, or calculate missing values.
- Do NOT include handwritten corrections if original printed value is crossed.
- Prefer values explicitly labeled as:
  TOTAL, GRAND TOTAL, FINAL TOTAL, PAYABLE AMOUNT.
- If multiple valid totals exist, choose the highest clearly labeled final amount.
- If the final total is crossed out or unclear, return null.
Return ONLY valid JSON.
No markdown.
No explanation text.
{
  "detected_prices": [number],
  "final_total": number | null,
  "confidence": "high" | "medium" | "low"
}
""")))), generationConfig = GenerationConfig(temperature = 0.0, maxOutputTokens = approxTokens))
                                try {
                                    val response = GeminiRetrofit.api.analyzeImage(apiKey = getString(R.string.api_key_Gemni), body = request)
                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful) {
                                            val text = response.body()
                                                ?.candidates
                                                ?.firstOrNull()
                                                ?.content
                                                ?.parts
                                                ?.firstOrNull { it.text != null }
                                                ?.text
                                            val total = text?.let {
                                                try {
                                                    val json = JSONObject(it)
                                                    val pricesArray = json.optJSONArray("detected_prices")
                                                    (0 until (pricesArray?.length() ?: 0))
                                                        .mapNotNull { index ->
                                                            pricesArray?.optDouble(index)
                                                                ?.takeIf { !it.isNaN() }
                                                        }
                                                        .sum()
                                                } catch (e: Exception) {
                                                    Log.e("GEMINI_RESPONSE", "JSON parsing error", e)
                                                    0.0
                                                }
                                            } ?: 0.0
                                            val formattedTotal = String.format("%.2f", total)
                                            binding.tvTotal.text = "Total: $ $formattedTotal"
                                            Log.d("GEMINI_RESPONSE", formattedTotal)
                                        } else {
                                            binding.tvTotal.text = "Scan basket for\ntotal"
                                            Log.e("GEMINI_ERROR", response.errorBody()?.string() ?: "Unknown error")
                                        }
                                    }
                                }  catch (e: Exception) {
                                    Log.e("GEMINI_UNKNOWN", "Unexpected error", e)
                                    withContext(Dispatchers.Main) {
                                        binding.tvTotal.text = "Scan basket for\ntotal"
                                    }
                                } finally {
                                    withContext(Dispatchers.Main) {
                                        BaseApplication.dismissMe()
                                    }
                                }
                            }
                            binding.webView.postDelayed ({
//                                BaseApplication.dismissMe()
                                binding.layBottom.visibility = View.VISIBLE
                                binding.webView.measure(
                                    View.MeasureSpec.makeMeasureSpec(originalWidth, View.MeasureSpec.EXACTLY),
                                    View.MeasureSpec.makeMeasureSpec(originalHeight, View.MeasureSpec.EXACTLY)
                                )
                                binding.webView.layout(0, 0, originalWidth, originalHeight)
                                binding.webView.invalidate()
                                binding.webView.requestLayout()
                            },2000)
                        }
                    }
                },2000)
            }
*/
            getHtmlFromWebView(binding.webView)
        }
        binding.imgleft.setOnClickListener { if (binding.webView.canGoBack()) binding.webView.goBack() }
        binding.imgright.setOnClickListener { if (binding.webView.canGoForward()) binding.webView.goForward() }
    }


    fun getHtmlFromWebView(webView: WebView) {
        webView.evaluateJavascript(
            "(function() { return document.documentElement.outerHTML; })();"
        ) { html ->
            var cleanHtml = html
                .removePrefix("\"")
                .removeSuffix("\"")
                .replace("\\u003C", "<")
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
            cleanHtml = cleanHtml
                .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
            cleanHtml = cleanHtml.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            lifecycleScope.launch {
                Log.d("HtmlData", "***$cleanHtml")
//              sendHtmlToGemini(cleanHtml.trim())
                val (prices, total) = extractAllPricesAndTotal(cleanHtml)
                Log.d("TOTAL", total)
                binding.tvTotal.text = "Total: $ $total"

            }
        }
    }

    private fun extractAllPricesAndTotal(html: String): Pair<List<Double>, String> {
        val document = Jsoup.parse(html)
        val prices = document.select("span.e-gx2pr0").mapNotNull { parent ->
            val mainValue = parent.selectFirst("span.e-1qkvt8e")?.text() ?: ""
            val allCurrencySpans = parent.select("span.e-p745l")
            val decimal = if (allCurrencySpans.size > 1)
                allCurrencySpans.last()?.text()
            else
                ""
            val priceString =
                if (decimal?.isNotEmpty() == true)
                    "$mainValue.$decimal"
                else
                    mainValue
            priceString.toDoubleOrNull()
        }
        val total = prices.sum()
        val formattedTotal = "" + String.format(Locale.US, "%.2f", total)
        return prices to formattedTotal
    }

    @SuppressLint("SetTextI18n")
    suspend fun sendHtmlToGemini(htmlContent: String) {
        withContext(Dispatchers.IO) {
            val estimatedTokens = (htmlContent.length / 4) + 500
            val safeMaxTokens = estimatedTokens.coerceAtMost(8000)
            Log.d("safeMaxTokens", "*****$safeMaxTokens")
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = """
You are given HTML content from a shopping list page.

TASK:
- Extract ALL numeric prices displayed for items
- INCLUDE prices marked as:
  - "(estimated)"
  - "each (estimated)"
  - "/pkg (est.)"
  - "per item", "per unit", or similar labels
- Treat estimated, per-item, and per-package prices as VALID prices and include them in the total
- Identify the final total of all items (sum of all extracted prices if not explicitly given)
- Do NOT include non-price numbers (such as quantities, weights, item counts)
- Do NOT guess or infer missing prices
- Ignore ONLY prices that are crossed out or explicitly labeled as "old price"
Return ONLY JSON in this format:
{
  "final_total": number | null,
}
HTML CONTENT:
$htmlContent
""".trimIndent())))), generationConfig = GenerationConfig(temperature = 0.0, maxOutputTokens = safeMaxTokens))
            try {
                val response = GeminiRetrofit.api.analyzeHtml(
                    apiKey = getString(R.string.api_key_Gemni),
                    body = request
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val text = response.body()
                            ?.candidates
                            ?.firstOrNull()
                            ?.content
                            ?.parts
                            ?.firstOrNull { it.text != null }
                            ?.text
                        val total = parseFinalTotal(text)
                        if (total != null) {
                            binding.tvTotal.text = "Total: $ %.2f".format(total)
                        } else {
                            binding.tvTotal.text = "Scan basket for\ntotal"
                        }
                        Log.d("GEMINI_HTML_RESPONSE", text ?: "No response text")
                    } else {
                        binding.tvTotal.text = "Scan basket for\ntotal"
                        Log.e("GEMINI_HTML_ERROR", response.errorBody()?.string() ?: "Unknown error")
                    }
                }
            } catch (e: Exception) {
            Log.e("GEMINI_UNKNOWN", "Unexpected error", e)
            withContext(Dispatchers.Main) {
                binding.tvTotal.text = "Scan basket for\ntotal"
            }
        } finally {
            withContext(Dispatchers.Main) {
                BaseApplication.dismissMe()
            }
        }
        }
    }

    // JSON parsing function
    fun parseFinalTotal(responseText: String?): Double? {
        if (responseText.isNullOrBlank()) return null
        return try {
            val clean = responseText
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(clean)
            val total = json.optDouble("final_total", Double.NaN)
            if (!total.isNaN()) total else null
        } catch (e: Exception) {
            Log.e("JSON_PARSE_ERROR", "Failed to parse", e)
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(
            outputStream.toByteArray(),
            Base64.NO_WRAP
        )
    }

    private fun captureFullPageImageFromWebView(
        webView: WebView,
        targetWidthPx: Int = Resources.getSystem().displayMetrics.widthPixels,
        completion: (Bitmap?) -> Unit
    ) {
        try {
            webView.post {
                try {
                    val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(targetWidthPx, View.MeasureSpec.EXACTLY)
                    val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    webView.measure(widthMeasureSpec, heightMeasureSpec)
                    val measuredHeight = webView.measuredHeight
                    val bitmap = createBitmap(targetWidthPx, measuredHeight)
                    val canvas = Canvas(bitmap)
                    webView.layout(0, 0, targetWidthPx, measuredHeight)
                    webView.draw(canvas)
                    completion(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    completion(null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            completion(null)
        }
    }

    private fun saveBitmapToGalleryTesting(context: Context, bitmap: Bitmap, fileName: String) {
        val fos: OutputStream?
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/MykaiBasket")
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = resolver.openOutputStream(imageUri!!)

        if (fos != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }
        fos?.close()
    }

    override fun onResume() {
        super.onResume()
        if (openedBrowser) {
              openedBrowser = false
              findNavController().navigateUp()
              findNavController().navigate(R.id.newMissingIngredientFragment)
        }
    }

}

