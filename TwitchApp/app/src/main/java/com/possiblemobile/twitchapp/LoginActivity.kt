package com.possiblemobile.twitchapp

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.possiblemobile.twitchapp.tasks.HandlerUserLoginTask
import android.content.Intent
import android.os.Build
import android.webkit.*

import kotlinx.android.synthetic.main.content_login.*

class LoginActivity : AppCompatActivity() {

    private val LOGIN_URL = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id=" + "vgl10ogqr6s8xqotaxc5256log6txm" + "&redirect_uri=http://localhost&scope=user_read"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_progress.visibility = View.VISIBLE
        initLoginView()

    }

    private fun initLoginView() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookie()
        val db = WebViewDatabase.getInstance(this)
        db.clearFormData()


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val ws = login_webview.settings
            ws.saveFormData = false
            ws.savePassword = false
        }

        login_webview.clearCache(true)
        login_webview.settings.javaScriptEnabled = true
        login_webview.settings.setSupportZoom(true)



        login_webview.webViewClient = object : WebViewClient() {

            @Suppress("DEPRECATION")
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                if (failingUrl == LOGIN_URL) {
                    handleNoInternet()
                }
            }

            @Suppress("DEPRECATION")
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                onReceivedError(view, error.errorCode, error.description.toString(), request.url.toString());
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return shouldOverrideUrlLoading(view, request.url.toString())
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.contains("access_token") && url.contains("localhost")) {
                    val mAccessToken = getAccessTokenFromURL(url)
                    login_webview.visibility = View.INVISIBLE
                    login_progress.visibility = View.VISIBLE
                    val handleTask = HandlerUserLoginTask()
                    val cm = CookieManager.getInstance()
                    cm.removeAllCookie()

                    view.clearCache(true)
                    view.clearHistory()
                    view.clearFormData()
                    handleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, baseContext, mAccessToken, this@LoginActivity)


                } else if (url.contains("The+user+denied+you+access")) {
                    handleUserCancel()
                    return true
                }

                return false
            }
        }
        login_webview.loadUrl(LOGIN_URL)
        login_progress.visibility = View.INVISIBLE

    }

    fun handleNoInternet() {

    }

    private fun getAccessTokenFromURL(url: String): String {
        val startIdentifier = "access_token"
        val endIdentifier = "&scope"

        val startIndex = url.indexOf(startIdentifier) + startIdentifier.length + 1
        val lastIndex = url.indexOf(endIdentifier)

        return url.substring(startIndex, lastIndex)
    }

    fun handleLoginSuccess() {
        login_progress.visibility = View.INVISIBLE
        val intent = Intent(baseContext, StreamsActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun handleUserCancel() {
        login_progress.visibility = View.VISIBLE
        login_webview.loadUrl(LOGIN_URL)
        login_progress.visibility = View.INVISIBLE

    }

    fun handleLoginFailure() {

    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }



}
