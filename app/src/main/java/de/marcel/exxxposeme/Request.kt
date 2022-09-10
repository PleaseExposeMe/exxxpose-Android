package de.marcel.exxxposeme

import okhttp3.OkHttpClient
import okhttp3.Request


class Request {
    private val client = OkHttpClient()

    fun run(url: String): String {
                val request: Request = Request.Builder()
                    .url(url)
                    .build()
                client.newCall(request).execute().use { response -> return response.body?.string().toString() }
    }
}