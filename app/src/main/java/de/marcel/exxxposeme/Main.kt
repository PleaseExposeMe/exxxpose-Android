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
import com.beust.klaxon.Klaxon
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.Serializable
import java.net.URISyntaxException


class Main : AppCompatActivity() {
    lateinit var webview: WebView
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    val REQUEST_SELECT_FILE = 100
    var onLogin = false
    var leaveTimeStemp =  System.currentTimeMillis()
    var postOpened = false
    var disableOnClickEvents = false
    var isUpdateAvailible = false
    var messageNotification = false
    var urlBeforeError = ""

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

    @Serializable
    data class Data(val version: String)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_home)

        val refresh = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        refresh.setOnRefreshListener {
            if(webview.url == "file:///android_asset/noconnection.html"){
                webview.loadUrl(urlBeforeError)
            }else{
                webview.reload()
            }
            refresh.isRefreshing = false
        }

        webview = findViewById(R.id.webview)
        webview.settings.javaScriptEnabled = true
        webview.settings.allowFileAccess = true
        webview.settings.domStorageEnabled = true
        webview.settings.allowContentAccess = true
        webview.webChromeClient = WebChromeClient()
        webview.setBackgroundColor(Color.parseColor("#f7f7f7"))

        //Darkmode
        when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                if(WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(webview.settings, WebSettingsCompat.FORCE_DARK_ON);
                }
                webview.setBackgroundColor(Color.parseColor("#212121"))
            }
            Configuration.UI_MODE_NIGHT_NO -> {}
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        var bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)


        /* An instance of this class will be registered as a JavaScript interface */
        class MyJavaScriptInterface() {
            @JavascriptInterface
            fun checkNotification(notificationState: Boolean) {
                val badge = bottomNavigationView.getOrCreateBadge(R.id.notifications)
                if(notificationState){
                    badge.isVisible = true
                    badge.backgroundColor = ContextCompat.getColor(applicationContext, R.color.main_green)
                }else{
                    badge.isVisible = false
                }
            }
            @JavascriptInterface
            fun updateMessagesBadge(notificationState: Boolean) {
                messageNotification = notificationState
            }
            @JavascriptInterface
            fun showWebview() {

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
                    updateURLBeforeError()
                    webview.loadUrl("file:///android_asset/noconnection.html")
                    return false
                }

                if (url == "https://www.exxxpose.me/") {
                    bottomNavigationView.selectedItemId = R.id.home;
                } else
                if (url.startsWith("https://www.exxxpose.me/?")) {

                } else
                if (url == "https://www.exxxpose.me/games/") {
                    bottomNavigationView.selectedItemId = R.id.games;
                } else
                if (url == "https://www.exxxpose.me/me/") {
                    bottomNavigationView.selectedItemId = R.id.account
                } else
                if (url == "https://www.exxxpose.me/search/") {
                    bottomNavigationView.selectedItemId = R.id.search
                } else
                if (url == "https://www.exxxpose.me/notifications/") {
                    bottomNavigationView.selectedItemId = R.id.notifications;
                }else
                if (url == "https://www.exxxpose.me/login/logout.php") {
                    //Nothing
                }else
                if (url.startsWith("https://www.exxxpose.me/terms.php")) {
                    //Nothing
                } else
                if (url.startsWith("https://www.exxxpose.me/notifications/clear.php")) {
                    //Nothing
                }else
                if (url.startsWith("https://www.exxxpose.me/post/clear.php")) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            bottomNavigationView.selectedItemId = R.id.account
                        },
                        300 // value in milliseconds
                    )
                }else
                if (url.contains("#exposures")) {
                    //Nothing
                } else {

                    //reload after Login
                    if (url.contains("/login/")) {
                        onLogin = true
                    }

                    loadViewer(url)
                    webview.stopLoading()
                    return false
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
                    } catch (e: URISyntaxException) {
                    }
                }else{
                    view.loadUrl(url)
                }
                return true
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                updateURLBeforeError()
                webview.loadUrl("file:///android_asset/noconnection.html")
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                progressBar.visibility = View.VISIBLE

                webview.visibility = View.INVISIBLE;

                //JavaScript/CSS injection mobile header
                val cssHeader = "main{padding-top: 20px !important;} .mobile-grid-container{margin-top: -20px !important;}" //your css as String
                val jsHeader = "var style = document.createElement('style'); style.innerHTML = '$cssHeader'; " +
                        "document.getElementsByClassName('header-mobile')[0].style.display = 'none';" +
                        "document.head.appendChild(style);"
                webview.loadUrl(
                    "javascript:(function() {"
                            + jsHeader +
                            "})()"
                )

                if(url == "https://www.exxxpose.me/"){
                    disableOnClickEvents = true
                    bottomNavigationView.selectedItemId = R.id.home
                    disableOnClickEvents = false
                }else  if(url == "https://www.exxxpose.me/notifications/"){
                    disableOnClickEvents = true
                    bottomNavigationView.selectedItemId = R.id.notifications
                    disableOnClickEvents = false
                }else  if(url == "https://www.exxxpose.me/me/"){
                    disableOnClickEvents = true
                    bottomNavigationView.selectedItemId = R.id.account
                    disableOnClickEvents = false
                }else  if(url == "https://www.exxxpose.me/games/"){
                    disableOnClickEvents = true
                    bottomNavigationView.selectedItemId = R.id.games
                    disableOnClickEvents = false
                }else  if(url == "https://www.exxxpose.me/search/"){
                    disableOnClickEvents = true
                    bottomNavigationView.selectedItemId = R.id.search
                    disableOnClickEvents = false
                }

                super.onPageStarted(view, url, favicon)
            }


            override fun onPageFinished(webView: WebView?, url: String?) {
                //Save cookies for login and popup
                CookieManager.getInstance().flush()


                //JavaScript/CSS injection mobile header
                val cssHeader = "html{-webkit-tap-highlight-color: transparent;} main{padding-top: 20px !important;} .mobile-grid-container{margin-top: -20px !important;}" //your css as String
                val jsHeader = "var style = document.createElement('style'); style.innerHTML = '$cssHeader'; " +
                        "document.getElementsByClassName('header-mobile')[0].style.display = 'none';" +
                        "document.head.appendChild(style);"
                webview.loadUrl(
                    "javascript:(function() {"
                            + jsHeader +
                            "})()"
                )

                //Js Interface
                webview.loadUrl("javascript:" +
                        "var notificationState = false;" +
                        "var notificationIcon = document.getElementsByClassName('menu-item-icon')[3].innerHTML;" +
                        "if(notificationIcon.includes('<span class=\"notification-bubble\">')){" +
                        "notificationState = true;" +
                        "}" +
                        "window.INTERFACE.checkNotification(notificationState);" +
                        "var messageState = false;" +
                        "var messageIcon = document.getElementsByClassName('menu-item-icon')[2].innerHTML;" +
                        "if(messageIcon.includes('<span class=\"notification-bubble\">')){" +
                        "messageState = true;" +
                        "}" +
                        "window.INTERFACE.updateMessagesBadge(messageState);")


                val menu = findViewById<ImageView>(R.id.menu)
                if (url == "https://www.exxxpose.me/me/") {
                    menu.visibility = View.VISIBLE
                }else{
                    menu.visibility = View.GONE
                }

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                        progressBar.visibility = View.INVISIBLE
                        webview.visibility = View.VISIBLE
                    },
                    250 // value in milliseconds
                )

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        updateMessageBadgeVisibility(messageNotification)
                    },
                    1000 // value in milliseconds
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


        webview.webChromeClient = object : WebChromeClient() {

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
        }

        //Check Internet Connection
        val webViewhelper = WebViewHelper()
        if(webViewhelper.isOnline(applicationContext)){
            //Load site
            webview.loadUrl("https://www.exxxpose.me/")

            //Show update popup

            //Call api to get current app version
            val thread = Thread {
                try {
                    val versionCode = BuildConfig.VERSION_CODE
                    val json = Request().run("http://exxxpose-extend.bplaced.net/api/getCurrentAppVersion/")

                    val result = Klaxon()
                        .parse<Data>(json)

                    if (result != null) {
                        if(result.version != versionCode.toString()){
                            isUpdateAvailible = true
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            thread.start()

            //Check if a update is available
            Handler(Looper.getMainLooper()).postDelayed(
                {
                   /* while (true){
                        if(isUpdateAvailible){
                            showUpdatePopup()
                            return@postDelayed
                        }
                    }*/
                    if(isUpdateAvailible){
                        showUpdatePopup()
                    }
                },
                2000 // value in milliseconds
            )
        }else{
            updateURLBeforeError()
            webview.loadUrl("file:///android_asset/noconnection.html")
        }


        //Floating btn
        val fab = findViewById<FloatingActionButton>(R.id.floating_action_button)
        fab.setOnClickListener {
                loadViewer("https://www.exxxpose.me/upload")
        }


        //AppBar Buttons
        val messagesbtn = findViewById<ImageView>(R.id.messagesbtn)
        messagesbtn.setOnClickListener {
            loadViewer("https://www.exxxpose.me/messages/");
        }

        val menubtn = findViewById<ImageView>(R.id.menu)
        menubtn.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_menu_layout)


            bottomSheetDialog.findViewById<LinearLayout>(R.id.bookmarks)
                ?.setOnClickListener(View.OnClickListener {
                    val intent = Intent(this, Bookmarks::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    bottomSheetDialog.dismiss()
                })

            bottomSheetDialog.findViewById<LinearLayout>(R.id.history)
                ?.setOnClickListener(View.OnClickListener {
                    val intent = Intent(this, History::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    bottomSheetDialog.dismiss()
                })


            bottomSheetDialog.show()
        }

        updateMessageBadgeVisibility(false)

        //Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener  { item ->
            when (item.itemId) {
                R.id.home -> {
                    if(!disableOnClickEvents){
                        //Check Internet Connection
                        if(webViewhelper.isOnline(applicationContext)){
                            //Load site
                            webview.loadUrl("https://www.exxxpose.me/")
                        }else{
                            updateURLBeforeError()
                            webview.loadUrl("file:///android_asset/noconnection.html")
                        }
                    }
                    true
                }
                R.id.notifications -> {
                    if(!disableOnClickEvents){
                        //Check Internet Connection
                        if(webViewhelper.isOnline(applicationContext)){
                            //Load site
                            webview.loadUrl("https://www.exxxpose.me/notifications/")
                        }else{
                            updateURLBeforeError()
                            webview.loadUrl("file:///android_asset/noconnection.html")
                        }
                    }
                    true
                }
                R.id.account -> {
                    if(!disableOnClickEvents){
                        //Check Internet Connection
                        if(webViewhelper.isOnline(applicationContext)){
                            //Load site
                            webview.loadUrl("https://www.exxxpose.me/me/")
                        }else{
                            updateURLBeforeError()
                            webview.loadUrl("file:///android_asset/noconnection.html")
                        }
                    }
                    true
                }
                R.id.search -> {
                    if(!disableOnClickEvents){
                        //Check Internet Connection
                        if(webViewhelper.isOnline(applicationContext)){
                            //Load site
                            webview.loadUrl("https://www.exxxpose.me/search/")
                        }else{
                            updateURLBeforeError()
                            webview.loadUrl("file:///android_asset/noconnection.html")
                        }
                    }
                    true
                }
                R.id.games -> {
                    if(!disableOnClickEvents){
                        //Check Internet Connection
                        if(webViewhelper.isOnline(applicationContext)){
                            //Load site
                            webview.loadUrl("https://www.exxxpose.me/games/")
                        }else{
                            updateURLBeforeError()
                            webview.loadUrl("file:///android_asset/noconnection.html")
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    fun Snackbar(message: String) {
        val contextView = findViewById<View>(R.id.webview)
        Snackbar.make(contextView, message, Snackbar.LENGTH_SHORT)
            .setAction("Dismiss") {
                // Responds to click on the action
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.main_green))
            .show()
    }

    override fun onResume() {
        super.onResume()

        val url = webview.url

        if (url == "https://www.exxxpose.me/me/" || url == "https://www.exxxpose.me/notifications/") {
            webview.visibility = View.INVISIBLE;
            webview.reload();
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    webview.visibility = View.VISIBLE;
                },
                1000 // value in milliseconds
            )
        }
        if(onLogin){
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigationView.selectedItemId = R.id.account
            onLogin = false
        }else{
            //Update bottom nav

            if(url == "https://www.exxxpose.me/"){
                val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                disableOnClickEvents = true
                bottomNavigationView.selectedItemId = R.id.home
                disableOnClickEvents = false
            }else  if(url == "https://www.exxxpose.me/notifications/"){
                val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                disableOnClickEvents = true
                bottomNavigationView.selectedItemId = R.id.notifications
                disableOnClickEvents = false
            }else  if(url == "https://www.exxxpose.me/me/"){
                val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                disableOnClickEvents = true
                bottomNavigationView.selectedItemId = R.id.account
                disableOnClickEvents = false
            }else  if(url == "https://www.exxxpose.me/games/"){
                val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                disableOnClickEvents = true
                bottomNavigationView.selectedItemId = R.id.games
                disableOnClickEvents = false
            }else  if(url == "https://www.exxxpose.me/search/"){
                val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                disableOnClickEvents = true
                bottomNavigationView.selectedItemId = R.id.search
                disableOnClickEvents = false
            }
        }

        //is post expired
        val difference = System.currentTimeMillis() - leaveTimeStemp
        if(difference < 250 && postOpened){
            Snackbar("Post is expired")
        }
        postOpened = false
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Really Exit?")
            .setMessage("Are you sure you want to exit?")
            .setNegativeButton("No", null)
            .setPositiveButton("Yes"
            ) { _, _ -> super@Main.onBackPressed() }.create().show()
    }

    fun showUpdatePopup(){
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout_update)


        bottomSheetDialog.findViewById<LinearLayout>(R.id.getTheUpdate)
            ?.setOnClickListener(View.OnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/exxxposeApp/exxxpose-Android"))
                startActivity(browserIntent)
                bottomSheetDialog.dismiss()
            })

        bottomSheetDialog.findViewById<LinearLayout>(R.id.Dismiss)
            ?.setOnClickListener(View.OnClickListener {
                bottomSheetDialog.dismiss()
            })


        bottomSheetDialog.show()
    }


    //Badge

    fun updateMessageBadgeVisibility(msgState: Boolean){
        val badge = findViewById<ImageView>(R.id.messagesBadge)
        if(msgState){
            badge.visibility = View.VISIBLE;
        }else{
            badge.visibility = View.GONE;
        }
    }

    fun updateURLBeforeError(){
        var webviewURL = webview.url.toString()
        if(webviewURL != "file:///android_asset/noconnection.html"){
            urlBeforeError = webviewURL
        }
    }


    fun loadViewer(url: String){
        if(url.startsWith("https://www.exxxpose.me/post/")){
            postOpened = true
            leaveTimeStemp = System.currentTimeMillis()
        }

        if(url.startsWith("https://www.exxxpose.me/")){
            val intent = Intent(this, Viewer::class.java)
            intent.putExtra("url", url)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }else{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
    }
}








