package de.marcel.exxxposeme



import android.annotation.SuppressLint
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    lateinit var webview: WebView
    var error = true

    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        webview = findViewById(R.id.checkversion)
        webview.settings.javaScriptEnabled = true
        webview.loadUrl("http://exxxpose-extend.bplaced.net/")

        /* An instance of this class will be registered as a JavaScript interface */
        class MyJavaScriptInterface() {
            @JavascriptInterface
            fun getVersion(version: String) {
                val versionCode = BuildConfig.VERSION_CODE

                if(versionCode.toString() != version){
                    updateInfo()
                }else{
                    startApp()
                }
            }
        }

        webview.addJavascriptInterface(MyJavaScriptInterface(), "INTERFACE");


        webview.webViewClient = object : WebViewClient() {

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler,
                error: SslError?
            ) {
                handler.cancel()
            }




            override fun onPageFinished(webView: WebView?, url: String?) {
                //Js Interface
                webview.loadUrl("javascript:" +
                        "var text = document.body.innerText;" +
                        "window.INTERFACE.getVersion(text);")

                super.onPageFinished(webView, url)
            }


        }

        // in your method, just use:
        val versionName = findViewById<TextView>(R.id.version)
        versionName.text = BuildConfig.VERSION_NAME

        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(error){
                    startApp()
                }
            },
            2000 // value in milliseconds
        )

    }

    fun startApp(){
        error = false
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val intent = Intent(this, Main::class.java)
                startActivity(intent)
                finish()
            },
            600 // value in milliseconds
        )
    }

    fun updateInfo(){
        error = false
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val intent = Intent(this, NewUpdateAvailableView::class.java)
                startActivity(intent)
                finish()
            },
            600 // value in milliseconds
        )
    }
}