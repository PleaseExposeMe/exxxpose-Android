package de.marcel.exxxposeme

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.net.URISyntaxException


class Viewer : AppCompatActivity() {
    lateinit var webview: WebView
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    val REQUEST_SELECT_FILE = 100
    var firstLoad = true
    var title = "No title"
    val Link: MutableList<String> = ArrayList()
    var bookmarkBtnState = false
    var leaveTimeStemp =  System.currentTimeMillis()
    var postOpened = false

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        //File Upload
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return
            uploadMessage!!.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    intent
                )
            )
            uploadMessage = null
        }
    }
    @SuppressLint("SetJavaScriptEnabled", "Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        webview = findViewById(R.id.uploadview)
        webview.settings.javaScriptEnabled = true
        webview.settings.allowFileAccess = true
        webview.settings.domStorageEnabled = true
        webview.settings.allowContentAccess = true
        webview.webChromeClient = WebChromeClient()

        val refresh = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        refresh.setOnRefreshListener {
            webview.reload()
            refresh.isRefreshing = false
        }

        //Darkmode
        when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                if(WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(webview.settings, WebSettingsCompat.FORCE_DARK_ON);
                    webview.setBackgroundColor(Color.parseColor("#515151"))
                }
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                webview.setBackgroundColor(Color.parseColor("#f7f7f7"))
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                webview.setBackgroundColor(Color.parseColor("#f7f7f7"))
            }
        }

        /* An instance of this class will be registered as a JavaScript interface */
         class MyJavaScriptInterface() {
            @JavascriptInterface
            fun setTitle(aContent: String) {
                title = try{
                    aContent.replace("\n","");
                }catch (c: Exception){
                    aContent
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

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                //Check Internet Connection
                val webViewhelper = WebViewHelper()
                if(!webViewhelper.isOnline(applicationContext)){
                    return false
                }

                if (url == "https://www.exxxpose.me/") {
                    finish()
                } else
                if (url.startsWith("https://www.exxxpose.me/?")) {
                   finish()
                } else
                if (url == "https://www.exxxpose.me/games/") {
                    finish()
                } else
                if (url.startsWith("https://www.exxxpose.me/me/")) {
                    finish()
                } else
                if (url == "https://www.exxxpose.me/notifications/") {
                    finish()
                } else
                if (url == "https://www.exxxpose.me/login/loggedin.php") {
                     finish()
                } else if (url.contains("tracker/")) {

                } else if (url.contains("#tracker")) {
                            reload()
                } else if (url.contains("#comments")) {
                             reload()
                }else if (url == "https://www.exxxpose.me/games/raffle/wait.php") {
                    showPopup()
                }else if (url.startsWith("https://www.exxxpose.me/games/")) {

                }else {
                      if (!firstLoad) {
                           loadViewer(url)
                           webview.stopLoading()
                           return false
                      }
                }

                //Open special links
                if (url.startsWith("mailto:")) {
                    var intent: Intent? = null
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        intent.addCategory("android.intent.category.BROWSABLE")
                        intent.component = null
                        intent.selector = null
                        view.context.startActivity(intent)
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }
                    view.goBack()
                    view.stopLoading()
                    view.context.startActivity(intent)
                } else if (url.startsWith("tel:")) {
                    var intent: Intent? = null
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        intent.addCategory("android.intent.category.BROWSABLE")
                        intent.component = null
                        intent.selector = null
                        view.context.startActivity(intent)
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }
                    view.goBack()
                    view.stopLoading()
                    view.context.startActivity(intent)
                } else if (url.startsWith("whatsapp:")) {
                    var intent: Intent? = null
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        intent.addCategory("android.intent.category.BROWSABLE")
                        intent.component = null
                        intent.selector = null
                        view.context.startActivity(intent)
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }
                    view.goBack()
                    view.goBack()
                    view.stopLoading()
                    view.context.startActivity(intent)
                } else if (url.startsWith("tg:")) {
                    var intent: Intent? = null
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        intent.addCategory("android.intent.category.BROWSABLE")
                        intent.component = null
                        intent.selector = null
                        view.context.startActivity(intent)
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }
                    view.goBack()
                    view.goBack()
                    view.stopLoading()
                    view.context.startActivity(intent)
                } else if (url.startsWith("geo:")) {
                    var intent: Intent? = null
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        intent.addCategory("android.intent.category.BROWSABLE")
                        intent.component = null
                        intent.selector = null
                        view.context.startActivity(intent)
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }
                    view.goBack()
                    view.goBack()
                    view.stopLoading()
                    view.context.startActivity(intent)
                } else if (url.startsWith("sms:")) {
                    var intent: Intent? = null
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        intent.addCategory("android.intent.category.BROWSABLE")
                        intent.component = null
                        intent.selector = null
                        view.context.startActivity(intent)
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }
                    view.goBack()
                    view.goBack()
                    view.stopLoading()
                    view.context.startActivity(intent)
                } else if (Uri.parse(url).scheme == "market") {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        val host = view.context as Activity
                        host.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // Google Play app is not installed, you may want to open the app store link
                        val uri = Uri.parse(url)
                        view.loadUrl("http://play.google.com/store/apps/" + uri.host + "?" + uri.query)
                    }
                    view.goBack()
                    view.stopLoading()
                } else if (url.startsWith("intent:")) {
                    try {
                        val context = view.context
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        intent!!.addCategory("android.intent.category.BROWSABLE")
                        intent.component = null
                        intent.selector = null
                        if (intent != null) {
                            view.stopLoading()
                            val packageManager = context.packageManager
                            val info = packageManager.resolveActivity(
                                intent,
                                PackageManager.MATCH_DEFAULT_ONLY
                            )
                            if (info != null) {
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "App is not installed!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return true
                        }
                    } catch (e: URISyntaxException) {
                    }
                }else{
                    view.loadUrl(url)
                }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                progressBar.visibility = View.VISIBLE
                webview.visibility = View.INVISIBLE
                //JavaScript/CSS injection
                val css = "html{-webkit-tap-highlight-color: transparent;}" //your css as String
                val js = "var style = document.createElement('style'); style.innerHTML = '$css'; " +
                        "document.getElementsByClassName('menu-item-icon')[2].style.display = 'none';" +
                        "document.getElementsByClassName('menu-item-icon')[3].style.display = 'none';" +
                        "document.getElementsByClassName('menu-item-profile')[1].style.display = 'none';" +
                        "document.getElementsByClassName('mobile-menu-item')[0].style.display = 'none';" +
                        "document.getElementsByClassName('mobile-menu-item')[1].style.display = 'none';" +
                        "document.getElementsByClassName('mobile-menu-item')[2].style.display = 'none';" +
                        "document.getElementsByClassName('mobile-menu-item')[5].style.display = 'none';" +
                        "document.head.appendChild(style);"
                webview.loadUrl(
                    "javascript:(function() {"
                            + js +
                            "})()"
                )

                //JavaScript/CSS injection mobile header
                val jsHeader = "document.getElementsByClassName('header-mobile')[0].style.display = 'none';" +
                        "document.getElementsByTagName('footer')[0].style.display = 'none';"
                webview.loadUrl(
                    "javascript:(function() {"
                            + jsHeader +
                            "})()"
                )
                super.onPageStarted(view, url, favicon)
            }


            override fun onPageFinished(webView: WebView?, url: String?) {
                //Save cookies for login and popup
                CookieManager.getInstance().flush()

                //JavaScript/CSS injection
                val css = "html{-webkit-tap-highlight-color: transparent;}" //your css as String
                val js = "var style = document.createElement('style'); style.innerHTML = '$css'; " +
                        "document.getElementsByClassName('menu-item-icon')[2].style.display = 'none';" +
                        "document.getElementsByClassName('menu-item-icon')[3].style.display = 'none';" +
                        "document.getElementsByClassName('menu-item-profile')[1].style.display = 'none';" +
                        "document.getElementsByClassName('mobile-menu-item')[0].style.display = 'none';" +
                        "document.getElementsByClassName('mobile-menu-item')[1].style.display = 'none';" +
                        "document.getElementsByClassName('mobile-menu-item')[2].style.display = 'none';" +
                        "document.getElementsByClassName('mobile-menu-item')[5].style.display = 'none';" +
                        "document.head.appendChild(style);"
                webview.loadUrl(
                    "javascript:(function() {"
                            + js +
                            "})()"
                )

                //JavaScript/CSS injection mobile header
                val cssHeader = "main{padding-top: 20px !important;}" //your css as String
                val jsHeader = "var style = document.createElement('style'); style.innerHTML = '$cssHeader'; " +
                        "document.getElementsByClassName('header-mobile')[0].style.display = 'none';" +
                        "document.getElementsByTagName('footer')[0].style.display = 'none';" +
                        "document.head.appendChild(style);"
                webview.loadUrl(
                    "javascript:(function() {"
                            + jsHeader +
                            "})()"
                )

                if(url?.startsWith("https://www.exxxpose.me/messages/") == true){
                    //JavaScript/CSS messages
                    val cssMessages = ".header-mobile{display: none;} .mobile-box{top: 60px !important;height: calc(100% - 60px)!important;}.message-headers{top: 0px !important;}.message-headers{width: calc(100% - 40px)!important;} .message-headers span a{display: none !important;} .message-text-content{ height: calc(100% - 92px) !important; position: fixed !important; bottom: 0 !important; top: 0 !important; }" //your css as String
                    val jsMessages = "var style = document.createElement('style'); style.innerHTML = '$cssMessages'; " +
                            "document.head.appendChild(style);"
                    webview.loadUrl(
                        "javascript:(function() {"
                                + jsMessages +
                                "})()"
                    )
                }

                //make exxxpose.me links clickable
                if (url?.startsWith("https://www.exxxpose.me/profile/") == true || url?.startsWith("https://www.exxxpose.me/post/") == true) {
                    val jsLinks = "const comments = document.getElementsByClassName('message-content-text');\n" +
                            "Array.from(comments).forEach((item, index, arr) => {" +
                            "var text = item.innerHTML;" +
                            "if(text.includes(\"https://www.exxxpose.me/\")){" +
                            "var split = text.split(\"https://www.exxxpose.me/\");" +
                            "var path = split[1].split(\" \");" +
                            "comments[index].innerHTML = comments[index].innerHTML.replace(\"https://www.exxxpose.me/\" + path[0],\"<a href='\" + \"https://www.exxxpose.me/\" + path[0] + \"'>https://www.exxxpose.me/\" + path[0] + \"</a>\");" +
                            "comments[index].innerHTML = comments[index].innerHTML.replace(\"#comments\",\"\");" +
                            "}" +
                            "})"

                    val jsLinksDes = "let object = document.getElementsByClassName('card-notes')[0];" +
                            "let str = object.innerHTML;" +
                            "let doc = new DOMParser().parseFromString(str, 'text/html');" +
                            "const text = doc.querySelector('p').textContent;" +
                            "if(text.includes(\"https://www.exxxpose.me/\")){" +
                            "    var split = text.split(\"https://www.exxxpose.me/\");" +
                            "    var path = split[1].split(\" \");" +
                            "    object.innerHTML = object.innerHTML.replace(\"https://www.exxxpose.me/\" + path[0],\"<a href='\" + \"https://www.exxxpose.me/\" + path[0] + \"'>https://www.exxxpose.me/\" + path[0] + \"</a>\");" +
                            "}"

                    webview.loadUrl(
                        "javascript:(function() {"
                                + jsLinks +
                                "})()"
                    )
                    webview.loadUrl(
                        "javascript:(function() {"
                                + jsLinksDes +
                                "})()"
                    )
                }

                //Js Interface
                if (url?.startsWith("https://www.exxxpose.me/profile/") == true || url?.startsWith("https://www.exxxpose.me/post/") == true) {
                    webview.loadUrl("javascript:window.INTERFACE.setTitle(document.getElementsByTagName('h1')[0].innerText);");
                }

                if (url?.startsWith("https://www.exxxpose.me/post/") == true && url?.contains("report.php") != true && url?.contains("delete.php") != true || url?.startsWith("https://www.exxxpose.me/profile/") == true) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            //New history entry
                            val history = SQLlite(applicationContext, null)
                            history.addHistoryEntry(url,title)
                        },
                        600 // value in milliseconds
                    )
                }

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                        progressBar.visibility = View.INVISIBLE
                        webview.visibility = View.VISIBLE;

                        //Enable open new view on link click
                        firstLoad = false
                    },
                    400 // value in milliseconds
                )
                super.onPageFinished(webView, url)
            }

        }

        webview.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->

            val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)


            val request = DownloadManager.Request(
                Uri.parse(url)
            )
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setTitle("Image Download") //Notify client once download is completed!
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "exxxpose_files/$filename"
            )
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(
                applicationContext,
                "Downloading Image",  //To notify the Client that the file is being downloaded
                Toast.LENGTH_LONG
            ).show()
        })

        webview.setWebChromeClient(object : WebChromeClient() {

            //File Upload
            override fun onShowFileChooser(
                mWebView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }
                uploadMessage = filePathCallback
                val intent = fileChooserParams.createIntent()
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE)
                } catch (e: ActivityNotFoundException) {
                    uploadMessage = null
                    return false
                }
                return true
            }
        })

        //get url from intent extra
        val url :String = intent.getStringExtra("url").toString()

        //Check Internet Connection
        val webViewhelper = WebViewHelper()
        if(webViewhelper.isOnline(applicationContext)){
            //Load site
            webview.loadUrl(url)
        }else{
            webview.loadUrl("file:///android_asset/noconnection.html")
        }

        //close btn
        val btn = findViewById<ImageView>(R.id.btn)
        btn.setOnClickListener {
            if(webview.url?.startsWith("https://www.exxxpose.me/games/dice/round.php?") == true) {
                AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to exit the dice?")
                    .setMessage("You can no longer continue editing!")
                    .setNegativeButton("No", null)
                    .setPositiveButton(
                        "Yes"
                    ) { _, _ -> finish() }.create().show()
            }else{
                finish()
            }
        }


        if(url.startsWith("https://www.exxxpose.me/messages/")){
            val refresh = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
            refresh.isEnabled = false;
        }


        //open in default browser
        val shareButton = findViewById<ImageView>(R.id.share)
        shareButton.setOnClickListener {
            sharePost()
        }

        //enable open in default browser
        if (url.startsWith("https://www.exxxpose.me/post/") && !url.contains("report.php") && !url.contains("delete.php") || url.startsWith("https://www.exxxpose.me/profile/")) {
            shareButton.visibility = View.VISIBLE

            val floatingActionButton = findViewById<FloatingActionButton>(R.id.floating_action_button)
            floatingActionButton.setColorFilter(Color.parseColor("#8C8C8C"));
            floatingActionButton.visibility = View.VISIBLE
            val db = SQLlite(this, null)
            floatingActionButton.setOnClickListener {
                if(bookmarkBtnState) {
                    db.deleteBookmark(url)
                    bookmarkBtnState = false

                    bookmarkRemoved()
                }else{
                    if (url.startsWith("https://www.exxxpose.me/post/")){
                        db.addBookmark(url,title,"post")
                    }else{
                        db.addBookmark(url,title,"profile")
                    }

                    bookmarkBtnState = true

                    bookmarkAdded()
                }
            }

            // below is the variable for cursor
            // we have called method to get
            // all names from our database
            // and add to name text view
            val cursor = db.getBookmarks()

            // moving the cursor to first position and
            // appending value in the text view
            cursor!!.moveToFirst()

            cursor.moveToFirst();
            while (!cursor.isAfterLast) {
                Link.add(cursor.getString(cursor.getColumnIndex(SQLlite.LINK_COl)))
                cursor.moveToNext();
            }

            // at last we close our cursor
            cursor.close()
            //restore bookmark state

            for (l in Link){
                if(url==l){
                    bookmarkBtnState = true
                    bookmarkAdded()
                }
            }
        }
    }


    @SuppressLint("ResourceAsColor")
    fun Snackbar(message: String) {
        val contextView = findViewById<View>(R.id.uploadview)
        Snackbar.make(contextView, message, Snackbar.LENGTH_SHORT)
            .setAction("Dismiss") {
                // Responds to click on the action
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.main_green))
            .show()
    }

    fun showPopup() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout)




        bottomSheetDialog.findViewById<LinearLayout>(R.id.beginner)
            ?.setOnClickListener(View.OnClickListener {
                webview.loadUrl("https://www.exxxpose.me/games/raffle/success.php?type=beginner")
                bottomSheetDialog.dismiss()
            })

        bottomSheetDialog.findViewById<LinearLayout>(R.id.intermediate)
            ?.setOnClickListener(View.OnClickListener {
                webview.loadUrl("https://www.exxxpose.me/games/raffle/success.php?type=intermediate")
                bottomSheetDialog.dismiss()
            })

        bottomSheetDialog.findViewById<LinearLayout>(R.id.extreme)
            ?.setOnClickListener(View.OnClickListener {
                webview.loadUrl("https://www.exxxpose.me/games/raffle/success.php?type=extreme")
                bottomSheetDialog.dismiss()
            })

        bottomSheetDialog.show()
    }

    fun bookmarkAdded(){
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.floating_action_button)
        floatingActionButton.setColorFilter(Color.parseColor("#d3f51a"));
    }
    fun bookmarkRemoved(){
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.floating_action_button)
        floatingActionButton.setColorFilter(Color.parseColor("#8C8C8C"));
    }

    fun reload(){
        Handler(Looper.getMainLooper()).postDelayed(
            {

                webview.reload();
            },
            300 // value in milliseconds
        )
    }

    fun sharePost(){
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, "Sharing exposure")
        i.putExtra(Intent.EXTRA_TEXT, webview.url)
        startActivity(Intent.createChooser(i, "Share Post"))
    }

    override fun onResume() {
        super.onResume()
        //is post expired
        val difference = System.currentTimeMillis() - leaveTimeStemp
        if(difference < 250 && postOpened){
            if(webview.url?.startsWith("https://www.exxxpose.me/post/delete.php") == true){
                Snackbar("Post is deleted")
            }else{
                Snackbar("Post is expired")
            }
        }
        postOpened = false
    }

    override fun onBackPressed() {

       if(webview.url?.startsWith("https://www.exxxpose.me/games/dice/round.php?") == true) {
            AlertDialog.Builder(this)
                .setTitle("Are you sure you want to exit the dice?")
                .setMessage("You can no longer continue editing!")
                .setNegativeButton("No", null)
                .setPositiveButton(
                    "Yes"
                ) { _, _ -> super@Viewer.onBackPressed() }.create().show()
        }else{
           super@Viewer.onBackPressed()
       }
    }

    fun loadViewer(url: String){

        if(url.startsWith("https://www.exxxpose.me/post/")){
            postOpened = true
            leaveTimeStemp = System.currentTimeMillis()
        }

        //Check Internet Connection
        val webViewhelper = WebViewHelper()
        if(webViewhelper.isOnline(applicationContext)){
            val intent = Intent(this, Viewer::class.java)
            intent.putExtra("url", url)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }else{
            webview.loadUrl("file:///android_asset/noconnection.html")
        }
    }
}